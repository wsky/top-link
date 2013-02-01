package com.taobao.top.link;

public abstract class IncomeConnection extends Connection {
	public abstract void send(byte[] data, int offset, int length);
}
