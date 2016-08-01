# Security

This chapter describes how security works with Apache ActiveMQ Artemis and how you can
configure it. To disable security completely simply set the
`security-enabled` property to false in the `broker.xml`
file.

For performance reasons security is cached and invalidated every so
long. To change this period set the property
`security-invalidation-interval`, which is in milliseconds. The default
is `10000` ms.

## Role based security for addresses

Apache ActiveMQ Artemis contains a flexible role-based security model for applying
security to queues, based on their addresses.

As explained in [Using Core](using-core.md), Apache ActiveMQ Artemis core consists mainly of sets of queues bound
to addresses. A message is sent to an address and the server looks up
the set of queues that are bound to that address, the server then routes
the message to those set of queues.

Apache ActiveMQ Artemis allows sets of permissions to be defined against the queues
based on their address. An exact match on the address can be used or a
wildcard match can be used using the wildcard characters '`#`' and
'`*`'.

Seven different permissions can be given to the set of queues which
match the address. Those permissions are:

-   `createDurableQueue`. This permission allows the user to create a
    durable queue under matching addresses.

-   `deleteDurableQueue`. This permission allows the user to delete a
    durable queue under matching addresses.

-   `createNonDurableQueue`. This permission allows the user to create a
    non-durable queue under matching addresses.

-   `deleteNonDurableQueue`. This permission allows the user to delete a
    non-durable queue under matching addresses.

-   `send`. This permission allows the user to send a message to
    matching addresses.

-   `consume`. This permission allows the user to consume a message from
    a queue bound to matching addresses.

-   `manage`. This permission allows the user to invoke management
    operations by sending management messages to the management address.

For each permission, a list of roles who are granted that permission is
specified. If the user has any of those roles, he/she will be granted
that permission for that set of addresses.

Let's take a simple example, here's a security block from
`broker.xml` file:

    <security-setting match="globalqueues.europe.#">
       <permission type="createDurableQueue" roles="admin"/>
       <permission type="deleteDurableQueue" roles="admin"/>
       <permission type="createNonDurableQueue" roles="admin, guest, europe-users"/>
       <permission type="deleteNonDurableQueue" roles="admin, guest, europe-users"/>
       <permission type="send" roles="admin, europe-users"/>
       <permission type="consume" roles="admin, europe-users"/>
    </security-setting>

The '`#`' character signifies "any sequence of words". Words are
delimited by the '`.`' character. For a full description of the wildcard
syntax please see [Understanding the Wildcard Syntax](wildcard-syntax.md).
The above security block applies to any address
that starts with the string "globalqueues.europe.":

Only users who have the `admin` role can create or delete durable queues
bound to an address that starts with the string "globalqueues.europe."

Any users with the roles `admin`, `guest`, or `europe-users` can create
or delete temporary queues bound to an address that starts with the
string "globalqueues.europe."

Any users with the roles `admin` or `europe-users` can send messages to
these addresses or consume messages from queues bound to an address that
starts with the string "globalqueues.europe."

The mapping between a user and what roles they have is handled by the
security manager. Apache ActiveMQ Artemis ships with a user manager that reads user
credentials from a file on disk, and can also plug into JAAS or JBoss
Application Server security.

For more information on configuring the security manager, please see 'Changing the Security Manager'.

There can be zero or more `security-setting` elements in each xml file.
Where more than one match applies to a set of addresses the *more
specific* match takes precedence.

Let's look at an example of that, here's another `security-setting`
block:

    <security-setting match="globalqueues.europe.orders.#">
       <permission type="send" roles="europe-users"/>
       <permission type="consume" roles="europe-users"/>
    </security-setting>

In this `security-setting` block the match
'globalqueues.europe.orders.\#' is more specific than the previous match
'globalqueues.europe.\#'. So any addresses which match
'globalqueues.europe.orders.\#' will take their security settings *only*
from the latter security-setting block.

Note that settings are not inherited from the former block. All the
settings will be taken from the more specific matching block, so for the
address 'globalqueues.europe.orders.plastics' the only permissions that
exist are `send` and `consume` for the role europe-users. The
permissions `createDurableQueue`, `deleteDurableQueue`,
`createNonDurableQueue`, `deleteNonDurableQueue` are not inherited from
the other security-setting block.

By not inheriting permissions, it allows you to effectively deny
permissions in more specific security-setting blocks by simply not
specifying them. Otherwise it would not be possible to deny permissions
in sub-groups of addresses.

## Secure Sockets Layer (SSL) Transport

When messaging clients are connected to servers, or servers are
connected to other servers (e.g. via bridges) over an untrusted network
then Apache ActiveMQ Artemis allows that traffic to be encrypted using the Secure
Sockets Layer (SSL) transport.

For more information on configuring the SSL transport, please see [Configuring the Transport](configuring-transports.md).

## Basic user credentials

Apache ActiveMQ Artemis ships with a security manager implementation that reads user
credentials, i.e. user names, passwords and role information from properties
files on the classpath called `artemis-users.properties` and `artemis-roles.properties`. This is the default security manager.

If you wish to use this security manager, then users, passwords and
roles can easily be added into these files.

