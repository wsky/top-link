package com.taobao.top.link.handler;

import com.taobao.top.link.Identity;

public interface IdentityHandler {
	// for income conn
	public Identity receiveHandshake(byte[] data, int offset, int length);

	// for outcome
	public byte[] sendHandshake(Identity identity);
}
