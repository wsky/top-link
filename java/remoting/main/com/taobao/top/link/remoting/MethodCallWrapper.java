package com.taobao.top.link.remoting;

@SuppressWarnings("serial")
public class MethodCallWrapper extends MethodCall {
	public String[] MethodSignature;
	public String[] Args;

	public MethodCallWrapper() {
	}

	public MethodCallWrapper(MethodCall origin) {
		this.MethodName = origin.MethodName;
		this.TypeName = origin.TypeName;
		this.Uri = origin.Uri;
	}
}