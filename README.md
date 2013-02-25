top-link
========

design draft: embedded duplex multi-channel endpoint and connection management for c#/java/...

https://gist.github.com/4680940

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