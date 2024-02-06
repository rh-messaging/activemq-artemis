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
 import { HawtioPlugin, configManager } from '@hawtio/react'
import { artemis } from './artemis'

/**
 * The entry function for the plugin exposed to Hawtio.
 *
 * The default name for the function is "plugin". If you want to use the name other
 * than the default one, you need to specify the name using {HawtioPlugin#pluginEntry()}
 * method when registering the plugin to JMX MBean server.
 *
 * <code>
 * new HawtioPlugin()
 *     .pluginEntry("registerMyPlugin");
 * </code>
 *
 * @see src/main/java/io/hawt/artemis/PluginContextListener.java
 */
export const plugin: HawtioPlugin = () => {
  artemis()
}


// Register the custom plugin version to Hawtio
// See package.json "replace-version" script for how to replace the version placeholder with a real version
configManager.addProductInfo('Artemis Plugin', '__PACKAGE_VERSION_PLACEHOLDER__')

// Branding and styles can be customised from a plugin as follows
/*eslint no-template-curly-in-string: "off"*/
configManager.configure(config => {
  config.branding = {
    appName: 'AMQ7 Console',
    showAppName: false,
    appLogoUrl: '/artemis-plugin/branding/Logo-RedHat-A-Reverse-RGB.png',
    css: '/artemis-plugin/branding/app.css',
    favicon: '/artemis-plugin/branding/favicon.ico',
  }
  config.login = {
    description: 'Login page for AMQ7 Console.'
  }
  config.about = {
    title: 'AMQ7 Broker Management Console',
    description: '',
    productInfo: [
      { name: 'AMQ', value: '${amq.broker.version}' },
      { name: 'Artemis', value: '${project.version}' },
      { name: 'HawtIO', value: '${hawtio.version}' },
    ],
    copyright: ' ',
    imgSrc: '/artemis-plugin/branding/Logo-RedHat-A-Reverse-RGB.png',
  }
  // If you want to disable specific plugins, you can specify the paths to disable them.
  //config.disabledRoutes = ['/simple-plugin']
})
