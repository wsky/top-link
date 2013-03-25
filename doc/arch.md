# Architecture

changes:
	- issues#22
	

## Modules
	
### Channel

Managing in/out connections, thin layer and simple shield specific io-framewirk dependency.

- ChannelSender: abstract, used to send message whatever in or out connection.

- ServerChannel: bind, accept income connection
	- WebSocket
	- TCP
	- HTTP

- ClientChannel: build on out connection, implement ChannelSender
	
- ChannelSelector: hold ClientChannels
	- Shared
	- Pooled

- ChannelHandler:
	- onConnect(ChannelContext context)
	- onMessage(ChannelContext context)
	- onError(ChannelContext context)

- ChannelContext: passed when ChannelHandler's events raised, easy to get something about event
	- getMessage
	- getError
	- reply

### Endpoint

Application exposure in a node on the network. They can talk with each other.

https://docs.google.com/drawings/d/1PRfzMVNGE4NKkpD9A_-QlH2PV47MFumZX8LbCwhzpQg/edit?usp=sharing

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

### Remoting

Just a RPC framework build on Channel, easy and efficient.

- Low-Level

- High-Level
