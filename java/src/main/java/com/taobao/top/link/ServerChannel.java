package com.taobao.top.link;

import com.taobao.top.link.handler.ChannelHandler;

public abstract class ServerChannel {
	protected abstract void run(ChannelHandler handler);
}
