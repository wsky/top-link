package com.taobao.top.link.remoting;

import java.lang.reflect.Type;

@SuppressWarnings("serial")
public class MethodCall implements Message {
	public String Uri;
	public String MethodName;
	public String TypeName;
	public Type[] MethodSignature;
	public Object[] Args;
}
