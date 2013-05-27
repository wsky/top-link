package com.taobao.top.link.endpoint;

import java.util.Map;

import com.taobao.top.link.LinkException;

public class DefaultIdentity implements Identity {

	private String name;

	public String getName() {
		return this.name;
	}

	public DefaultIdentity(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Identity parse(Object data) throws LinkException {
		Map<String, String> dict = (Map<String, String>) data;
		return new DefaultIdentity(dict.get("name"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(Object to) {
		Map<String, String> dict = (Map<String, String>) to;
		dict.put("name", this.name);
	}

	@Override
	public boolean equals(Identity id) {
		return id.getClass() == DefaultIdentity.class &&
				this.name.equals(((DefaultIdentity) id).name);
	}

	@Override
	public String toString() {
		return this.name;
	}
}
