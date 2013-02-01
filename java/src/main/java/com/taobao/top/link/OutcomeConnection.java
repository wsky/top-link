package com.taobao.top.link;

public abstract class OutcomeConnection extends Connection {
	public abstract void send(byte[] data, int offset, int length);
}
