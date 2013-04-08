top-link
========

[![Build Status](https://travis-ci.org/wsky/top-link.png?branch=master)](https://travis-ci.org/wsky/top-link)

design draft: embedded duplex multi-channel endpoint and connection management for c#/java/...

https://gist.github.com/4680940

## Build

set external repo first
```
git submodule init
git submodule update
git submodule foreach git pull
```

Java
```bash
cd java
mvn package
```

or

```shell
build_java.ps1
```

custom package:
```shell
cd java
mvn -DgroupId=yourDomain -DartifactId=yourId -DfinalName=jarName clean package
```

C#
```c#
cd csharp
build.bat
```

## Endpoint

https://docs.google.com/drawings/d/1PRfzMVNGE4NKkpD9A_-QlH2PV47MFumZX8LbCwhzpQg/edit

run as receiver
```java
WebSocketServerChannel serverChannel = new WebSocketServerChannel("localhost", 8080);
Endpoint endpoint = new Endpoint();
endpoint.setChannelHandler(new ChannelHandler() {
	@Override
	public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
		String dataString = new String(data, offset, length);
		context.reply("ok".getBytes(), 0, 2);
	}
});
//can bind to multi-channels
endpoint.bind(serverChannel);
//know about connect in/out endpoints
endpoint.getConnected();
```

run as sender
```java
Endpoint endpoint = new Endpoint();
try {
	EndpointProxy target = endpoint.getEndpoint(new URI("ws://localhost:8080/link"));
	// message base send
	target.send("Hi".getBytes(), 0, 2);
} catch (ChannelException e) {
	e.printStackTrace();
}
```

use identity
see https://github.com/wsky/top-link/tree/master/java/src/test/java/com/taobao/top/link/IdentityTest.java
```java

```

- More
	- [X] Identity support
	- [ ] sync send/receive
	- [ ] tcp channel

## Build-in RPC

- Low-Level implementation to support application extension.

Server Bind
```java
URI uri = new URI("ws://localhost/");
WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
Endpoint server = new Endpoint();
server.setChannelHandler(new DefaultRemotingServerChannelHandler());
server.bind(serverChannel);
```

//Send
```java
//ByteBuffer resultBuffer = RemotingService.connect(uri).send("hi".getBytes(), 0, 2);
//assertEquals("ok", new String(new byte[] { resultBuffer.get(), resultBuffer.get() }));
//hold
```

invoke
```java
MethodReturn methodReturn = RemotingService.connect(uri).invoke(new MethodCall());
if(methodReturn.Exception != null)
	throw methodReturn.Exception;
return methodReturn.ReturnValue;
```

- High-Level Abstract Remoting
	- [X] Dynamic Proxy for Java Interface
	- [X] IOC support at server/client, spring
	- [ ] Extendable sink design, like custom FormatterSink/TransportSink
	- [ ] ServiceFramework

```java
RemotingConfiguration.configure().
	websocket(uri.getPort()).
	addProcessor("sample", new SampleService());

SampleService sampleService = (SampleService) RemotingService.connect("ws://localhost/sample", SampleService.class);
assertEquals("hi", sampleService.echo("hi"));
```

#### spring-support:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
<beans>
	<bean name="testService" class="TestService" />
	<bean class="com.taobao.top.link.remoting.ServiceBean">
		<property name="interfaceName" value="TestInterface" />
		<property name="target">
			<ref bean="testService" />
		</property>
	</bean>
</beans>
```

```java
ListableBeanFactory beanFactory;//get from current context or whatever
RemotingConfiguration.
	configure().
	websocket(8889).
	addProcessor("api", new SpringMethodCallProcessor(beanFactory));

```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
<beans>
	<bean name="test" class="com.taobao.top.link.remoting.SpringServiceProxyBean">
		<property name="interfaceName" value="TestInterface" />
		<property name="uri" value="ws://localhost:8889/api" />
	</bean>
</beans>
```

```java
TestInterface testInterface = (TestInterface) beanFactory.getBean("test");
assertEquals("hi", testInterface.echo("hi"));
```

## License

- Netty, Apache License Version 2.0

	https://github.com/netty/netty

	https://github.com/netty/netty/blob/master/LICENSE.txt

- RemotingProtocolParser, MIT License

	https://github.com/wsky/RemotingProtocolParser

	https://github.com/wsky/RemotingProtocolParser/blob/master/README.md#license

