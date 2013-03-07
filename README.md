top-link
========

design draft: embedded duplex multi-channel endpoint and connection management for c#/java/...

https://gist.github.com/4680940

[![Build Status](https://travis-ci.org/wsky/top-link.png?branch=master)](https://travis-ci.org/wsky/top-link)

## Build

set external repo first
```
git submodule init
git submodule update
git submodule foreach git pull
```

Java
```java
cd java
mvn package
```

C#
```c#
cd csharp
build.bat
```

## Endpoint

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
endpoint.bind(serverChannel);
```

```java
Endpoint endpoint = new Endpoint();
try {
	EndpointProxy target = endpoint.getEndpoint(new URI("ws://localhost:8080/link"));
	target.send("Hi".getBytes(), 0, 2);
} catch (ChannelException e) {
	e.printStackTrace();
}
```

sync call sample
```java
Endpoint endpoint = new Endpoint();
try {
	EndpointProxy target = endpoint.getEndpoint(new URI("ws://localhost:8080/link"));
	assertEquals("ok", new String(target.call("Hi".getBytes(), 0, 2)));
} catch (ChannelException e) {
	e.printStackTrace();
}
```

## Build-in RPC

Low-Level implementation to support application extension.

Server Bind
```java
URI uri = new URI("ws://localhost:9001/link");
WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
Endpoint server = new Endpoint();
server.setChannelHandler(new RemotingServerChannelHandler() {
	@Override
	public byte[] onRequest(ByteBuffer buffer) {
		return "ok".getBytes();
	}
});
server.bind(serverChannel);
```

Send
```java
ByteBuffer resultBuffer = RemotingService.connect(uri).send("hi".getBytes(), 0, 2);
assertEquals("ok", new String(new byte[] { resultBuffer.get(), resultBuffer.get() }));
```

Call
```java
MethodResponse ret = RemotingService.connect(uri).call(new MethodCall());
if(ret.Exception!=null)
	throw ret.Exception;
return ret.ReturnValue;
```

High-Level Abstract Remoting
	- IOC support at server/client
	- Extendable sink design, like custom FormatterSink/TransportSink

```java
SampleService sampleService = (SampleService) RemotingService.connect(uri, SampleService.class);
assertEquals("hi", sampleService.echo("hi"));
```

## License

- Netty, Apache License Version 2.0

	https://github.com/netty/netty

	https://github.com/netty/netty/blob/master/LICENSE.txt

- RemotingProtocolParser, MIT License

	https://github.com/wsky/RemotingProtocolParser

	https://github.com/wsky/RemotingProtocolParser/blob/master/README.md#license

