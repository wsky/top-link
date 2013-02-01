package com.taobao.top.link;

import com.taobao.top.link.handler.ReceiveHandler;

public class EndpointProxy {

	private Identity identity;
	private Connection connection;

	protected void using(Connection connection) {
		this.connection = connection;
	}

	public Identity getIdentity() {
		return this.identity;
	}

	public void send(byte[] data, int offset, int length) {
		this.connection.send(data, offset, length);
	}

	public void send(byte[] data, int offset, int length, ReceiveHandler handler) {

	}
}
