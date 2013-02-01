package com.taobao.top.link;

public abstract class IncomeClientChannel extends ClientChannel {
	public abstract void send(byte[] data, int offset, int length);
}
