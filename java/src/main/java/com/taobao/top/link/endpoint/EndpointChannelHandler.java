package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ChannelSender;
import com.taobao.top.link.channel.ChannelSender.SendHandler;

// make timing
public class EndpointChannelHandler implements ChannelHandler {
	private Logger logger;
	private Endpoint endpoint;
	private AtomicInteger flag;
	private HashMap<String, SendCallback> callbackByFlag;
	// all connect in/out endpoints
	private HashMap<String, Identity> idByToken;

	public EndpointChannelHandler(LoggerFactory loggerFactory, Endpoint endpoint) {
		this.logger = loggerFactory.create(this);
		this.flag = new AtomicInteger();
		this.callbackByFlag = new HashMap<String, SendCallback>();
		this.idByToken = new HashMap<String, Identity>();
		this.endpoint = endpoint;
	}

	public void pending(Message msg, ChannelSender sender) throws ChannelException {
		this.pending(msg, sender, null);
	}

	// all send in Endpoint module must call here
	public void pending(Message msg, ChannelSender sender, SendCallback callback) throws ChannelException {
		if (callback != null)
			this.callbackByFlag.put(Integer.toString(this.flag.incrementAndGet()), callback);
		final ByteBuffer buffer = BufferManager.getBuffer();
		MessageIO.writeMessage(buffer, msg);
		sender.send(buffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(buffer);
			}
		});
		// TODO:dump sent message
	}

	@Override
	public void onConnect(ChannelContext context) {
	}

	@Override
	public void onMessage(ChannelContext context) throws Exception {
		Message msg = MessageIO.readMessage((ByteBuffer) context.getMessage());

		// TODO:dump received message

		if (msg.messageType == MessageType.CONNECT) {
			this.handleConnect(context, msg);
			return;
		}

		SendCallback callback = this.callbackByFlag.remove(msg.flag);

		if (msg.messageType == MessageType.CONNECTACK) {
			this.handleConnectAck(callback, msg);
			return;
		}

		Identity msgFrom = this.idByToken.get(msg.token);
		// must CONNECT/CONNECTACK for got token before SEND
		if (msgFrom == null)
			throw new LinkException("uknown message from");

		if (callback != null) {
			if (callback.getTarget().getIdentity().equals(msgFrom))
				callback.setReturn(msg.content);
			return;
		}

		// raise onMessage for async receive mode
		if (this.endpoint.getMessageHandler() == null)
			return;
		EndpointContext endpointContext = 
				new EndpointContext(context, this.endpoint, msgFrom, msg.token);
		endpointContext.setMessage(msg.content);
		this.endpoint.getMessageHandler().onMessage(endpointContext);
	}

	@Override
	public void onError(ChannelContext context) throws Exception {
		this.logger.error("channel error", context.getError());
	}

	// deal with connect-in message from endpoint,
	// parse identity send from endpoint and assign it a token,
	// token just used for routing message-from, not auth
	private void handleConnect(ChannelContext context, Message message) throws ChannelException {
		Message ack = new Message();
		ack.messageType = MessageType.CONNECTACK;
		try {
			Identity id = this.endpoint.getIdentity().parse(message.content);
			EndpointProxy proxy = this.endpoint.getEndpoint(id);
			proxy.add(context.getSender());
			// uuid for token? or get from id?
			String token = UUID.randomUUID().toString();
			proxy.setToken(token);
			this.idByToken.put(token, id);
			this.logger.info("accept a connect-in endpoint#%s and assign token#%s", id, token);
		} catch (LinkException e) {
			ack.statusCode = e.getErrorCode();
			ack.statusPhase = e.getMessage();
			this.logger.warn("refuse a connect-in endpoint", e);
		}
		final ByteBuffer buffer = BufferManager.getBuffer();
		MessageIO.writeMessage(buffer, ack);
		context.reply(buffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(buffer);
			}
		});
	}

	private void handleConnectAck(SendCallback callback, Message msg) throws LinkException {
		if (callback == null)
			throw new LinkException("receive CONNECTACK, but no callback to handle it");
		if (msg.statusCode > 0)
			callback.setError(new LinkException(msg.statusCode, msg.statusPhase));
		else {
			callback.setComplete();
			// set token for proxy for sending message next time
			callback.getTarget().setToken(msg.token);
			// store token from target endpoint for receiving it's message
			// next time
			this.idByToken.put(msg.token, callback.getTarget().getIdentity());
			this.logger.info("sucessfully connect to endpoint#%s, and got token#%s",
					callback.getTarget().getIdentity(),
					msg.token);
		}
	}
}
