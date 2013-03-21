package com.taobao.top.link;

public interface Identity {
	public Identity parse(Object data) throws LinkException;

	public void render(Object to);

	public boolean equals(Identity id);
}