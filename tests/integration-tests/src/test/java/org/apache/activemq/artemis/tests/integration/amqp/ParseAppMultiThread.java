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
package org.apache.activemq.artemis.tests.integration.amqp;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.ActiveMQBuffers;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.protocol.amqp.broker.AMQPMessage;
import org.apache.activemq.artemis.protocol.amqp.broker.AMQPMessagePersisterV2;
import org.apache.activemq.artemis.protocol.amqp.broker.AMQPStandardMessage;
import org.apache.activemq.artemis.protocol.amqp.util.NettyWritable;
import org.apache.activemq.artemis.protocol.amqp.util.TLSEncode;
import org.apache.activemq.artemis.utils.RandomUtil;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.DeliveryAnnotations;
import org.apache.qpid.proton.amqp.messaging.Footer;
import org.apache.qpid.proton.amqp.messaging.Header;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.codec.EncoderImpl;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.junit.Assert;
import org.junit.Test;

public class ParseAppMultiThread {


   // code borrowed from AMQPStandardMessage.createMessage from upstream branch...
   public static AMQPStandardMessage AMQPStandardMessage_createMessage(long messageID,
                                                   long messageFormat,
                                                   SimpleString replyTo,
                                                   Header header,
                                                   Properties properties,
                                                   Map<Symbol, Object> daMap,
                                                   Map<Symbol, Object> maMap,
                                                   Map<String, Object> apMap,
                                                   Map<Symbol, Object> footerMap,
                                                   Section body) {
      ByteBuf buffer = PooledByteBufAllocator.DEFAULT.heapBuffer(1024);

      try {
         EncoderImpl encoder = TLSEncode.getEncoder();
         encoder.setByteBuffer(new NettyWritable(buffer));

         if (header != null) {
            encoder.writeObject(header);
         }
         if (daMap != null) {
            encoder.writeObject(new DeliveryAnnotations(daMap));
         }
         if (maMap != null) {
            encoder.writeObject(new MessageAnnotations(maMap));
         }
         if (properties != null) {
            encoder.writeObject(properties);
         }
         if (apMap != null) {
            encoder.writeObject(new ApplicationProperties(apMap));
         }
         if (body != null) {
            encoder.writeObject(body);
         }
         if (footerMap != null) {
            encoder.writeObject(new Footer(footerMap));
         }

         byte[] data = new byte[buffer.writerIndex()];
         buffer.readBytes(data);

         AMQPStandardMessage amqpMessage = new AMQPStandardMessage(messageFormat, data, null);
         amqpMessage.setMessageID(messageID);
         amqpMessage.setReplyTo(replyTo);
         return amqpMessage;

      } finally {
         TLSEncode.getEncoder().setByteBuffer((WritableBuffer) null);
         buffer.release();
      }
   }

   @Test
   public void testMultiThreadParsing() throws Exception {

      for (int rep = 0; rep < 50; rep++) {
         String randomStr = RandomUtil.randomString();
         HashMap map = new HashMap();
         map.put("color", randomStr);
         for (int i = 0; i < 10; i++) {
            map.put("stuff" + i, "value" + i); // just filling stuff
         }
         AMQPStandardMessage originalMessage = AMQPStandardMessage_createMessage(1, 0, SimpleString.toSimpleString("duh"), null, null, null, null, map, null, null);


         // doing a round trip that would be made through persistence
         AMQPMessagePersisterV2 persister = AMQPMessagePersisterV2.getInstance();

         ActiveMQBuffer buffer = ActiveMQBuffers.dynamicBuffer(1024);
         persister.encode(buffer, originalMessage);
         buffer.readerIndex(1);

         AMQPStandardMessage amqpStandardMessage = (AMQPStandardMessage) persister.decode(buffer, null, null);


         if (rep == 0) {
            // it is enough to check the first time only
            // this is to make sure the message does not have application properties parsed
            Field field = AMQPMessage.class.getDeclaredField("applicationProperties");
            field.setAccessible(true);
            Assert.assertNull(field.get(amqpStandardMessage));
         }


         Thread[] threads = new Thread[50];
         CyclicBarrier barrier = threads.length > 0 ? new CyclicBarrier(threads.length) : null;

         AtomicInteger errors = new AtomicInteger(0);

         for (int i = 0; i < threads.length; i++) {
            Runnable r = () -> {
               try {
                  barrier.await();
                  Assert.assertEquals(randomStr, amqpStandardMessage.getObjectProperty(SimpleString.toSimpleString("color")));
               } catch (Throwable e) {
                  e.printStackTrace();
                  errors.incrementAndGet();
               }
            };

            threads[i] = new Thread(r);
            threads[i].start();
         }

         for (Thread t : threads) {
            t.join();
         }

         Assert.assertEquals(randomStr, amqpStandardMessage.getObjectPropertyForFilter(SimpleString.toSimpleString("color")));
         Assert.assertEquals(0, errors.get());
      }

   }

}
