package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;

// one callback per rpc-call
public abstract class RemotingCallback {

	public String flag;

	public abstract void onException(Throwable exception);

	public abstract void onReceive(ByteBuffer buffer);
}
