/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.hawtio.plugin;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import io.hawt.web.plugin.HawtioPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginContextListener implements ServletContextListener {

   private static final Logger LOG = LoggerFactory.getLogger(PluginContextListener.class);

   private HawtioPlugin plugin = null;

   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
      /*
       * These are the parameters required to load a remote Hawtio plugin (a.k.a. Module Federation remote module):
       *
       * - url: The URL of the remote entry for the plugin. This must be the same location as the Hawtio console.
       * - scope: The name of the container defined at Webpack ModuleFederationPlugin. See also: artemis-plugin/craco.config.js
       * - module: The path exposed from Webpack ModuleFederationPlugin. See also: artemis-plugin/craco.config.js
       */
      plugin = new HawtioPlugin()
              .scope("artemisPlugin")
              .module("./plugin")
              .url("/artemis-plugin");

      /*
       * By default, Hawtio expects "plugin" as the name of the Hawtio plugin entry function.
       * If you want to use the name other than the default one, specify the name using HawtioPlugin#pluginEntry()
       * as follows. See also: artemis-plugin/src/artemis-plugin/index.ts
       */
      //plugin.pluginEntry("registerMyPlugin");

      plugin.init();

      LOG.info("Initialised plugin: {}", plugin.getScope());
   }

   @Override
   public void contextDestroyed(ServletContextEvent servletContextEvent) {
      plugin.destroy();
      LOG.info("Destroyed plugin: {}", plugin.getScope());
   }
}
