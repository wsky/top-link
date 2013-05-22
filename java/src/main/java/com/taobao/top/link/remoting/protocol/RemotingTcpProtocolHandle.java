package com.taobao.top.link.remoting.protocol;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import remoting.protocol.tcp.TcpProtocolHandle;

public class RemotingTcpProtocolHandle extends TcpProtocolHandle {
	public RemotingTcpProtocolHandle(ByteBuffer source) {
		super(source);
	}

	@Override
	protected boolean writeExtendedHeader(Entry<String, Object> entry) {
		return super.writeExtendedHeader(entry);
	}
	
	@Override
	protected boolean readExtendedHeader(short headerType, HashMap<String, Object> dict) {
		return super.readExtendedHeader(headerType, dict);
	}
}
