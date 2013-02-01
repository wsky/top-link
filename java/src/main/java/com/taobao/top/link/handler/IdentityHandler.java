package com.taobao.top.link.handler;

import com.taobao.top.link.Identity;

public interface IdentityHandler {
	// for income conn
	public Identity receiveHandshake();
	// for outcome
	public void sendHandshake(Identity identity);
}
