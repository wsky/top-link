# Endpoint

Application exposure in a node on the network. They can talk(RTT based) with each other.

https://docs.google.com/drawings/d/1PRfzMVNGE4NKkpD9A_-QlH2PV47MFumZX8LbCwhzpQg/edit?usp=sharing

![stack](https://raw.github.com/wsky/top-link/master/doc/top-link-endpoint.png)


## Getting Started

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

Know about all connect in/out endpoints:

```java
public Iterator<EndpointProxy> getConnected();
```

- Identity:
	- send or receive identity message after onConnect
	- Endpoints know each other by identity
	
```java
public EndpointProxy getEndpoint(Identity identity);
```

- MessageHandler
	- onMessage

Message send/receive asynchronous	

- EndpointContext
	
	like ChannelContext, make processor easy to know about coming message and endpoint the message came from
	
- EndpointProxy: a local proxy of remote endpoint
	- hold identity of the remote endpoint
	- List<ChannelSender> senders
	- send()
