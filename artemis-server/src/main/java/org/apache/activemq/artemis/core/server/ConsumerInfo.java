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
package org.apache.activemq.artemis.core.server;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;

public interface ConsumerInfo {

   /** an unique sequential ID for this consumer */
   long getSequentialID();

   /** @return name of the queue that is being consumed */
   SimpleString getQueueName();

   /** @return routing type of the queue that is being consumed */
   RoutingType getQueueType();

   /** @return address of the queue that is being consumed */
   SimpleString getQueueAddress();

   SimpleString getFilterString();

   String getSessionName();

   String getConnectionClientID();

   /**
    * Returns the name of the protocol for this Remoting Connection
    * @return the name of protocol
    */
   String getConnectionProtocolName();

   /**
    * Returns a string representation of the local address this connection is
    * connected to. This is useful when the server is configured at 0.0.0.0 (or
    * multiple IPs). This will give you the actual IP that's being used.
    *
    * @return the local address
    */
   String getConnectionLocalAddress();

   /**
    * Returns a string representation of the remote address this connection is
    * connected to.
    *
    * @return the remote address
    */
   String getConnectionRemoteAddress();

   /**
    * Returns how many messages are out for delivery but not yet acknowledged
    * @return delivering count
    */
   int getMessagesInTransit();

   /**
    * Returns the combined size of all the messages out for delivery but not yet acknowledged
    * @return the total size of all the messages
    */
   long getMessagesInTransitSize();

   /**
    * Returns The total number of messages sent to a consumer including redeliveries that have been acknowledged
    * @return the total number of messages delivered.
    */
   long getMessagesDelivered();

   /**
    * Returns the total size of all the messages delivered to the consumer. This includes redelivered messages
    * @return The total size of all the messages
    */
   long getMessagesDeliveredSize();

   /**
    * Returns the number of messages acknowledged by this consumer since it was created
    * @return messages acknowledged
    */
   long getMessagesAcknowledged();

   /**
    * Returns the number of acknowledged messages that are awaiting commit in a transaction
    * @return th eno acknowledgements awaiting commit
    */
   int getMessagesAcknowledgedAwaitingCommit();

   /**
    * Returns the time in milliseconds that the last message was delivered to a consumer
    * @return the time of the last message delivered
    */
   long getLastDeliveredTime();

   /**
    * Returns the time in milliseconds that the last message was acknowledged by a consumer
    * @return the time of the last message was acknowledged
    */
   long getLastAcknowledgedTime();
}