package com.taobao.top.link;

public abstract class Connection {
	public abstract void send(byte[] data, int offset, int length);
}
