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

package org.apache.activemq.artemis.tests.smoke.console.artemisJMX;

import org.apache.activemq.artemis.tests.smoke.console.pages.artemisJMX.ArtemisTreePage;
import org.apache.activemq.artemis.tests.smoke.console.pages.LoginPage;
import org.apache.activemq.artemis.tests.smoke.console.pages.artemis.StatusPage;
import org.apache.activemq.artemis.tests.smoke.console.pages.artemisJMX.AttributesPage;
import org.apache.activemq.artemis.utils.RetryRule;
import org.apache.activemq.artemis.utils.Wait;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.MutableCapabilities;

@RunWith(Parameterized.class)
public class JMXTreeTest extends ArtemisJMXTest {

   @Rule public RetryRule retryRule = new RetryRule(2);

   public JMXTreeTest(MutableCapabilities browserOptions) {
      super(browserOptions);
   }

   @Test
   public void testExpandTree() {
      loadLandingPage();
      LoginPage loginPage = new LoginPage(driver);
      StatusPage statusPage = loginPage.loginValidUser(
            SERVER_ADMIN_USERNAME, SERVER_ADMIN_PASSWORD, DEFAULT_TIMEOUT);

      loadLandingPage();
      ArtemisTreePage treePage = new ArtemisTreePage(driver);
      treePage.expandTree(DEFAULT_TIMEOUT);

      assertEquals("Select Artemis Node", treePage.getNodeTitle());
   }

   @Test
   public void testSelectBrokerNode() throws Exception {
      loadLandingPage();
      LoginPage loginPage = new LoginPage(driver);
      StatusPage statusPage = loginPage.loginValidUser(
            SERVER_ADMIN_USERNAME, SERVER_ADMIN_PASSWORD, DEFAULT_TIMEOUT);

      loadLandingPage();
      ArtemisTreePage treePage = new ArtemisTreePage(driver);
      treePage.expandTree(DEFAULT_TIMEOUT);

      AttributesPage attributesPage = treePage.selectBrokerNode();

      Wait.assertEquals("0.0.0.0", () -> attributesPage.getAttributevalue("Name"));

      attributesPage.selectAttribute("Name");

      Wait.assertTrue(() -> attributesPage.isAttributeDisplayed("Name"));
   }
}
