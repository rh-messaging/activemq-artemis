/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.tests.integration.amqp;

import java.lang.invoke.MethodHandles;

import org.apache.activemq.artemis.core.server.ServerSession;
import org.apache.activemq.artemis.core.server.impl.ServerSessionImpl;
import org.apache.activemq.artemis.tests.util.Wait;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpReceiver;
import org.apache.activemq.transport.amqp.client.AmqpSender;
import org.apache.activemq.transport.amqp.client.AmqpSession;
import org.apache.activemq.transport.amqp.client.AmqpValidator;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmqpSessionTest extends AmqpClientTestSupport {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   @Test(timeout = 60000)
   public void testCreateSession() throws Exception {
      AmqpClient client = createAmqpClient();
      AmqpConnection connection = addConnection(client.connect());
      AmqpSession session = connection.createSession();
      assertNotNull(session);
      connection.close();
   }

   @Test(timeout = 60000)
   public void testSessionClosedDoesNotGetReceiverDetachFromRemote() throws Exception {
      AmqpClient client = createAmqpClient();
      assertNotNull(client);

      client.setValidator(new AmqpValidator() {

         @Override
         public void inspectClosedResource(Session session) {
            logger.debug("Session closed: {}", session.getContext());
         }

         @Override
         public void inspectDetachedResource(Receiver receiver) {
            markAsInvalid("Broker should not detach receiver linked to closed session.");
         }

         @Override
         public void inspectClosedResource(Receiver receiver) {
            markAsInvalid("Broker should not close receiver linked to closed session.");
         }
      });

      AmqpConnection connection = addConnection(client.connect());
      assertNotNull(connection);
      AmqpSession session = connection.createSession();
      assertNotNull(session);
      AmqpReceiver receiver = session.createReceiver(getQueueName());
      assertNotNull(receiver);

      session.close();

      connection.getStateInspector().assertValid();
      connection.close();
   }

   @Test(timeout = 60000)
   public void testCreateSessionProducerConsumerDoesNotLeakClosable() throws Exception {
      AmqpClient client = createAmqpClient();
      AmqpConnection connection = addConnection(client.connect());
      AmqpSession session = connection.createSession();
      assertNotNull(session);

      for (int i = 0; i < 10; i++) {
         AmqpReceiver receiver = session.createReceiver(getQueueName());
         AmqpSender sender = session.createSender(getQueueName());
         receiver.close();
         sender.close();
      }

      assertEquals(1, server.getSessions().size());
      for (ServerSession serverSession : server.getSessions()) {
         Assert.assertNull( ((ServerSessionImpl) serverSession).getCloseables());
      }

      connection.close();
   }

   @Test
   public void testSessionClosedOnServerEndsClientSession() throws Exception {
      doTestSessionClosedOnServerEndsClientSession(false, false);
   }

   @Test
   public void testSessionClosedOnServerEndsClientSessionWithFailed() throws Exception {
      doTestSessionClosedOnServerEndsClientSession(true, false);
   }

   @Test
   public void testSessionClosedOnServerEndsClientSessionWithFailedAndForced() throws Exception {
      doTestSessionClosedOnServerEndsClientSession(true, true);
   }

   @Test
   public void testSessionClosedOnServerEndsClientSessionForced() throws Exception {
      doTestSessionClosedOnServerEndsClientSession(false, true);
   }

   public void doTestSessionClosedOnServerEndsClientSession(boolean failed, boolean forced) throws Exception {
      final AmqpClient client = createAmqpClient();
      final AmqpConnection connection = addConnection(client.connect());
      final AmqpSession session = connection.createSession();

      assertNotNull(session);
      assertEquals(1, server.getSessions().size());

      final ServerSession serverSession = server.getSessions().iterator().next();

      assertNotNull(serverSession);

      serverSession.close(failed, forced); // Should trigger End frame.

      Wait.assertEquals(0, () -> server.getSessions().size(), 5000, 100);

      try {
         session.createReceiver(getQueueName());
         fail("Should not be able to use this session now.");
      } catch (Exception e) {
         // Expected.
      }

      connection.close();
   }
}
