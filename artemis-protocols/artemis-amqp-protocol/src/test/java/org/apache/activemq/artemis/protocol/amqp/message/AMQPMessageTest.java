/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.artemis.protocol.amqp.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.ActiveMQBuffers;
import org.apache.activemq.artemis.api.core.ICoreMessage;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.protocol.amqp.broker.AMQPMessage;
import org.apache.activemq.artemis.protocol.amqp.broker.AMQPMessagePersisterV2;
import org.apache.activemq.artemis.protocol.amqp.util.NettyWritable;
import org.apache.activemq.artemis.spi.core.protocol.EmbedMessageUtil;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Assert;
import org.junit.Test;

public class AMQPMessageTest {

   @Test
   public void testVerySimple() {
      MessageImpl protonMessage = (MessageImpl) Message.Factory.create();
      protonMessage.setHeader( new Header());
      Properties properties = new Properties();
      properties.setTo("someNiceLocal");
      protonMessage.setProperties(properties);
      protonMessage.getHeader().setDeliveryCount(new UnsignedInteger(7));
      protonMessage.getHeader().setDurable(Boolean.TRUE);
      protonMessage.setApplicationProperties(new ApplicationProperties(new HashedMap()));

      ByteBuf nettyBuffer = Unpooled.buffer(1500);

      protonMessage.encode(new NettyWritable(nettyBuffer));

      byte[] bytes = new byte[nettyBuffer.writerIndex()];

      nettyBuffer.readBytes(bytes);

      AMQPMessage encode = new AMQPMessage(0, bytes);

      Assert.assertEquals(7, encode.getHeader().getDeliveryCount().intValue());
      Assert.assertEquals(true, encode.getHeader().getDurable());
      Assert.assertEquals("someNiceLocal", encode.getAddress());
   }

   @Test
   public void testExtraProperty() {
      MessageImpl protonMessage = (MessageImpl) Message.Factory.create();

      byte[] original = RandomUtil.randomBytes();
      SimpleString name = SimpleString.toSimpleString("myProperty");
      AMQPMessage decoded = encodeAndDecodeMessage(protonMessage);
      decoded.setAddress("someAddress");
      decoded.setMessageID(33);
      decoded.putExtraBytesProperty(name, original);

      ICoreMessage coreMessage = decoded.toCore();
      Assert.assertEquals(original, coreMessage.getBytesProperty(name));

      ActiveMQBuffer buffer = ActiveMQBuffers.pooledBuffer(10 * 1024);
      try {
         decoded.getPersister().encode(buffer, decoded);
         Assert.assertEquals(AMQPMessagePersisterV2.getInstance().getID(), buffer.readByte()); // the journal reader will read 1 byte to find the persister
         AMQPMessage readMessage = (AMQPMessage)decoded.getPersister().decode(buffer, null);
         Assert.assertEquals(33, readMessage.getMessageID());
         Assert.assertEquals("someAddress", readMessage.getAddress());
         Assert.assertArrayEquals(original, readMessage.getExtraBytesProperty(name));
      } finally {
         buffer.release();
      }

      {
         ICoreMessage embeddedMessage = EmbedMessageUtil.embedAsCoreMessage(decoded);
         AMQPMessage readMessage = (AMQPMessage) EmbedMessageUtil.extractEmbedded(embeddedMessage);
         Assert.assertEquals(33, readMessage.getMessageID());
         Assert.assertEquals("someAddress", readMessage.getAddress());
         Assert.assertArrayEquals(original, readMessage.getExtraBytesProperty(name));
      }

   }

   private AMQPMessage encodeAndDecodeMessage(MessageImpl message) {
      ByteBuf nettyBuffer = Unpooled.buffer(1500);

      message.encode(new NettyWritable(nettyBuffer));
      byte[] bytes = new byte[nettyBuffer.writerIndex()];
      nettyBuffer.readBytes(bytes);

      return new AMQPMessage(0, bytes);
   }
}
