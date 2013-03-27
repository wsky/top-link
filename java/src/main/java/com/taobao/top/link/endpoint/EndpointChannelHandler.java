package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ChannelSender.SendHandler;

// make timing
public class EndpointChannelHandler implements ChannelHandler {
	private Logger logger;
	private Endpoint endpoint;
	private AtomicInteger flag;
	private HashMap<String, SendCallback> callbacks;

	public EndpointChannelHandler(LoggerFactory loggerFactory, Endpoint endpoint) {
		this.logger = loggerFactory.create(this);
		this.flag=new AtomicInteger();
		this.endpoint = endpoint;
	}
	
	public void pending(Message msg, SendCallback callback) {
		this.callbacks.put(Integer.toString(this.flag.incrementAndGet()), callback);
	}

	@Override
	public void onConnect(ChannelContext context) {
	}

	@Override
	public void onMessage(ChannelContext context) throws Exception {
		Message msg = MessageIO.readMessage((ByteBuffer) context.getMessage());

		if (msg.messageType == MessageType.CONNECT) {
			Message ack = new Message();
			ack.messageType = MessageType.CONNECTACK;
			try {
				Identity id = this.endpoint.getIdentity().parse(msg.content);
				EndpointProxy proxy = this.endpoint.getEndpoint(id);
				proxy.add(context.getSender());
				this.logger.info("accept a connect-in channl from endpoint#" + id);
			} catch (LinkException e) {
				ack.statusCode = e.getErrorCode();
				ack.statusPhase = e.getMessage();
				this.logger.warn("refuse a connect-in channel", e);
			}
			final ByteBuffer buffer = BufferManager.getBuffer();
			MessageIO.writeMessage(buffer, ack);
			context.reply(buffer, new SendHandler() {
				@Override
				public void onSendComplete() {
					BufferManager.returnBuffer(buffer);
				}
			});
			return;
		}

		SendCallback callback = this.callbacks.remove(msg.flag);

		if (msg.messageType == MessageType.CONNECTACK) {
			if (msg.statusCode > 0)
				callback.setError(new LinkException(msg.statusCode, msg.statusPhase));
			else
				callback.setComplete();
			return;
		}

		if (callback != null) {
			callback.setReturn(msg.content);
			return;
		}

		if (this.endpoint.getMessageHandler() == null)
			return;

		EndpointContext endpointContext = new EndpointContext(context);
		endpointContext.setMessage(msg.content);
		this.endpoint.getMessageHandler().onMessage(endpointContext);
	}

	@Override
	public void onError(ChannelContext context) throws Exception {
		this.logger.error("channel error", context.getError());
	}
}
