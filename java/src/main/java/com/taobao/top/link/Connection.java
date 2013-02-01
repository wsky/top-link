package com.taobao.top.link;

//TCPConnection is declared in network framework, not link layer
@Deprecated
public abstract class Connection {
	public abstract void send(byte[] data, int offset, int length);
}
