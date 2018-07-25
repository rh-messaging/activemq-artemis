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

package org.apache.activemq.artemis.tests.integration.replication;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.client.impl.ServerLocatorImpl;
import org.apache.activemq.artemis.core.config.ClusterConnectionConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.ha.ReplicaPolicyConfiguration;
import org.apache.activemq.artemis.core.config.ha.ReplicatedPolicyConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.io.IOCriticalErrorListener;
import org.apache.activemq.artemis.core.io.SequentialFile;
import org.apache.activemq.artemis.core.io.SequentialFileFactory;
import org.apache.activemq.artemis.core.io.nio.NIOSequentialFile;
import org.apache.activemq.artemis.core.io.nio.NIOSequentialFileFactory;
import org.apache.activemq.artemis.core.paging.PagingManager;
import org.apache.activemq.artemis.core.paging.PagingStore;
import org.apache.activemq.artemis.core.paging.impl.PagingManagerImpl;
import org.apache.activemq.artemis.core.paging.impl.PagingStoreFactoryNIO;
import org.apache.activemq.artemis.core.persistence.StorageManager;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.server.JournalType;
import org.apache.activemq.artemis.core.server.impl.ActiveMQServerImpl;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.apache.activemq.artemis.tests.util.Wait;
import org.apache.activemq.artemis.utils.ExecutorFactory;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SharedNothingReplicationFlowControlTest extends ActiveMQTestBase {

   ExecutorService sendMessageExecutor;

   @Before
   public void setupExecutor() {
      sendMessageExecutor = Executors.newCachedThreadPool();
   }

   @After
   public void teardownExecutor() {
      sendMessageExecutor.shutdownNow();
   }

   private static final Logger logger = Logger.getLogger(SharedNothingReplicationFlowControlTest.class);

   @Rule
   public TemporaryFolder brokersFolder = new TemporaryFolder();

   @Test
   public void testSendPages() throws Exception {
      // start live
      Configuration liveConfiguration = createLiveConfiguration();
      ActiveMQServer liveServer = addServer(ActiveMQServers.newActiveMQServer(liveConfiguration));
      liveServer.start();

      Wait.waitFor(() -> liveServer.isStarted());

      ServerLocator locator = ServerLocatorImpl.newLocator("tcp://localhost:61616");
      locator.setCallTimeout(60_000L);
      locator.setConnectionTTL(60_000L);

      final ClientSessionFactory csf = locator.createSessionFactory();
      ClientSession sess = csf.createSession();
      sess.createQueue("flowcontrol", "flowcontrol", true);

      PagingStore store = liveServer.getPagingManager().getPageStore(SimpleString.toSimpleString("flowcontrol"));
      store.startPaging();

      ClientProducer prod = sess.createProducer("flowcontrol");
      for (int i = 0; i < 100; i++) {
         prod.send(sess.createMessage(true));

         if (i % 10 == 0) {
            sess.commit();
            store.forceAnotherPage();
         }
      }

      sess.close();

      openCount.set(0);
      closeCount.set(0);
      // start backup
      Configuration backupConfiguration = createBackupConfiguration().setNetworkCheckURLList(null);

      ActiveMQServer backupServer = new ActiveMQServerImpl(backupConfiguration, ManagementFactory.getPlatformMBeanServer(), new ActiveMQJAASSecurityManager(InVMLoginModule.class.getName(), new SecurityConfiguration())) {
         @Override
         public PagingManager createPagingManager() throws Exception {
            PagingManagerImpl manager = (PagingManagerImpl) super.createPagingManager();
            PagingStoreFactoryNIO originalPageStore = (PagingStoreFactoryNIO) manager.getPagingStoreFactory();
            manager.replacePageStoreFactory(new PageStoreFactoryTestable(originalPageStore));
            return manager;
         }
      };

      addServer(backupServer).start();

      Wait.waitFor(() -> backupServer.isStarted());

      Wait.waitFor(backupServer::isReplicaSync, 30000);

      PageStoreFactoryTestable testablePageStoreFactory = (PageStoreFactoryTestable) ((PagingManagerImpl) backupServer.getPagingManager()).getPagingStoreFactory();

      Assert.assertEquals(openCount.get(), closeCount.get());
   }

   static AtomicInteger openCount = new AtomicInteger(0);
   static AtomicInteger closeCount = new AtomicInteger(0);

   private static class PageStoreFactoryTestable extends PagingStoreFactoryNIO {

      PageStoreFactoryTestable(StorageManager storageManager,
                                      File directory,
                                      long syncTimeout,
                                      ScheduledExecutorService scheduledExecutor,
                                      ExecutorFactory executorFactory,
                                      boolean syncNonTransactional,
                                      IOCriticalErrorListener critialErrorListener) {
         super(storageManager, directory, syncTimeout, scheduledExecutor, executorFactory, syncNonTransactional, critialErrorListener);
      }

      PageStoreFactoryTestable(PagingStoreFactoryNIO other) {
         this(other.getStorageManager(), other.getDirectory(), other.getSyncTimeout(), other.getScheduledExecutor(), other.getExecutorFactory(), other.isSyncNonTransactional(), other.getCritialErrorListener());
      }

      @Override
      protected SequentialFileFactory newFileFactory(String directoryName) {
         return new TestableNIOFactory(new File(getDirectory(), directoryName), false, getCritialErrorListener(), 1);
      }
   }

   public static class TestableNIOFactory extends NIOSequentialFileFactory {

      public TestableNIOFactory(File journalDir, boolean buffered, IOCriticalErrorListener listener, int maxIO) {
         super(journalDir, buffered, listener, maxIO);
      }

      @Override
      public SequentialFile createSequentialFile(String fileName) {
         return new TestableSequentialFile(this, journalDir, fileName, maxIO, writeExecutor);
      }
   }

   public static class TestableSequentialFile extends NIOSequentialFile {

      public TestableSequentialFile(SequentialFileFactory factory,
                                    File directory,
                                    String file,
                                    int maxIO,
                                    Executor writerExecutor) {
         super(factory, directory, file, maxIO, writerExecutor);
      }

      @Override
      public void open(int maxIO, boolean useExecutor) throws IOException {
         super.open(maxIO, useExecutor);
         openCount.incrementAndGet();
      }

      @Override
      public synchronized void close() throws IOException, InterruptedException, ActiveMQException {
         super.close();
         closeCount.incrementAndGet();
      }
   }

   // Set a small call timeout and write buffer high water mark value to trigger replication flow control
   private Configuration createLiveConfiguration() throws Exception {
      Configuration conf = new ConfigurationImpl();
      conf.setName("localhost::live");

      File liveDir = brokersFolder.newFolder("live");
      conf.setBrokerInstance(liveDir);

      conf.addAcceptorConfiguration("live", "tcp://localhost:61616?writeBufferHighWaterMark=2048&writeBufferLowWaterMark=2048");
      conf.addConnectorConfiguration("backup", "tcp://localhost:61617");
      conf.addConnectorConfiguration("live", "tcp://localhost:61616");

      conf.setClusterUser("mycluster");
      conf.setClusterPassword("mypassword");

      ReplicatedPolicyConfiguration haPolicy = new ReplicatedPolicyConfiguration();
      haPolicy.setCheckForLiveServer(false);
      conf.setHAPolicyConfiguration(haPolicy);

      ClusterConnectionConfiguration ccconf = new ClusterConnectionConfiguration();
      ccconf.setStaticConnectors(new ArrayList<>()).getStaticConnectors().add("backup");
      ccconf.setName("cluster");
      ccconf.setConnectorName("live");
      ccconf.setCallTimeout(4000);
      conf.addClusterConfiguration(ccconf);

      conf.setSecurityEnabled(false).setJMXManagementEnabled(false).setJournalType(JournalType.NIO).setJournalFileSize(1024 * 512).setConnectionTTLOverride(60_000L);

      return conf;
   }

   private Configuration createBackupConfiguration() throws Exception {
      Configuration conf = new ConfigurationImpl();
      conf.setName("localhost::backup");

      File backupDir = brokersFolder.newFolder("backup");
      conf.setBrokerInstance(backupDir);

      ReplicaPolicyConfiguration haPolicy = new ReplicaPolicyConfiguration();
      haPolicy.setClusterName("cluster");
      conf.setHAPolicyConfiguration(haPolicy);

      conf.addAcceptorConfiguration("backup", "tcp://localhost:61617");
      conf.addConnectorConfiguration("live", "tcp://localhost:61616");
      conf.addConnectorConfiguration("backup", "tcp://localhost:61617");

      conf.setClusterUser("mycluster");
      conf.setClusterPassword("mypassword");

      ClusterConnectionConfiguration ccconf = new ClusterConnectionConfiguration();
      ccconf.setStaticConnectors(new ArrayList<>()).getStaticConnectors().add("live");
      ccconf.setName("cluster");
      ccconf.setConnectorName("backup");
      conf.addClusterConfiguration(ccconf);

      /**
       * Set a bad url then, as a result the backup node would make a decision
       * of replicating from live node in the case of connection failure.
       * Set big check period to not schedule checking which would stop server.
       */
      conf.setNetworkCheckPeriod(1000000).setNetworkCheckURLList("http://localhost:28787").setSecurityEnabled(false).setJMXManagementEnabled(false).setJournalType(JournalType.NIO).setJournalFileSize(1024 * 512).setConnectionTTLOverride(60_000L);

      return conf;
   }
}
