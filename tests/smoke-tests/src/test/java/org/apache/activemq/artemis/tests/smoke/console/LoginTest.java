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
package org.apache.activemq.artemis.tests.smoke.console;

import org.apache.activemq.artemis.tests.smoke.console.pages.LoginPage;
import org.apache.activemq.artemis.utils.RetryRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.MutableCapabilities;

import static org.apache.activemq.artemis.tests.smoke.console.PageConstants.DEFAULT_CONSOLE_LOGIN_BRAND_IMAGE;
import static org.apache.activemq.artemis.tests.smoke.console.PageConstants.WEB_URL_PATH;
import static org.junit.Assert.assertEquals;


public class LoginTest extends ConsoleTest {

   @Rule
   public RetryRule retryRule = new RetryRule(2);

   private static final String DEFAULT_CONSOLE_LOGIN_BRAND_IMAGE = "/activemq-branding/plugin/img/activemq.png";

   public LoginTest(MutableCapabilities browserOptions) {
      super(browserOptions);
   }

   @Test
   public void testLogin() {
      driver.get(webServerUrl + WEB_URL_PATH);
      LoginPage loginPage = new LoginPage(driver);
      loginPage.loginValidUser(SERVER_ADMIN_USERNAME, SERVER_ADMIN_PASSWORD, DEFAULT_TIMEOUT);
   }

   @Test
   public void testLoginBrand() {
      String expectedBrandImage = webServerUrl + System.getProperty(
            "artemis.console.login.brand.image", DEFAULT_CONSOLE_LOGIN_BRAND_IMAGE);

      driver.get(webServerUrl + WEB_URL_PATH);
      LoginPage loginPage = new LoginPage(driver);
      assertEquals(expectedBrandImage, loginPage.getBrandImage(DEFAULT_TIMEOUT));
   }
}