To configure this manager then it needs to be added to the `bootstrap.xml` configuration.
Lets take a look at what this might look like:

    <basic-security>
      <users>file:${activemq.home}/config/non-clustered/artemis-users.properties</users>
      <roles>file:${activemq.home}/config/non-clustered/artemis-roles.properties</roles>
      <default-user>guest</default-user>
    </basic-security>

The first 2 elements `users` and `roles` define what properties files should be used to load in the users and passwords.

The next thing to note is the element `defaultuser`. This defines what
user will be assumed when the client does not specify a
username/password when creating a session. In this case they will be the
user `guest`. Multiple roles can be specified for a default user in the
`artemis-roles.properties`.

Lets now take alook at the `artemis-users.properties` file, this is basically
just a set of key value pairs that define the users and their password, like so:

    bill=activemq
    andrew=activemq1
    frank=activemq2
    sam=activemq3

The `artemis-roles.properties` defines what groups these users belong too
where the key is the user and the value is a comma separated list of the groups
the user belongs to, like so:

    bill=user
    andrew=europe-user,user
    frank=us-user,news-user,user
    sam=news-user,user

## Changing the username/password for clustering

In order for cluster connections to work correctly, each node in the
cluster must make connections to the other nodes. The username/password
they use for this should always be changed from the installation default
to prevent a security risk.

Please see [Management](management.md) for instructions on how to do this.


## Securing the console

Artemis comes with a web console that allows user to browse Artemis documentation via an embedded server. By default the
web access is plain HTTP. It is configured in `bootstrap.xml`:

    <web bind="http://localhost:8161" path="web">
        <app url="jolokia" war="jolokia-war-1.3.3.war"/>
    </web>

Alternatively you can edit the above configuration to enable secure access using HTTPS protocol. e.g.:

    <web bind="https://localhost:8443"
        path="web"
        keyStorePath="${artemis.instance}/etc/keystore.jks"
        keyStorePassword="password">
        <app url="jolokia" war="jolokia-war-1.3.3.war"/>
    </web>

As shown in the example, to enable https the first thing to do is config the `bind` to be an `https` url. In addition,
You will have to configure a few extra properties desribed as below.

-   `keyStorePath` - The path of the key store file.

-   `keyStorePassword` - The key store's password.

-   `clientAuth` - The boolean flag indicates whether or not client authentication is required. Default is `false`.

-   `trustStorePath` - The path of the trust store file. This is needed only if `clientAuth` is `true`.

-   `trustStorePassword` - The trust store's password.

## Controlling JMS ObjectMessage deserialization

Artemis provides a simple class filtering mechanism with which a user can specify which
packages are to be trusted and which are not. Objects whose classes are from trusted packages
can be deserialized without problem, whereas those from 'not trusted' packages will be denied
deserialization.

Artemis keeps a `black list` to keep track of packages that are not trusted and a `white list`
for trusted packages. By default both lists are empty, meaning any serializable object is 
allowed to be deserialized. If an object whose class matches one of the packages in black list,
it is not allowed to be deserialized. If it matches one in the white list
the object can be deserialized. If a package appears in both black list and white list, 
the one in black list takes precedence. If a class neither matches with `black list` 
nor with the `white list`, the class deserialization will be denied 
unless the white list is empty (meaning the user doesn't specify the white list at all).

A class is considered as a 'match' if

-   its full name exactly matches one of the entries in the list.
-   its package matches one of the entries in the list or is a sub-package of one of the entries.

For example, if a class full name is "org.apache.pkg1.Class1", some matching entries could be:

-   `org.apache.pkg1.Class1` - exact match.
-   `org.apache.pkg1` - exact package match.
-   `org.apache` -- sub package match.

A `*` means 'match-all' in a black or white list.

### Specifying black list and white list via Connection Factories

To specify the white and black lists one can append properties `deserializationBlackList` and `deserializationWhiteList` respectively
to a Connection Factory's url string. For example:

     ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://0?deserializationBlackList=org.apache.pkg1,org.some.pkg2");

The above statement creates a factory that has a black list contains two forbidden packages, "org.apache.pkg1" and "org.some.pkg2",
separated by a comma.

You can also set the values via ActiveMQConnectionFactory's API:

    public void setDeserializationBlackList(String blackList);
    public void setDeserializationWhiteList(String whiteList);
   
Again the parameters are comma separated list of package/class names.

### Specifying black list and white list via system properties

There are two system properties available for specifying black list and white list:

-   `org.apache.activemq.artemis.jms.deserialization.whitelist` - comma separated list of entries for the white list.
-   `org.apache.activemq.artemis.jms.deserialization.blacklist` - comma separated list of entries for the black list.

Once defined, all JMS object message deserialization in the VM is subject to checks against the two lists. However if you create a ConnectionFactory
and set a new set of black/white lists on it, the new values will override the system properties.

### Specifying black list and white list for resource adapters

Message beans using a JMS resource adapter to receive messages can also control their object deserialization via properly configuring relevant
properties for their resource adapters. There are two properties that you can configure with connection factories in a resource adapter:

-   `deserializationBlackList` - comma separated values for black list
-   `deserializationWhiteList` - comma separated values for white list

These properties, once specified, are eventually set on the corresponding internal factories.








