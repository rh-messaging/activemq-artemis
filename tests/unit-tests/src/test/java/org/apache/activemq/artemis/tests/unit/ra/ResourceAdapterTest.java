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
package org.apache.activemq.artemis.tests.unit.ra;

import javax.jms.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.UDPBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQDestination;
import org.apache.activemq.artemis.ra.ActiveMQRAManagedConnectionFactory;
import org.apache.activemq.artemis.ra.ActiveMQResourceAdapter;
import org.apache.activemq.artemis.ra.ConnectionFactoryProperties;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivation;
import org.apache.activemq.artemis.ra.inflow.ActiveMQActivationSpec;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ResourceAdapterTest extends ActiveMQTestBase {

   @Test
   public void testDefaultConnectionFactory() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ActiveMQConnectionFactory factory = ra.getDefaultActiveMQConnectionFactory();
      assertEquals(ActiveMQClient.DEFAULT_CALL_TIMEOUT, factory.getCallTimeout());
      assertEquals(ActiveMQClient.DEFAULT_CLIENT_FAILURE_CHECK_PERIOD, factory.getClientFailureCheckPeriod());
      assertNull(factory.getClientID());
      assertEquals(ActiveMQClient.DEFAULT_CONNECTION_LOAD_BALANCING_POLICY_CLASS_NAME, factory.getConnectionLoadBalancingPolicyClassName());
      assertEquals(ActiveMQClient.DEFAULT_CONNECTION_TTL, factory.getConnectionTTL());
      assertEquals(ActiveMQClient.DEFAULT_CONSUMER_MAX_RATE, factory.getConsumerMaxRate());
      assertEquals(ActiveMQClient.DEFAULT_CONSUMER_WINDOW_SIZE, factory.getConsumerWindowSize());
      assertEquals(ActiveMQClient.DEFAULT_ACK_BATCH_SIZE, factory.getDupsOKBatchSize());
      assertEquals(ActiveMQClient.DEFAULT_MIN_LARGE_MESSAGE_SIZE, factory.getMinLargeMessageSize());
      assertEquals(ActiveMQClient.DEFAULT_PRODUCER_MAX_RATE, factory.getProducerMaxRate());
      assertEquals(ActiveMQClient.DEFAULT_CONFIRMATION_WINDOW_SIZE, factory.getConfirmationWindowSize());
      // by default, reconnect attempts is set to -1
      assertEquals(-1, factory.getReconnectAttempts());
      assertEquals(ActiveMQClient.DEFAULT_RETRY_INTERVAL, factory.getRetryInterval());
      assertEquals(ActiveMQClient.DEFAULT_RETRY_INTERVAL_MULTIPLIER, factory.getRetryIntervalMultiplier(), 0.00001);
      assertEquals(ActiveMQClient.DEFAULT_SCHEDULED_THREAD_POOL_MAX_SIZE, factory.getScheduledThreadPoolMaxSize());
      assertEquals(ActiveMQClient.DEFAULT_THREAD_POOL_MAX_SIZE, factory.getThreadPoolMaxSize());
      assertEquals(ActiveMQClient.DEFAULT_ACK_BATCH_SIZE, factory.getTransactionBatchSize());
      assertEquals(ActiveMQClient.DEFAULT_AUTO_GROUP, factory.isAutoGroup());
      assertEquals(ActiveMQClient.DEFAULT_BLOCK_ON_ACKNOWLEDGE, factory.isBlockOnAcknowledge());
      assertEquals(ActiveMQClient.DEFAULT_BLOCK_ON_NON_DURABLE_SEND, factory.isBlockOnNonDurableSend());
      assertEquals(ActiveMQClient.DEFAULT_BLOCK_ON_DURABLE_SEND, factory.isBlockOnDurableSend());
      assertEquals(ActiveMQClient.DEFAULT_PRE_ACKNOWLEDGE, factory.isPreAcknowledge());
      assertEquals(ActiveMQClient.DEFAULT_USE_GLOBAL_POOLS, factory.isUseGlobalPools());
   }

   @Test
   public void test2DefaultConnectionFactorySame() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ActiveMQConnectionFactory factory = ra.getDefaultActiveMQConnectionFactory();
      ActiveMQConnectionFactory factory2 = ra.getDefaultActiveMQConnectionFactory();
      assertEquals(factory, factory2);
   }

   @Test
   public void testCreateConnectionFactoryNoOverrides() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(new ConnectionFactoryProperties());
      assertEquals(ActiveMQClient.DEFAULT_CALL_TIMEOUT, factory.getCallTimeout());
      assertEquals(ActiveMQClient.DEFAULT_CLIENT_FAILURE_CHECK_PERIOD, factory.getClientFailureCheckPeriod());
      assertNull(factory.getClientID());
      assertEquals(ActiveMQClient.DEFAULT_CONNECTION_LOAD_BALANCING_POLICY_CLASS_NAME, factory.getConnectionLoadBalancingPolicyClassName());
      assertEquals(ActiveMQClient.DEFAULT_CONNECTION_TTL, factory.getConnectionTTL());
      assertEquals(ActiveMQClient.DEFAULT_CONSUMER_MAX_RATE, factory.getConsumerMaxRate());
      assertEquals(ActiveMQClient.DEFAULT_CONSUMER_WINDOW_SIZE, factory.getConsumerWindowSize());
      assertEquals(ActiveMQClient.DEFAULT_ACK_BATCH_SIZE, factory.getDupsOKBatchSize());
      assertEquals(ActiveMQClient.DEFAULT_MIN_LARGE_MESSAGE_SIZE, factory.getMinLargeMessageSize());
      assertEquals(ActiveMQClient.DEFAULT_PRODUCER_MAX_RATE, factory.getProducerMaxRate());
      assertEquals(ActiveMQClient.DEFAULT_CONFIRMATION_WINDOW_SIZE, factory.getConfirmationWindowSize());
      // by default, reconnect attempts is set to -1
      assertEquals(-1, factory.getReconnectAttempts());
      assertEquals(ActiveMQClient.DEFAULT_RETRY_INTERVAL, factory.getRetryInterval());
      assertEquals(ActiveMQClient.DEFAULT_RETRY_INTERVAL_MULTIPLIER, factory.getRetryIntervalMultiplier(), 0.000001);
      assertEquals(ActiveMQClient.DEFAULT_SCHEDULED_THREAD_POOL_MAX_SIZE, factory.getScheduledThreadPoolMaxSize());
      assertEquals(ActiveMQClient.DEFAULT_THREAD_POOL_MAX_SIZE, factory.getThreadPoolMaxSize());
      assertEquals(ActiveMQClient.DEFAULT_ACK_BATCH_SIZE, factory.getTransactionBatchSize());
      assertEquals(ActiveMQClient.DEFAULT_AUTO_GROUP, factory.isAutoGroup());
      assertEquals(ActiveMQClient.DEFAULT_BLOCK_ON_ACKNOWLEDGE, factory.isBlockOnAcknowledge());
      assertEquals(ActiveMQClient.DEFAULT_BLOCK_ON_NON_DURABLE_SEND, factory.isBlockOnNonDurableSend());
      assertEquals(ActiveMQClient.DEFAULT_BLOCK_ON_DURABLE_SEND, factory.isBlockOnDurableSend());
      assertEquals(ActiveMQClient.DEFAULT_PRE_ACKNOWLEDGE, factory.isPreAcknowledge());
      assertEquals(ActiveMQClient.DEFAULT_USE_GLOBAL_POOLS, factory.isUseGlobalPools());
   }

   @Test
   public void testDefaultConnectionFactoryOverrides() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ra.setAutoGroup(!ActiveMQClient.DEFAULT_AUTO_GROUP);
      ra.setBlockOnAcknowledge(!ActiveMQClient.DEFAULT_BLOCK_ON_ACKNOWLEDGE);
      ra.setBlockOnNonDurableSend(!ActiveMQClient.DEFAULT_BLOCK_ON_NON_DURABLE_SEND);
      ra.setBlockOnDurableSend(!ActiveMQClient.DEFAULT_BLOCK_ON_DURABLE_SEND);
      ra.setCallTimeout(1L);
      ra.setClientFailureCheckPeriod(2L);
      ra.setClientID("myid");
      ra.setConnectionLoadBalancingPolicyClassName("mlbcn");
      ra.setConnectionTTL(3L);
      ra.setConsumerMaxRate(4);
      ra.setConsumerWindowSize(5);
      ra.setDiscoveryInitialWaitTimeout(6L);
      ra.setDiscoveryRefreshTimeout(7L);
      ra.setDupsOKBatchSize(8);
      ra.setMinLargeMessageSize(10);
      ra.setPreAcknowledge(!ActiveMQClient.DEFAULT_PRE_ACKNOWLEDGE);
      ra.setProducerMaxRate(11);
      ra.setConfirmationWindowSize(12);
      ra.setReconnectAttempts(13);
      ra.setRetryInterval(14L);
      ra.setRetryIntervalMultiplier(15d);
      ra.setScheduledThreadPoolMaxSize(16);
      ra.setThreadPoolMaxSize(17);
      ra.setTransactionBatchSize(18);
      ra.setUseGlobalPools(!ActiveMQClient.DEFAULT_USE_GLOBAL_POOLS);
      ActiveMQConnectionFactory factory = ra.getDefaultActiveMQConnectionFactory();
      assertEquals(1, factory.getCallTimeout());
      assertEquals(2, factory.getClientFailureCheckPeriod());
      assertEquals("myid", factory.getClientID());
      assertEquals("mlbcn", factory.getConnectionLoadBalancingPolicyClassName());
      assertEquals(3, factory.getConnectionTTL());
      assertEquals(4, factory.getConsumerMaxRate());
      assertEquals(5, factory.getConsumerWindowSize());
      assertEquals(8, factory.getDupsOKBatchSize());
      assertEquals(10, factory.getMinLargeMessageSize());
      assertEquals(11, factory.getProducerMaxRate());
      assertEquals(12, factory.getConfirmationWindowSize());
      assertEquals(13, factory.getReconnectAttempts());
      assertEquals(14, factory.getRetryInterval());
      assertEquals(15d, factory.getRetryIntervalMultiplier(), 0.00001);
      assertEquals(16, factory.getScheduledThreadPoolMaxSize());
      assertEquals(17, factory.getThreadPoolMaxSize());
      assertEquals(18, factory.getTransactionBatchSize());
      assertEquals(!ActiveMQClient.DEFAULT_AUTO_GROUP, factory.isAutoGroup());
      assertEquals(!ActiveMQClient.DEFAULT_BLOCK_ON_ACKNOWLEDGE, factory.isBlockOnAcknowledge());
      assertEquals(!ActiveMQClient.DEFAULT_BLOCK_ON_NON_DURABLE_SEND, factory.isBlockOnNonDurableSend());
      assertEquals(!ActiveMQClient.DEFAULT_BLOCK_ON_DURABLE_SEND, factory.isBlockOnDurableSend());
      assertEquals(!ActiveMQClient.DEFAULT_PRE_ACKNOWLEDGE, factory.isPreAcknowledge());
      assertEquals(!ActiveMQClient.DEFAULT_USE_GLOBAL_POOLS, factory.isUseGlobalPools());
   }

   @Test
   public void testCreateConnectionFactoryOverrides() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ConnectionFactoryProperties connectionFactoryProperties = new ConnectionFactoryProperties();
      connectionFactoryProperties.setAutoGroup(!ActiveMQClient.DEFAULT_AUTO_GROUP);
      connectionFactoryProperties.setBlockOnAcknowledge(!ActiveMQClient.DEFAULT_BLOCK_ON_ACKNOWLEDGE);
      connectionFactoryProperties.setBlockOnNonDurableSend(!ActiveMQClient.DEFAULT_BLOCK_ON_NON_DURABLE_SEND);
      connectionFactoryProperties.setBlockOnDurableSend(!ActiveMQClient.DEFAULT_BLOCK_ON_DURABLE_SEND);
      connectionFactoryProperties.setCallTimeout(1L);
      connectionFactoryProperties.setClientFailureCheckPeriod(2L);
      connectionFactoryProperties.setClientID("myid");
      connectionFactoryProperties.setConnectionLoadBalancingPolicyClassName("mlbcn");
      connectionFactoryProperties.setConnectionTTL(3L);
      connectionFactoryProperties.setConsumerMaxRate(4);
      connectionFactoryProperties.setConsumerWindowSize(5);
      connectionFactoryProperties.setDiscoveryInitialWaitTimeout(6L);
      connectionFactoryProperties.setDiscoveryRefreshTimeout(7L);
      connectionFactoryProperties.setDupsOKBatchSize(8);
      connectionFactoryProperties.setMinLargeMessageSize(10);
      connectionFactoryProperties.setPreAcknowledge(!ActiveMQClient.DEFAULT_PRE_ACKNOWLEDGE);
      connectionFactoryProperties.setProducerMaxRate(11);
      connectionFactoryProperties.setConfirmationWindowSize(12);
      connectionFactoryProperties.setReconnectAttempts(13);
      connectionFactoryProperties.setRetryInterval(14L);
      connectionFactoryProperties.setRetryIntervalMultiplier(15d);
      connectionFactoryProperties.setScheduledThreadPoolMaxSize(16);
      connectionFactoryProperties.setThreadPoolMaxSize(17);
      connectionFactoryProperties.setTransactionBatchSize(18);
      connectionFactoryProperties.setUseGlobalPools(!ActiveMQClient.DEFAULT_USE_GLOBAL_POOLS);
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(connectionFactoryProperties);
      assertEquals(1, factory.getCallTimeout());
      assertEquals(2, factory.getClientFailureCheckPeriod());
      assertEquals("myid", factory.getClientID());
      assertEquals("mlbcn", factory.getConnectionLoadBalancingPolicyClassName());
      assertEquals(3, factory.getConnectionTTL());
      assertEquals(4, factory.getConsumerMaxRate());
      assertEquals(5, factory.getConsumerWindowSize());
      assertEquals(8, factory.getDupsOKBatchSize());
      assertEquals(10, factory.getMinLargeMessageSize());
      assertEquals(11, factory.getProducerMaxRate());
      assertEquals(12, factory.getConfirmationWindowSize());
      assertEquals(13, factory.getReconnectAttempts());
      assertEquals(14, factory.getRetryInterval());
      assertEquals(15d, factory.getRetryIntervalMultiplier(), 0.000001);
      assertEquals(16, factory.getScheduledThreadPoolMaxSize());
      assertEquals(17, factory.getThreadPoolMaxSize());
      assertEquals(18, factory.getTransactionBatchSize());
      assertEquals(!ActiveMQClient.DEFAULT_AUTO_GROUP, factory.isAutoGroup());
      assertEquals(!ActiveMQClient.DEFAULT_BLOCK_ON_ACKNOWLEDGE, factory.isBlockOnAcknowledge());
      assertEquals(!ActiveMQClient.DEFAULT_BLOCK_ON_NON_DURABLE_SEND, factory.isBlockOnNonDurableSend());
      assertEquals(!ActiveMQClient.DEFAULT_BLOCK_ON_DURABLE_SEND, factory.isBlockOnDurableSend());
      assertEquals(!ActiveMQClient.DEFAULT_PRE_ACKNOWLEDGE, factory.isPreAcknowledge());
      assertEquals(!ActiveMQClient.DEFAULT_USE_GLOBAL_POOLS, factory.isUseGlobalPools());
   }

   @Test
   public void testCreateConnectionFactoryOverrideConnector() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ConnectionFactoryProperties connectionFactoryProperties = new ConnectionFactoryProperties();
      List<String> value = new ArrayList<>();
      value.add(NettyConnectorFactory.class.getName());
      connectionFactoryProperties.setParsedConnectorClassNames(value);
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(connectionFactoryProperties);
      ActiveMQConnectionFactory defaultFactory = ra.getDefaultActiveMQConnectionFactory();
      assertNotSame(factory, defaultFactory);
   }

   @Test
   public void testCreateConnectionFactoryOverrideDiscovery() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(InVMConnectorFactory.class.getName());
      ConnectionFactoryProperties connectionFactoryProperties = new ConnectionFactoryProperties();
      connectionFactoryProperties.setDiscoveryAddress("myhost");
      connectionFactoryProperties.setDiscoveryPort(5678);
      connectionFactoryProperties.setDiscoveryLocalBindAddress("newAddress");
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(connectionFactoryProperties);
      ActiveMQConnectionFactory defaultFactory = ra.getDefaultActiveMQConnectionFactory();
      assertNotSame(factory, defaultFactory);
      DiscoveryGroupConfiguration dc = factory.getServerLocator().getDiscoveryGroupConfiguration();
      UDPBroadcastEndpointFactory udpDg = (UDPBroadcastEndpointFactory) dc.getBroadcastEndpointFactory();
      assertEquals("newAddress", udpDg.getLocalBindAddress());
      assertEquals("myhost", udpDg.getGroupAddress());
      assertEquals(5678, udpDg.getGroupPort());
   }

   @Test
   public void testCreateConnectionFactoryMultipleConnectors() {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(NETTY_CONNECTOR_FACTORY + "," + INVM_CONNECTOR_FACTORY + "," + NETTY_CONNECTOR_FACTORY);
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(new ConnectionFactoryProperties());
      TransportConfiguration[] configurations = factory.getServerLocator().getStaticTransportConfigurations();
      assertNotNull(configurations);
      assertEquals(3, configurations.length);
      assertEquals(NETTY_CONNECTOR_FACTORY, configurations[0].getFactoryClassName());
      assertEquals(2, configurations[0].getParams().size());
      assertEquals(INVM_CONNECTOR_FACTORY, configurations[1].getFactoryClassName());
      assertEquals(1, configurations[1].getParams().size());
      assertEquals(NETTY_CONNECTOR_FACTORY, configurations[2].getFactoryClassName());
      assertEquals(2, configurations[2].getParams().size());
   }

   @Test
   public void testCreateConnectionFactoryMultipleConnectorsAndParams() {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(NETTY_CONNECTOR_FACTORY + "," + INVM_CONNECTOR_FACTORY + "," + NETTY_CONNECTOR_FACTORY);
      ra.setConnectionParameters("host=host1;port=61616, serverid=0, host=host2;port=61617");
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(new ConnectionFactoryProperties());
      TransportConfiguration[] configurations = factory.getServerLocator().getStaticTransportConfigurations();
      assertNotNull(configurations);
      assertEquals(3, configurations.length);
      assertEquals(NETTY_CONNECTOR_FACTORY, configurations[0].getFactoryClassName());
      assertEquals(2, configurations[0].getParams().size());
      assertEquals("host1", configurations[0].getParams().get("host"));
      assertEquals("61616", configurations[0].getParams().get("port"));
      assertEquals(INVM_CONNECTOR_FACTORY, configurations[1].getFactoryClassName());
      assertEquals(1, configurations[1].getParams().size());
      assertEquals("0", configurations[1].getParams().get("serverid"));
      assertEquals(NETTY_CONNECTOR_FACTORY, configurations[2].getFactoryClassName());
      assertEquals(2, configurations[2].getParams().size());
      assertEquals("host2", configurations[2].getParams().get("host"));
      assertEquals("61617", configurations[2].getParams().get("port"));
   }

   @Test
   public void testCreateConnectionFactoryMultipleConnectorsOverride() {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(NETTY_CONNECTOR_FACTORY + "," + INVM_CONNECTOR_FACTORY + "," + NETTY_CONNECTOR_FACTORY);
      ConnectionFactoryProperties overrideProperties = new ConnectionFactoryProperties();
      List<String> value = new ArrayList<>();
      value.add(INVM_CONNECTOR_FACTORY);
      value.add(NETTY_CONNECTOR_FACTORY);
      value.add(INVM_CONNECTOR_FACTORY);
      overrideProperties.setParsedConnectorClassNames(value);
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(overrideProperties);
      TransportConfiguration[] configurations = factory.getServerLocator().getStaticTransportConfigurations();
      assertNotNull(configurations);
      assertEquals(3, configurations.length);
      assertEquals(INVM_CONNECTOR_FACTORY, configurations[0].getFactoryClassName());
      assertEquals(1, configurations[0].getParams().size());
      assertEquals(NETTY_CONNECTOR_FACTORY, configurations[1].getFactoryClassName());
      assertEquals(2, configurations[1].getParams().size());
      assertEquals(INVM_CONNECTOR_FACTORY, configurations[2].getFactoryClassName());
      assertEquals(1, configurations[2].getParams().size());
   }

   @Test
   public void testCreateConnectionFactoryMultipleConnectorsOverrideAndParams() {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ra.setConnectorClassName(NETTY_CONNECTOR_FACTORY + "," + INVM_CONNECTOR_FACTORY + "," + NETTY_CONNECTOR_FACTORY);
      ra.setConnectionParameters("host=host1;port=61616, serverid=0, host=host2;port=61617");
      ConnectionFactoryProperties overrideProperties = new ConnectionFactoryProperties();
      List<String> value = new ArrayList<>();
      value.add(INVM_CONNECTOR_FACTORY);
      value.add(NETTY_CONNECTOR_FACTORY);
      value.add(INVM_CONNECTOR_FACTORY);
      overrideProperties.setParsedConnectorClassNames(value);
      List<Map<String, Object>> connectionParameters = new ArrayList<>();
      Map<String, Object> map1 = new HashMap<>();
      map1.put("serverid", "0");
      connectionParameters.add(map1);
      Map<String, Object> map2 = new HashMap<>();
      map2.put("host", "myhost");
      map2.put("port", "61616");
      connectionParameters.add(map2);
      Map<String, Object> map3 = new HashMap<>();
      map3.put("serverid", "1");
      connectionParameters.add(map3);
      overrideProperties.setParsedConnectionParameters(connectionParameters);
      ActiveMQConnectionFactory factory = ra.getConnectionFactory(overrideProperties);
      TransportConfiguration[] configurations = factory.getServerLocator().getStaticTransportConfigurations();
      assertNotNull(configurations);
      assertEquals(3, configurations.length);
      assertEquals(INVM_CONNECTOR_FACTORY, configurations[0].getFactoryClassName());
      assertEquals(1, configurations[0].getParams().size());
      assertEquals("0", configurations[0].getParams().get("serverid"));
      assertEquals(NETTY_CONNECTOR_FACTORY, configurations[1].getFactoryClassName());
      assertEquals(2, configurations[1].getParams().size());
      assertEquals("myhost", configurations[1].getParams().get("host"));
      assertEquals("61616", configurations[1].getParams().get("port"));
      assertEquals(INVM_CONNECTOR_FACTORY, configurations[2].getFactoryClassName());
      assertEquals(1, configurations[2].getParams().size());
      assertEquals("1", configurations[2].getParams().get("serverid"));
   }

   @Test
   public void testCreateConnectionFactoryThrowsException() throws Exception {
      ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
      ConnectionFactoryProperties connectionFactoryProperties = new ConnectionFactoryProperties();
      try {
         ra.getConnectionFactory(connectionFactoryProperties);
         fail("should throw exception");
      } catch (IllegalArgumentException e) {
         // pass
      }
   }

   @Test
   public void testValidateProperties() throws Exception {
      validateGettersAndSetters(new ActiveMQResourceAdapter(), "backupTransportConfiguration", "connectionParameters", "jndiParams");
      validateGettersAndSetters(new ActiveMQRAManagedConnectionFactory(), "connectionParameters", "sessionDefaultType", "backupConnectionParameters", "jndiParams");
      validateGettersAndSetters(new ActiveMQActivationSpec(), "connectionParameters", "acknowledgeMode", "subscriptionDurability", "jndiParams", "maxSession");

      ActiveMQActivationSpec spec = new ActiveMQActivationSpec();

      spec.setAcknowledgeMode("DUPS_OK_ACKNOWLEDGE");
      assertEquals("Dups-ok-acknowledge", spec.getAcknowledgeMode());

      spec.setSubscriptionDurability("Durable");
      assertEquals("Durable", spec.getSubscriptionDurability());

      spec.setSubscriptionDurability("NonDurable");
      assertEquals("NonDurable", spec.getSubscriptionDurability());

      final int validMaxSessionValue = 110;
      spec.setMaxSession(validMaxSessionValue);
      assertEquals(validMaxSessionValue, (int) spec.getMaxSession());

      spec.setMaxSession(-3);
      assertEquals(1, (int) spec.getMaxSession());

      spec = new ActiveMQActivationSpec();
      ActiveMQResourceAdapter adapter = new ActiveMQResourceAdapter();

      adapter.setUserName("us1");
      adapter.setPassword("ps1");
      adapter.setClientID("cl1");

      spec.setResourceAdapter(adapter);

      assertEquals("us1", spec.getUser());
      assertEquals("ps1", spec.getPassword());

      spec.setUser("us2");
      spec.setPassword("ps2");
      spec.setClientID("cl2");

      assertEquals("us2", spec.getUser());
      assertEquals("ps2", spec.getPassword());
      assertEquals("cl2", spec.getClientID());

   }

   @Test
   public void testStartActivation() throws Exception {
      ActiveMQServer server = createServer(false);

      try {

         server.start();
         ServerLocator locator = createInVMNonHALocator();
         ClientSessionFactory factory = createSessionFactory(locator);
         ClientSession session = factory.createSession(false, false, false);
         ActiveMQDestination queue = (ActiveMQDestination) ActiveMQJMSClient.createQueue("test");
         session.createQueue(QueueConfiguration.of(queue.getSimpleAddress()));
         session.close();

         ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();

         ra.setConnectorClassName(INVM_CONNECTOR_FACTORY);
         ra.setUserName("userGlobal");
         ra.setPassword("passwordGlobal");
         ra.start(new BootstrapContext());

         Connection conn = ra.getDefaultActiveMQConnectionFactory().createConnection();

         conn.close();

         ActiveMQActivationSpec spec = new ActiveMQActivationSpec();

         spec.setResourceAdapter(ra);

         spec.setUseJNDI(false);

         spec.setUser("user");
         spec.setPassword("password");

         spec.setDestinationType("javax.jms.Topic");
         spec.setDestination("test");

         spec.setMinSession(10);
         spec.setMaxSession(10);

         ActiveMQActivation activation = new ActiveMQActivation(ra, new MessageEndpointFactory(), spec);

         activation.start();
         assertEquals(11, server.getConnectionCount(), "wrong connection count ");
         activation.stop();

         ra.stop();

         locator.close();

      } finally {
         server.stop();
      }
   }

   @Test
   public void testStartActivationSingleConnection() throws Exception {
      ActiveMQServer server = createServer(false);

      try {

         server.start();
         ServerLocator locator = createInVMNonHALocator();
         ClientSessionFactory factory = createSessionFactory(locator);
         ClientSession session = factory.createSession(false, false, false);
         ActiveMQDestination queue = (ActiveMQDestination) ActiveMQJMSClient.createQueue("test");
         session.createQueue(QueueConfiguration.of(queue.getSimpleAddress()));
         session.close();

         ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();

         ra.setConnectorClassName(INVM_CONNECTOR_FACTORY);
         ra.setUserName("userGlobal");
         ra.setPassword("passwordGlobal");
         ra.start(new BootstrapContext());

         Connection conn = ra.getDefaultActiveMQConnectionFactory().createConnection();

         conn.close();

         ActiveMQActivationSpec spec = new ActiveMQActivationSpec();

         spec.setResourceAdapter(ra);

         spec.setUseJNDI(false);

         spec.setUser("user");
         spec.setPassword("password");

         spec.setDestinationType("javax.jms.Topic");
         spec.setDestination("test");

         spec.setMinSession(1);
         spec.setMaxSession(10);
         spec.setSingleConnection(true);

         ActiveMQActivation activation = new ActiveMQActivation(ra, new MessageEndpointFactory(), spec);

         activation.start();
         assertEquals(2, server.getConnectionCount(), "wrong connection count ");
         activation.stop();

         ra.stop();

         locator.close();

      } finally {
         server.stop();
      }
   }

   @Test
   public void testDeprecatedActivationDeserializationParameters() throws Exception {
      ActiveMQServer server = createServer(false);

      try {

         server.start();

         ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();

         ra.setConnectorClassName(INVM_CONNECTOR_FACTORY);
         ra.setUserName("userGlobal");
         ra.setPassword("passwordGlobal");
         ra.setDeserializationWhiteList("a.b.c.d.e");
         ra.setDeserializationBlackList("f.g.h.i.j");
         ra.start(new BootstrapContext());

         ActiveMQConnectionFactory factory = ra.getDefaultActiveMQConnectionFactory();
         assertEquals("a.b.c.d.e", factory.getDeserializationWhiteList());
         assertEquals("f.g.h.i.j", factory.getDeserializationBlackList());

         ConnectionFactoryProperties overrides = new ConnectionFactoryProperties();
         overrides.setDeserializationWhiteList("k.l.m.n");
         overrides.setDeserializationBlackList("o.p.q.r");

         factory = ra.newConnectionFactory(overrides);
         assertEquals("k.l.m.n", factory.getDeserializationWhiteList());
         assertEquals("o.p.q.r", factory.getDeserializationBlackList());

         ra.stop();

      } finally {
         server.stop();
      }
   }

   @Test
   public void testActivationDeserializationParameters() throws Exception {
      ActiveMQServer server = createServer(false);

      try {

         server.start();

         ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();

         ra.setConnectorClassName(INVM_CONNECTOR_FACTORY);
         ra.setUserName("userGlobal");
         ra.setPassword("passwordGlobal");
         ra.setDeserializationAllowList("a.b.c.d.e");
         ra.setDeserializationDenyList("f.g.h.i.j");
         ra.start(new BootstrapContext());

         ActiveMQConnectionFactory factory = ra.getDefaultActiveMQConnectionFactory();
         assertEquals("a.b.c.d.e", factory.getDeserializationAllowList());
         assertEquals("f.g.h.i.j", factory.getDeserializationDenyList());

         ConnectionFactoryProperties overrides = new ConnectionFactoryProperties();
         overrides.setDeserializationAllowList("k.l.m.n");
         overrides.setDeserializationDenyList("o.p.q.r");

         factory = ra.newConnectionFactory(overrides);
         assertEquals("k.l.m.n", factory.getDeserializationAllowList());
         assertEquals("o.p.q.r", factory.getDeserializationDenyList());

         ra.stop();

      } finally {
         server.stop();
      }
   }

   @Test
   public void testForConnectionLeakDuringActivationWhenSessionCreationFails() throws Exception {
      ActiveMQServer server = createServer(false);
      ActiveMQResourceAdapter ra = null;
      ActiveMQActivation activation = null;

      try {
         server.getConfiguration().setSecurityEnabled(true);
         server.start();

         ra = new ActiveMQResourceAdapter();

         ra.setConnectorClassName(INVM_CONNECTOR_FACTORY);
         ra.setUserName("badUser");
         ra.setPassword("badPassword");
         ra.start(new BootstrapContext());

         ActiveMQActivationSpec spec = new ActiveMQActivationSpec();

         spec.setResourceAdapter(ra);

         spec.setUseJNDI(false);

         spec.setUser("user");
         spec.setPassword("password");

         spec.setDestinationType("javax.jms.Topic");
         spec.setDestination("test");

         spec.setMinSession(1);
         spec.setMaxSession(1);
         spec.setSetupAttempts(1);

         activation = new ActiveMQActivation(ra, new MessageEndpointFactory(), spec);

         try {
            activation.start();
         } catch (Exception e) {
            // ignore
         }

         assertEquals(0, server.getRemotingService().getConnections().size());
      } finally {
         if (activation != null)
            activation.stop();
         if (ra != null)
            ra.stop();
         server.stop();
      }
   }
}
