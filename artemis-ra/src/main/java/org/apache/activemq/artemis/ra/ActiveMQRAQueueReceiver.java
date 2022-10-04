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
package org.apache.activemq.artemis.ra;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;

/**
 * A wrapper for a queue receiver
 */
public class ActiveMQRAQueueReceiver extends ActiveMQRAMessageConsumer implements QueueReceiver {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   /**
    * Create a new wrapper
    *
    * @param consumer the queue receiver
    * @param session  the session
    */
   public ActiveMQRAQueueReceiver(final QueueReceiver consumer, final ActiveMQRASession session) {
      super(consumer, session);

      if (logger.isTraceEnabled()) {
         logger.trace("constructor(" + consumer + ", " + session + ")");
      }
   }

   /**
    * Get queue
    *
    * @return The queue
    * @throws JMSException Thrown if an error occurs
    */
   @Override
   public Queue getQueue() throws JMSException {
      if (logger.isTraceEnabled()) {
         logger.trace("getQueue()");
      }

      checkState();
      return ((QueueReceiver) consumer).getQueue();
   }
}
