package com.taobao.top.link.remoting;

// default impl using reflect
public class DefaultMethodCallProcessor implements MethodCallProcessor {
	// private HashMap<String, Method> methods = new HashMap<String, Method>();

	@Override
	public MethodReturn process(MethodCall methodCall, MethodCallContext callContext) throws Throwable {
		MethodReturn methodReturn = new MethodReturn();
		methodReturn.ReturnValue = this.getClass()
				.getMethod(methodCall.MethodName, methodCall.MethodSignature)
				.invoke(this, methodCall.Args);
		return methodReturn;
	}
}
