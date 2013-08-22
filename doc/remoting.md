# Remoting

Top-link.remoting was Build-in RPC abstraction.


## Getting Started

### Low-Level 

Low-Level implementation to support application extension.

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
	
### Cross-Language support

Serialization detail: https://github.com/wsky/top-link/issues/43

C# sample:
```c#
using Taobao.Top.Link.Remoting;
//via websocekt
RemotingService.Connect<TestService>("ws://localhost/").Echo("hi");
//via tcp and .net remoting extension
//upcoming...
(System.Runtime.Remoting.RemotingService.Connect("ws://localhost/") as TestService).Echo("hi");
```


### Multi-Transport support

- [X] WebSocket
- [X] TCP
- [ ] HTTP

### High-Level Abstract Remoting

- [X] Dynamic Proxy for Java Interface
- [X] IOC support at server/client, spring
- [ ] Extendable sink design, like custom FormatterSink/TransportSink
- [ ] ServiceFramework (coming soon)
	- IOC support
	- ServiceRegisger

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
	<bean name="server" class="com.taobao.top.link.remoting.SpringServerBean">
		<property name="port" value="8889" />
		<property name="path" value="api" />
		<property name="maxMessageSize" value="1024" />
		<property name="minBusinessThreadCount" value="20" />
		<property name="maxBusinessThreadCount" value="200" />
	</bean>
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
beanFactory.getBean("server");
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">
<beans>
	<bean name="test" class="com.taobao.top.link.remoting.SpringServiceProxyBean">
		<property name="interfaceName" value="TestInterface" />
		<property name="uri" value="ws://localhost:8889/" />
		<property name="executionTimeout" value="5000" />
		<property name="serialization" value="json" />
	</bean>
</beans>
```

```java
TestInterface testInterface = (TestInterface) beanFactory.getBean("test");
assertEquals("hi", testInterface.echo("hi"));
```

More settings sample see here: https://github.com/wsky/top-link/blob/master/java/spring-support/test/com/taobao/top/link/remoting/SpringTest.java
