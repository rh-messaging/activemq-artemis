# The Artemis Hawtio  WAR Plugin

This Artemis HawtIO plugin is written using [Hawtio v4](https://github.com/hawtio/hawtio).
The plugin is  written in TypeScript. Since a Hawtio plugin is based on React and [Webpack Module Federation](https://module-federation.github.io/), 
this project uses Yarn v3 and [CRACO](https://craco.js.org/) as the build tools. 

## How to run

Altho the plugin is built and deployed via the embedded artemis web server, it is possible for development purposes to run it standalone
### Build

The following command first builds the `artemis-plugin` frontend project and then compiles and packages the main Java project together.

```console
mvn clean install
```

Building the frontend project can take time, so if you build it once and make no changes on the project afterwards, you can speed up the whole build by skipping the frontend part next time.

```console
mvn install -Dskip.yarn
```

### Test run

You can quickly run and test the console by using `jetty-maven-plugin` configured in `pom.xml`. It launches an embedded Jetty server and deploy the plugin WAR application, as well as the main `hawtio.war`.

```console
mvn jetty:run -Dskip.yarn
```

You can access the Artemis console with the sample plugin at: <http://localhost:8080/console/>

## Faster plugin development

You could run `mvn install` or `mvn jetty:run` every time to incrementally develop the `artemis-plugin` frontend project while checking its behaviour in the browser. But this is not suitable for running the fast development feedback cycle.

As shown below, a faster development cycle can be achieved by directly running the `artemis-plugin` frontend project itself in development mode with `yarn start`, 

### Development
Start the plugin project in development mode:

```console
cd artemis-plugin
yarn start
```

Now you should be able to preview the plugins under development at <http://localhost:3001/hawtio/>. However, since it still hasn't been connected to a backend JVM, You can then connect to a running Artemis instance using the connect tab using for instance http://localhost:8161/console/jolokia.
You can now edit the artemis console web application and see changes loaded live.


