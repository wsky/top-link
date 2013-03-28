package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.taobao.top.link.LinkException;

public class DefaultIdentity implements Identity {

	private String name;

	public String getName() {
		return this.name;
	}

	public DefaultIdentity(String name) {
		this.name = name;
	}

	@Override
	public Identity parse(Object data) throws LinkException {
		ByteBuffer buffer = (ByteBuffer) data;
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int length = buffer.getInt();
		buffer.order(ByteOrder.BIG_ENDIAN);
		return new DefaultIdentity(new String(buffer.array(),
				buffer.position(), length, Charset.forName("UTF-8")));
	}

	@Override
	public void render(Object to) {
		ByteBuffer buffer = (ByteBuffer) to;
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		byte[] data = this.name.getBytes(Charset.forName("UTF-8"));
		buffer.putInt(data.length);
		buffer.put(data);
		buffer.order(ByteOrder.BIG_ENDIAN);
	}

	@Override
	public boolean equals(Identity id) {
		return id.getClass() == DefaultIdentity.class &&
				this.name.equals(((DefaultIdentity) id).name);
	}

}
