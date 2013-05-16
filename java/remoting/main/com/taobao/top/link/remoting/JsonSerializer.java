package com.taobao.top.link.remoting;

import com.alibaba.fastjson.JSON;

public class JsonSerializer implements Serializer {
	@Override
	public byte[] serializeMethodCall(MethodCall methodCall) throws FormatterException {
		MethodCallWrapper wrapper = new MethodCallWrapper(methodCall);
		if (methodCall.MethodSignature != null) {
			wrapper.strMethodSignature = new String[methodCall.MethodSignature.length];
			for (int i = 0; i < methodCall.MethodSignature.length; i++) {
				wrapper.strMethodSignature[i] = methodCall.MethodSignature[i].getName();
			}
		}
		if (methodCall.Args != null) {
			wrapper.strArgs = new String[methodCall.Args.length];
			for (int i = 0; i < methodCall.Args.length; i++) {
				wrapper.strArgs[i] = JSON.toJSONString(methodCall.Args[i]);
			}
		}
		return JSON.toJSONBytes(wrapper);
	}

	// TODO: base type name, long,int,

	@Override
	public MethodCall deserializeMethodCall(byte[] input) throws FormatterException {
		MethodCallWrapper wrapper = JSON.parseObject(input, MethodCallWrapper.class);
		MethodCall methodCall = new MethodCall();
		methodCall.MethodName = wrapper.MethodName;
		methodCall.TypeName = wrapper.TypeName;
		methodCall.Uri = wrapper.Uri;

		if (wrapper.strMethodSignature != null) {
			methodCall.MethodSignature = new Class<?>[wrapper.strMethodSignature.length];
			for (int i = 0; i < wrapper.strMethodSignature.length; i++) {
				try {
					methodCall.MethodSignature[i] = Class.forName(
							wrapper.strMethodSignature[i], false, this.getClass().getClassLoader());
				} catch (ClassNotFoundException e) {
					throw new FormatterException("parse MethodSignature error", e);
				}
			}
		}
		if (wrapper.strArgs != null) {
			methodCall.Args = new Object[wrapper.strArgs.length];
			for (int i = 0; i < wrapper.strArgs.length; i++) {
				methodCall.Args[i] = JSON.parseObject(wrapper.strArgs[i], methodCall.MethodSignature[i]);
			}
		}
		return methodCall;
	}

	@Override
	public MethodReturn deserializeMethodReturn(byte[] input) throws FormatterException {
		return JSON.parseObject(input, MethodReturn.class);
	}

	@Override
	public byte[] serializeMethodReturn(MethodReturn methodReturn) throws FormatterException {
		return JSON.toJSONBytes(methodReturn);
	}
}