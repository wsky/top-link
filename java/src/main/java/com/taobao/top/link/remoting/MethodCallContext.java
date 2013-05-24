package com.taobao.top.link.remoting;

import java.util.HashMap;
import java.util.Map;

public class MethodCallContext {
	private Map<Object, Object> context;

	public MethodCallContext() {
		this.context = new HashMap<Object, Object>();
	}

	public Object getCallContext(Object key) {
		return this.context.get(key);
	}

	public void setCallContext(Object key, Object value) {
		this.context.put(key, value);
	}
}
