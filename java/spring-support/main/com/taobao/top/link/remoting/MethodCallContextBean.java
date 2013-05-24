package com.taobao.top.link.remoting;

public class MethodCallContextBean {
	private static ThreadLocal<MethodCallContext> currentContext = new ThreadLocal<MethodCallContext>();

	public Object get(Object key) {
		return currentContext.get().get(key);
	}

	protected static void setCurrentContext(MethodCallContext context) {
		currentContext.set(context);
	}
}
