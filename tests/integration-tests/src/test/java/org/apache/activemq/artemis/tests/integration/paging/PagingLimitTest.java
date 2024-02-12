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
package org.apache.activemq.artemis.tests.integration.paging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.impl.AddressInfo;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.logs.AssertionLoggerHandler;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.apache.activemq.artemis.tests.util.CFUtil;
import org.apache.activemq.artemis.tests.util.Wait;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PagingLimitTest extends ActiveMQTestBase {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   ActiveMQServer server;

   @Test
   public void testPageLimitMessageCoreFail() throws Exception {
      testPageLimitMessage("CORE", false);
   }

   @Test
   public void testPageLimitAMQPFail() throws Exception {
      testPageLimitMessage("AMQP", false);
   }

   @Test
   public void testPageLimitMessagesOpenWireFail() throws Exception {
      testPageLimitMessage("OPENWIRE", false);
   }

   @Test
   public void testPageLimitMessageCoreDrop() throws Exception {
      testPageLimitMessage("CORE", false);
   }

   @Test
   public void testPageLimitAMQPDrop() throws Exception {
      testPageLimitMessage("AMQP", false);
   }

   @Test
   public void testPageLimitMessagesOpenWireDrop() throws Exception {
      testPageLimitMessage("OPENWIRE", false);
   }

   public void testPageLimitMessage(String protocol, boolean drop) throws Exception {

      String queueNameTX = getName() + "_TX";
      String queueNameNonTX = getName() + "_NONTX";

      Configuration config = createDefaultConfig(true);
      config.setJournalSyncTransactional(false).setJournalSyncTransactional(false);

      final int PAGE_MAX = 20 * 1024;

      final int PAGE_SIZE = 10 * 1024;

      int pageLimitMessages = 300;

      server = createServer(true, config, PAGE_SIZE, PAGE_MAX, -1, -1, null, Long.valueOf(pageLimitMessages), drop ? "DROP" : "FAIL", null);
      server.start();

      server.addAddressInfo(new AddressInfo(queueNameTX).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameTX).setRoutingType(RoutingType.ANYCAST));
      server.addAddressInfo(new AddressInfo(queueNameNonTX).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameNonTX).setRoutingType(RoutingType.ANYCAST));

      Wait.assertTrue(() -> server.locateQueue(queueNameNonTX) != null);
      Wait.assertTrue(() -> server.locateQueue(queueNameTX) != null);

      testPageLimitMessageFailInternal(queueNameTX, protocol, true, drop, pageLimitMessages);
      testPageLimitMessageFailInternal(queueNameNonTX, protocol, false, drop, pageLimitMessages);


      // Update PageLimitMessages to 400
      pageLimitMessages = 400;
      ActiveMQServerControl serverControl = (ActiveMQServerControl)server.getManagementService().getResource(ResourceNames.BROKER);
      AddressSettings defaultAddressSettings = AddressSettings.fromJSON(serverControl.getAddressSettingsAsJSON("#"));
      defaultAddressSettings.setPageLimitMessages(Long.valueOf(pageLimitMessages));
      serverControl.addAddressSettings("#", defaultAddressSettings.toJSON());


      String queueNameTX1 = queueNameTX + "1";
      String queueNameNonTX1 = queueNameNonTX + "1";

      server.addAddressInfo(new AddressInfo(queueNameTX1).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameTX1).setRoutingType(RoutingType.ANYCAST));
      server.addAddressInfo(new AddressInfo(queueNameNonTX1).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameNonTX1).setRoutingType(RoutingType.ANYCAST));

      Wait.assertTrue(() -> server.locateQueue(queueNameTX1) != null);
      Wait.assertTrue(() -> server.locateQueue(queueNameNonTX1) != null);

      testPageLimitMessageFailInternal(queueNameTX1, protocol, true, drop, pageLimitMessages);
      testPageLimitMessageFailInternal(queueNameNonTX1, protocol, false, drop, pageLimitMessages);

      // Update PageLimitMessages to 500
      pageLimitMessages = 500;
      defaultAddressSettings.setPageLimitMessages(Long.valueOf(pageLimitMessages));
      serverControl.addAddressSettings("#", defaultAddressSettings.toJSON());


      server.stop();
      server.start();


      String queueNameTX2 = queueNameTX + "2";
      String queueNameNonTX2 = queueNameNonTX + "2";

      server.addAddressInfo(new AddressInfo(queueNameTX2).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameTX2).setRoutingType(RoutingType.ANYCAST));
      server.addAddressInfo(new AddressInfo(queueNameNonTX2).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameNonTX2).setRoutingType(RoutingType.ANYCAST));

      Wait.assertTrue(() -> server.locateQueue(queueNameTX2) != null);
      Wait.assertTrue(() -> server.locateQueue(queueNameNonTX2) != null);

      testPageLimitMessageFailInternal(queueNameTX2, protocol, true, drop, pageLimitMessages);
      testPageLimitMessageFailInternal(queueNameNonTX2, protocol, false, drop, pageLimitMessages);
   }

   private void testPageLimitMessageFailInternal(String queueName,
                                                 String protocol,
                                                 boolean transacted,
                                                 boolean drop,
                                                 int pageLimitMessages) throws Exception {
      org.apache.activemq.artemis.core.server.Queue serverQueue = server.locateQueue(queueName);
      Assert.assertNotNull(serverQueue);

      ConnectionFactory factory = CFUtil.createConnectionFactory(protocol, "tcp://localhost:61616");
      try (Connection connection = factory.createConnection()) {
         Session session = connection.createSession(transacted, transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
         Queue queue = session.createQueue(queueName);
         MessageProducer producer = session.createProducer(queue);
         connection.start();

         for (int i = 0; i < 100; i++) {
            TextMessage message = session.createTextMessage("initial " + i);
            message.setIntProperty("i", i);
            producer.send(message);
         }
         if (transacted) {
            session.commit();
            Assert.assertTrue(serverQueue.getPagingStore().isPaging());
         }

         for (int i = 0; i < pageLimitMessages; i++) {
            if (i == 200) {
               // the initial sent has to be consumed on transaction as we need a sync on the consumer for AMQP
               try (MessageConsumer consumer = session.createConsumer(queue)) {
                  for (int initI = 0; initI < 100; initI++) {
                     TextMessage recMessage = (TextMessage) consumer.receive(1000);
                     Assert.assertEquals("initial " + initI, recMessage.getText());
                  }
               }
               if (transacted) {
                  session.commit();
               }
               Wait.assertEquals(200L, serverQueue::getMessageCount);
            }

            try {
               TextMessage message = session.createTextMessage("hello world " + i);
               message.setIntProperty("i", i);
               producer.send(message);
               if (i % 100 == 0) {
                  logger.info("sent " + i);
               }
               if (transacted) {
                  if (i % 100 == 0 && i > 0) {
                     session.commit();
                  }
               }
            } catch (Exception e) {
               logger.warn(e.getMessage(), e);
               Assert.fail("Exception happened at " + i);
            }
         }
         if (transacted) {
            session.commit();
         }

         try (AssertionLoggerHandler loggerHandler = new AssertionLoggerHandler()) {
            producer.send(session.createTextMessage("should not complete"));
            if (transacted) {
               session.commit();
            }
            if (!drop) {
               Assert.fail("an Exception was expected");
            }
            Assert.assertTrue(loggerHandler.findText("AMQ224120"));
         } catch (JMSException e) {
            logger.debug("Expected exception, ok!", e);
         }


         Assert.assertTrue(serverQueue.getPagingStore().isPaging());

         MessageConsumer consumer = session.createConsumer(queue);
         for (int i = 0; i < 150; i++) { // we will consume half of the messages
            TextMessage message = (TextMessage) consumer.receive(5000);
            Assert.assertNotNull(message);
            Assert.assertEquals("hello world " + i, message.getText());
            Assert.assertEquals(i, message.getIntProperty("i"));
            if (transacted) {
               if (i % 100 == 0 && i > 0) {
                  session.commit();
               }
            }
         }
         if (transacted) {
            session.commit();
         }
         Future<Boolean> cleanupDone = serverQueue.getPagingStore().getCursorProvider().scheduleCleanup();

         Assert.assertTrue(cleanupDone.get(30, TimeUnit.SECONDS));



         for (int i = pageLimitMessages; i < pageLimitMessages + 150; i++) {
            try {
               TextMessage message = session.createTextMessage("hello world " + i);
               message.setIntProperty("i", i);
               producer.send(message);
               if (i % 100 == 0) {
                  logger.info("sent " + i);
               }
               if (transacted) {
                  if (i % 10 == 0 && i > 0) {
                     session.commit();
                  }
               }
            } catch (Exception e) {
               logger.warn(e.getMessage(), e);
               Assert.fail("Exception happened at " + i);
            }
         }
         if (transacted) {
            session.commit();
         }


         try (AssertionLoggerHandler loggerHandler = new AssertionLoggerHandler()) {
            producer.send(session.createTextMessage("should not complete"));
            if (transacted) {
               session.commit();
            }
            if (!drop) {
               Assert.fail("an Exception was expected");
            } else {
               Assert.assertFalse(loggerHandler.findText("AMQ224120"));
            }
         } catch (JMSException e) {
            logger.debug("Expected exception, ok!", e);
         }

         for (int i = 150; i < pageLimitMessages + 150; i++) { // we will consume half of the messages
            TextMessage message = (TextMessage) consumer.receive(5000);
            Assert.assertNotNull(message);
            Assert.assertEquals("hello world " + i, message.getText());
            Assert.assertEquals(i, message.getIntProperty("i"));
            if (transacted) {
               if (i % 100 == 0 && i > 0) {
                  session.commit();
               }
            }
         }

         Assert.assertNull(consumer.receiveNoWait());
      }

   }


   @Test
   public void testPageLimitBytesAMQP() throws Exception {
      testPageLimitBytes("AMQP");
   }

   @Test
   public void testPageLimitBytesCore() throws Exception {
      testPageLimitBytes("CORE");
   }

   @Test
   public void testPageLimitBytesOpenWire() throws Exception {
      testPageLimitBytes("OPENWIRE");
   }

   public void testPageLimitBytes(String protocol) throws Exception {

      String queueNameTX = getName() + "_TX";
      String queueNameNonTX = getName() + "_NONTX";

      Configuration config = createDefaultConfig(true);
      config.setJournalSyncTransactional(false).setJournalSyncTransactional(false);

      final int PAGE_MAX = 20 * 1024;

      final int PAGE_SIZE = 10 * 1024;

      server = createServer(true, config, PAGE_SIZE, PAGE_MAX, -1, -1, (long)(PAGE_MAX * 10), null, "FAIL", null);
      server.start();

      server.addAddressInfo(new AddressInfo(queueNameTX).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameTX).setRoutingType(RoutingType.ANYCAST));
      server.addAddressInfo(new AddressInfo(queueNameNonTX).addRoutingType(RoutingType.ANYCAST));
      server.createQueue(new QueueConfiguration(queueNameNonTX).setRoutingType(RoutingType.ANYCAST));

      Wait.assertTrue(() -> server.locateQueue(queueNameNonTX) != null);
      Wait.assertTrue(() -> server.locateQueue(queueNameTX) != null);

      testPageLimitBytesFailInternal(queueNameTX, protocol, true);
      testPageLimitBytesFailInternal(queueNameNonTX, protocol, false);

   }



   private void testPageLimitBytesFailInternal(String queueName,
                                                 String protocol,
                                                 boolean transacted) throws Exception {
      org.apache.activemq.artemis.core.server.Queue serverQueue = server.locateQueue(queueName);
      Assert.assertNotNull(serverQueue);

      ConnectionFactory factory = CFUtil.createConnectionFactory(protocol, "tcp://localhost:61616");
      try (Connection connection = factory.createConnection()) {
         Session session = connection.createSession(transacted, transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
         Queue queue = session.createQueue(queueName);
         MessageProducer producer = session.createProducer(queue);
         connection.start();

         int successfullSends = 0;
         boolean failed = false;

         for (int i = 0; i < 1000; i++) {
            try {
               TextMessage message = session.createTextMessage("hello world " + i);
               message.setIntProperty("i", i);
               producer.send(message);
               if (transacted) {
                  session.commit();
               }
            } catch (Exception e) {
               logger.debug(e.getMessage(), e);
               failed = true;
               break;
            }
            successfullSends++;
         }

         Wait.assertEquals(successfullSends, serverQueue::getMessageCount);
         Assert.assertTrue(failed);

         int reads = successfullSends / 2;

         connection.start();
         try (MessageConsumer consumer = session.createConsumer(queue)) {
            for (int i = 0; i < reads; i++) { // we will consume half of the messages
               TextMessage message = (TextMessage) consumer.receive(5000);
               Assert.assertNotNull(message);
               Assert.assertEquals("hello world " + i, message.getText());
               Assert.assertEquals(i, message.getIntProperty("i"));
               if (transacted) {
                  if (i % 100 == 0 && i > 0) {
                     session.commit();
                  }
               }
            }
            if (transacted) {
               session.commit();
            }
         }

         failed = false;

         int originalSuccess = successfullSends;

         Future<Boolean> result = serverQueue.getPagingStore().getCursorProvider().scheduleCleanup();
         Assert.assertTrue(result.get(10, TimeUnit.SECONDS));

         for (int i = successfullSends; i < 1000; i++) {
            try {
               TextMessage message = session.createTextMessage("hello world " + i);
               message.setIntProperty("i", i);
               producer.send(message);
               if (transacted) {
                  session.commit();
               }
            } catch (Exception e) {
               logger.debug(e.getMessage(), e);
               failed = true;
               break;
            }
            successfullSends++;
         }

         Assert.assertTrue(failed);
         Assert.assertTrue(successfullSends > originalSuccess);

         try (MessageConsumer consumer = session.createConsumer(queue)) {
            for (int i = reads; i < successfullSends; i++) {
               TextMessage message = (TextMessage) consumer.receive(5000);
               Assert.assertNotNull(message);
               Assert.assertEquals("hello world " + i, message.getText());
               Assert.assertEquals(i, message.getIntProperty("i"));
               if (transacted) {
                  if (i % 100 == 0 && i > 0) {
                     session.commit();
                  }
               }
            }
            if (transacted) {
               session.commit();
            }
            Assert.assertNull(consumer.receiveNoWait());
         }


      }

   }



}