package com.taobao.top.link.remoting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

// design for cross-language
public class CrossLanguageJsonSerializer implements Serializer {
	private static final SerializerFeature[] features = {
			// SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullBooleanAsFalse,
	};

	@Override
	public byte[] serializeMethodCall(MethodCall methodCall) throws FormatterException {
		MethodCallWrapper wrapper = new MethodCallWrapper(methodCall);
		if (methodCall.MethodSignature != null) {
			wrapper.MethodSignature = new String[methodCall.MethodSignature.length];
			for (int i = 0; i < methodCall.MethodSignature.length; i++) {
				wrapper.MethodSignature[i] = this.parseTypeName(methodCall.MethodSignature[i]);
			}
		}
		if (methodCall.Args != null) {
			wrapper.Args = new String[methodCall.Args.length];
			for (int i = 0; i < methodCall.Args.length; i++) {
				wrapper.Args[i] = JSON.toJSONString(methodCall.Args[i], features);
			}
		}
		return JSON.toJSONBytes(wrapper, features);
	}

	@Override
	public MethodCall deserializeMethodCall(byte[] input) throws FormatterException {
		MethodCallWrapper wrapper = JSON.parseObject(input, MethodCallWrapper.class);
		MethodCall methodCall = new MethodCall();
		methodCall.MethodName = wrapper.MethodName;
		methodCall.TypeName = wrapper.TypeName;
		methodCall.Uri = wrapper.Uri;

		if (wrapper.MethodSignature != null) {
			methodCall.MethodSignature = new Class<?>[wrapper.MethodSignature.length];
			for (int i = 0; i < wrapper.MethodSignature.length; i++) {
				try {
					methodCall.MethodSignature[i] = this.parseType(wrapper.MethodSignature[i]);
				} catch (ClassNotFoundException e) {
					throw new FormatterException("parse MethodSignature error", e);
				}
			}
		}
		if (wrapper.Args != null) {
			methodCall.Args = new Object[wrapper.Args.length];
			for (int i = 0; i < wrapper.Args.length; i++) {
				methodCall.Args[i] = JSON.parseObject(wrapper.Args[i], methodCall.MethodSignature[i]);
			}
		}
		return methodCall;
	}

	@Override
	public byte[] serializeMethodReturn(MethodReturn methodReturn) throws FormatterException {
		MethodReturnWrapper wrapper = new MethodReturnWrapper();
		if (methodReturn.ReturnValue != null) {
			wrapper.ReturnValue = JSON.toJSONString(methodReturn.ReturnValue, features);
			wrapper.ReturnType = methodReturn.ReturnValue.getClass().getName();
		}
		if (methodReturn.Exception != null) {
			wrapper.Exception = methodReturn.Exception.toString();
		}
		return JSON.toJSONBytes(wrapper, features);
	}

	@Override
	public MethodReturn deserializeMethodReturn(byte[] input) throws FormatterException {
		MethodReturnWrapper wrapper = JSON.parseObject(input, MethodReturnWrapper.class);
		MethodReturn methodReturn = new MethodReturn();
		if (wrapper.ReturnValue != null && wrapper.ReturnType != null) {
			try {
				methodReturn.ReturnValue = JSON.parseObject(wrapper.ReturnValue, this.parseType(wrapper.ReturnType));
			} catch (ClassNotFoundException e) {
				throw new FormatterException("parse ReturnValue error", e);
			}
		}
		if (wrapper.Exception != null) {
			methodReturn.Exception = new Exception(wrapper.Exception);
			methodReturn.Exception.setStackTrace(new StackTraceElement[0]);
		}
		return methodReturn;
	}

	private String parseTypeName(Class<?> type) {
		if (String.class.equals(type))
			return "string";
		if (Byte.class.equals(type))
			return "byte";
		if (Double.class.equals(type))
			return "double";
		if (Float.class.equals(type))
			return "float";
		if (Integer.class.equals(type))
			return "int";
		if (Long.class.equals(type))
			return "long";
		if (Short.class.equals(type))
			return "short";
		if (Date.class.equals(type))
			return "date";
		if (Map.class.equals(type) || Map.class.isAssignableFrom(type))
			return "map";
		return type.getName();
	}

	private Class<?> parseType(String typeName) throws ClassNotFoundException {
		if ("string".equalsIgnoreCase(typeName))
			return String.class;
		if ("byte".equalsIgnoreCase(typeName))
			return byte.class;
		if ("double".equalsIgnoreCase(typeName))
			return double.class;
		if ("float".equalsIgnoreCase(typeName))
			return float.class;
		if ("int".equalsIgnoreCase(typeName))
			return int.class;
		if ("long".equalsIgnoreCase(typeName))
			return long.class;
		if ("short".equalsIgnoreCase(typeName))
			return short.class;
		if ("date".equalsIgnoreCase(typeName))
			return Date.class;
		if ("map".equalsIgnoreCase(typeName))
			return HashMap.class;
		return Class.forName(typeName, false, this.getClass().getClassLoader());
	}
}