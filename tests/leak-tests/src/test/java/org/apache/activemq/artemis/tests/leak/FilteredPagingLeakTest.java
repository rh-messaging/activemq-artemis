/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.tests.leak;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.checkleak.core.CheckLeak;
import io.netty.util.collection.IntObjectMap;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.core.paging.PagingStore;
import org.apache.activemq.artemis.core.paging.cursor.impl.PageSubscriptionImpl;
import org.apache.activemq.artemis.core.protocol.core.impl.RemotingConnectionImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.Queue;
import org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl;
import org.apache.activemq.artemis.core.server.impl.AddressInfo;
import org.apache.activemq.artemis.core.server.impl.ServerStatus;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.apache.activemq.artemis.tests.util.CFUtil;
import org.apache.activemq.artemis.utils.Wait;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.activemq.artemis.tests.leak.MemoryAssertions.assertMemory;

/* This test creates a condition where one queue is filtering a lot of data.
*  Upon completing a page after the ignored filter.
*  it will then make sure the removed references list and acked list is cleared from the PageInfo map. */
public class FilteredPagingLeakTest extends ActiveMQTestBase {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   ActiveMQServer server;

   @BeforeClass
   public static void beforeClass() throws Exception {
      Assume.assumeTrue(CheckLeak.isLoaded());
   }

   @After
   public void validateServer() throws Exception {
      CheckLeak checkLeak = new CheckLeak();

      // I am doing this check here because the test method might hold a client connection
      // so this check has to be done after the test, and before the server is stopped
      assertMemory(checkLeak, 0, RemotingConnectionImpl.class.getName());

      server.stop();

      server = null;

      clearServers();
      ServerStatus.clear();

      assertMemory(checkLeak, 0, ActiveMQServerImpl.class.getName());
   }

   @Override
   @Before
   public void setUp() throws Exception {
      server = createServer(true, createDefaultConfig(1, true));
      server.getConfiguration().setJournalPoolFiles(4).setJournalMinFiles(2);
      server.start();
   }

   @Test
   public void testCore() throws Exception {
      doTest("CORE");
   }

   private void doTest(String protocol) throws Exception {
      int MESSAGES = 10_000;
      int MESSAGE_SIZE = 104;
      int COMMIT_INTERVAL = 100;
      int FILTERED_A = 100;

      CheckLeak checkLeak = new CheckLeak();
      int initialMaps = checkLeak.getAllObjects(IntObjectMap.class).length;

      ExecutorService executorService = Executors.newFixedThreadPool(2);
      runAfter(executorService::shutdownNow);

      ConnectionFactory cf = CFUtil.createConnectionFactory(protocol, "tcp://localhost:61616");

      server.addAddressInfo(new AddressInfo(getName()).addRoutingType(RoutingType.MULTICAST));
      server.createQueue(new QueueConfiguration("A").setAddress(getName()).setRoutingType(RoutingType.MULTICAST).setDurable(true).setFilterString("i >= " + (MESSAGES - FILTERED_A)));
      server.createQueue(new QueueConfiguration("B").setAddress(getName()).setRoutingType(RoutingType.MULTICAST).setDurable(true));

      final Queue serverQueueA = server.locateQueue("A");

      PagingStore pagingStore = serverQueueA.getPagingStore();
      pagingStore.startPaging();

      try (Connection connection = cf.createConnection()) {
         Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
         MessageProducer producer = session.createProducer(session.createTopic(getName()));
         connection.start();

         for (int i = 0; i < MESSAGES; i++) {
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(new byte[MESSAGE_SIZE]);
            message.setIntProperty("i", i);
            producer.send(message);
            if (i > 0 && i % COMMIT_INTERVAL == 0) {
               session.commit();
               pagingStore.forceAnotherPage();
            }
         }
         session.commit();
      }

      try (Connection connection = cf.createConnection()) {
         Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
         MessageConsumer consumer = session.createConsumer(session.createQueue("A"));
         connection.start();

         for (int i = 0; i < FILTERED_A; i++) {
            BytesMessage message = (BytesMessage) consumer.receive(5000);
            Assert.assertNotNull(message);
            logger.debug("Received {}", message.getIntProperty("i"));
            if (i % 100 == 0) {
               session.commit();
            }
         }
         Assert.assertNull(consumer.receiveNoWait());
         session.commit();
      }

      Future<Boolean> future = serverQueueA.getPagingStore().getCursorProvider().scheduleCleanup();
      future.get();

      Object[] pageCursorInfos = checkLeak.getAllObjects(PageSubscriptionImpl.PageCursorInfo.class);
      logger.debug("There are {} PageCursorInfo elements in the heap", pageCursorInfos.length);

      for (Object cursorInfoObject : pageCursorInfos) {
         PageSubscriptionImpl.PageCursorInfo cursorInfo = (PageSubscriptionImpl.PageCursorInfo) cursorInfoObject;
         if (cursorInfo.getCompletePageInformation() != null) {
            // this is asserting if the fix is in place
            Assert.assertNull(cursorInfo.getAcks());
            Assert.assertNull(cursorInfo.getRemovedReferences());
         }
      }

      Assert.assertTrue(checkLeak.getAllObjects(IntObjectMap.class).length - initialMaps <= 300);

      try (Connection connection = cf.createConnection()) {
         Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
         MessageConsumer consumer = session.createConsumer(session.createQueue("B"));
         connection.start();

         for (int i = 0; i < MESSAGES; i++) {
            BytesMessage message = (BytesMessage) consumer.receive(5000);
            Assert.assertNotNull(message);
            logger.debug("Received {}", message.getIntProperty("i"));
            if (i % 100 == 0) {
               session.commit();
            }
         }
         Assert.assertNull(consumer.receiveNoWait());
         session.commit();
      }

      Wait.assertFalse(serverQueueA.getPagingStore()::isPaging);
   }
}