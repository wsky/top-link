package com.taobao.top.link;

public interface Identity {
	public Identity parse(Object data) throws LinkException;

	public byte[] toBytes();

	public boolean equals(Identity id);
}