package com.taobao.top.link;

public abstract class ClientChannel {
	public abstract void send(byte[] data, int offset, int length);
}
