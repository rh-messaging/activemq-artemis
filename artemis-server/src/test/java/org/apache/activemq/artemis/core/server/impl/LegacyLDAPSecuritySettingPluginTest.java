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
package org.apache.activemq.artemis.core.server.impl;

import java.util.Map;

import org.apache.activemq.artemis.utils.RandomUtil;
import org.junit.Test;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LegacyLDAPSecuritySettingPluginTest {

   @Test
   public void testBlankParameterMap() throws Exception {
      new LegacyLDAPSecuritySettingPlugin().init(Map.of());
   }

   @Test
   public void testFullParameterMap() throws Exception {
      final String initialContextFactoryValue = RandomUtil.randomString();
      final String connectionURLValue = RandomUtil.randomString();
      final String connectionUsernameValue = RandomUtil.randomString();
      final String connectionPasswordValue = RandomUtil.randomString();
      final String connectionProtocolValue = RandomUtil.randomString();
      final String authenticationValue = RandomUtil.randomString();
      final String roleAttributeValue = RandomUtil.randomString();
      final String filterValue = RandomUtil.randomString();
      final String destinationBaseValue = RandomUtil.randomString();
      final String adminPermissionValueValue = RandomUtil.randomString();
      final String readPermissionValueValue = RandomUtil.randomString();
      final String writePermissionValueValue = RandomUtil.randomString();
      final String enableListenerValue = String.valueOf(RandomUtil.randomBoolean());
      final String refreshIntervalValue = String.valueOf(RandomUtil.randomInt());
      final String mapAdminToManageValue = String.valueOf(RandomUtil.randomBoolean());
      final String allowQueueAdminOnReadValue = String.valueOf(RandomUtil.randomBoolean());
      final String anyWordsWildcardConversionValue = String.valueOf(RandomUtil.randomChar());
      final String singleWordWildcardConversionValue = String.valueOf(RandomUtil.randomChar());
      final String delimiterWildcardConversionValue = String.valueOf(RandomUtil.randomChar());

      LegacyLDAPSecuritySettingPlugin plugin = new LegacyLDAPSecuritySettingPlugin().init(Map.ofEntries(
         entry("initialContextFactory", initialContextFactoryValue),
         entry("connectionURL", connectionURLValue),
         entry("connectionUsername", connectionUsernameValue),
         entry("connectionPassword", connectionPasswordValue),
         entry("connectionProtocol", connectionProtocolValue),
         entry("authentication", authenticationValue),
         entry("roleAttribute", roleAttributeValue),
         entry("filter", filterValue),
         entry("destinationBase", destinationBaseValue),
         entry("adminPermissionValue", adminPermissionValueValue),
         entry("readPermissionValue", readPermissionValueValue),
         entry("writePermissionValue", writePermissionValueValue),
         entry("enableListener", enableListenerValue),
         entry("refreshInterval", refreshIntervalValue),
         entry("mapAdminToManage", mapAdminToManageValue),
         entry("allowQueueAdminOnRead", allowQueueAdminOnReadValue),
         entry("anyWordsWildcardConversion", anyWordsWildcardConversionValue),
         entry("singleWordWildcardConversion", singleWordWildcardConversionValue),
         entry("delimiterWildcardConversion", delimiterWildcardConversionValue)
      ));
      assertEquals(initialContextFactoryValue, plugin.getInitialContextFactory());
      assertEquals(connectionURLValue, plugin.getConnectionURL());
      assertEquals(connectionUsernameValue, plugin.getConnectionUsername());
      assertEquals(connectionPasswordValue, plugin.getConnectionPassword());
      assertEquals(connectionProtocolValue, plugin.getConnectionProtocol());
      assertEquals(authenticationValue, plugin.getAuthentication());
      assertEquals(roleAttributeValue, plugin.getRoleAttribute());
      assertEquals(filterValue, plugin.getFilter());
      assertEquals(destinationBaseValue, plugin.getDestinationBase());
      assertEquals(adminPermissionValueValue, plugin.getAdminPermissionValue());
      assertEquals(readPermissionValueValue, plugin.getReadPermissionValue());
      assertEquals(writePermissionValueValue, plugin.getWritePermissionValue());
      assertEquals(enableListenerValue, String.valueOf(plugin.isEnableListener()));
      assertEquals(refreshIntervalValue, String.valueOf(plugin.getRefreshInterval()));
      assertEquals(mapAdminToManageValue, String.valueOf(plugin.isMapAdminToManage()));
      assertEquals(allowQueueAdminOnReadValue, String.valueOf(plugin.isAllowQueueAdminOnRead()));
      assertEquals(anyWordsWildcardConversionValue, String.valueOf(plugin.getAnyWordsWildcardConversion()));
      assertEquals(singleWordWildcardConversionValue, String.valueOf(plugin.getSingleWordWildcardConversion()));
      assertEquals(delimiterWildcardConversionValue, String.valueOf(plugin.getDelimiterWildcardConversion()));
   }

   @Test
   public void testLongAnyWords() throws Exception {
      assertThrows(IllegalArgumentException.class, () -> {
         new LegacyLDAPSecuritySettingPlugin().init(Map.of(LegacyLDAPSecuritySettingPlugin.ANY_WORDS_WILDCARD_CONVERSION, "xxx"));
      });
   }

   @Test
   public void testLongSingleWord() throws Exception {
      assertThrows(IllegalArgumentException.class, () -> {
         new LegacyLDAPSecuritySettingPlugin().init(Map.of(LegacyLDAPSecuritySettingPlugin.SINGLE_WORD_WILDCARD_CONVERSION, "xxx"));
      });
   }

   @Test
   public void testLongDelimiter() throws Exception {
      assertThrows(IllegalArgumentException.class, () -> {
         new LegacyLDAPSecuritySettingPlugin().init(Map.of(LegacyLDAPSecuritySettingPlugin.DELIMITER_WILDCARD_CONVERSION, "xxx"));
      });
   }
}
