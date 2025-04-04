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
package org.apache.activemq;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

public class JmsClientAckTest extends TestSupport {

   private Connection connection;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      connection = createConnection();
   }

   @Override
   protected void tearDown() throws Exception {
      if (connection != null) {
         connection.close();
         connection = null;
      }
      super.tearDown();
   }

   /**
    * Tests if acknowledged messages are being consumed.
    */
   public void testAckedMessageAreConsumed() throws JMSException {
      connection.start();
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Queue queue = session.createQueue(getQueueName());
      MessageProducer producer = session.createProducer(queue);
      producer.send(session.createTextMessage("Hello"));

      // Consume the message...
      MessageConsumer consumer = session.createConsumer(queue);
      Message msg = consumer.receive(1000);
      assertNotNull(msg);
      msg.acknowledge();

      // Reset the session.
      session.close();
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      // Attempt to Consume the message...
      consumer = session.createConsumer(queue);
      msg = consumer.receive(1000);
      assertNull(msg);

      session.close();
   }

   /**
    * Tests if acknowledged messages are being consumed.
    */
   public void testLastMessageAcked() throws JMSException {
      connection.start();
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Queue queue = session.createQueue(getQueueName());
      MessageProducer producer = session.createProducer(queue);
      producer.send(session.createTextMessage("Hello"));
      producer.send(session.createTextMessage("Hello2"));
      producer.send(session.createTextMessage("Hello3"));

      // Consume the message...
      MessageConsumer consumer = session.createConsumer(queue);
      Message msg = consumer.receive(1000);
      assertNotNull(msg);
      msg = consumer.receive(1000);
      assertNotNull(msg);
      msg = consumer.receive(1000);
      assertNotNull(msg);
      msg.acknowledge();

      // Reset the session.
      session.close();
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      // Attempt to Consume the message...
      consumer = session.createConsumer(queue);
      msg = consumer.receive(1000);
      assertNull(msg);

      session.close();
   }

   /**
    * Tests if unacknowledged messages are being re-delivered when the consumer connects again.
    */
   public void testUnAckedMessageAreNotConsumedOnSessionClose() throws JMSException {
      connection.start();
      Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
      Queue queue = session.createQueue(getQueueName());
      MessageProducer producer = session.createProducer(queue);
      producer.send(session.createTextMessage("Hello"));

      // Consume the message...
      MessageConsumer consumer = session.createConsumer(queue);
      Message msg = consumer.receive(1000);
      assertNotNull(msg);
      // Don't ack the message.

      // Reset the session.  This should cause the unacknowledged message to be re-delivered.
      session.close();
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

      // Attempt to Consume the message...
      consumer = session.createConsumer(queue);
      msg = consumer.receive(2000);
      assertNotNull(msg);
      msg.acknowledge();

      session.close();
   }

   protected String getQueueName() {
      return getClass().getName() + "." + getName();
   }

}
