package com.taobao.top.link.remoting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

// design for cross-language
public class CrossLanguageJsonSerializer implements Serializer {
	private static final SerializerFeature[] features = {
			// SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullBooleanAsFalse,
			// wrapper.Args = methodCall.Args; maybe raise it
			SerializerFeature.DisableCircularReferenceDetect
	};

	@Override
	public byte[] serializeMethodCall(MethodCall methodCall) throws FormatterException {
		MethodCallWrapper wrapper = new MethodCallWrapper(methodCall);
		wrapper.Args = methodCall.Args;
		wrapper.MethodSignature = new String[
				methodCall.MethodSignature != null ? methodCall.MethodSignature.length : 0];
		for (int i = 0; i < methodCall.MethodSignature.length; i++)
			wrapper.MethodSignature[i] = this.parseTypeName(methodCall.MethodSignature[i]);
		return JSON.toJSONBytes(wrapper, features);
	}

	@Override
	public MethodCall deserializeMethodCall(byte[] input) throws FormatterException {
		JSONObject obj = (JSONObject) JSON.parse(input);
		MethodCall methodCall = new MethodCall();
		methodCall.MethodName = obj.getString("MethodName");
		methodCall.TypeName = obj.getString("TypeName");
		methodCall.Uri = obj.getString("Uri");

		JSONArray methodSignature = obj.getJSONArray("MethodSignature");
		if (methodSignature != null) {
			methodCall.MethodSignature = new Class<?>[methodSignature.size()];
			for (int i = 0; i < methodCall.MethodSignature.length; i++) {
				try {
					methodCall.MethodSignature[i] = this.parseType(methodSignature.getString(i));
				} catch (ClassNotFoundException e) {
					throw new FormatterException("parse MethodSignature error", e);
				}
			}
		}

		JSONArray args = obj.getJSONArray("Args");
		if (args != null) {
			methodCall.Args = new Object[args.size()];
			for (int i = 0; i < methodCall.Args.length; i++)
				methodCall.Args[i] = args.getObject(i, methodCall.MethodSignature[i]);
		}
		return methodCall;
	}

	@Override
	public byte[] serializeMethodReturn(MethodReturn methodReturn) throws FormatterException {
		MethodReturnWrapper wrapper = new MethodReturnWrapper();
		wrapper.ReturnValue = methodReturn.ReturnValue;
		wrapper.ReturnType = wrapper.ReturnValue != null ?
				this.parseTypeName(methodReturn.ReturnValue.getClass()) :
				null;
		wrapper.Exception = methodReturn.Exception != null ?
				methodReturn.Exception.toString() :
				null;
		return JSON.toJSONBytes(wrapper, features);
	}

	@Override
	public MethodReturn deserializeMethodReturn(byte[] input) throws FormatterException {
		JSONObject obj = (JSONObject) JSON.parse(input);
		MethodReturn methodReturn = new MethodReturn();

		try {
			String returnType = obj.getString("ReturnType");
			methodReturn.ReturnValue = returnType != null ?
					obj.getObject("ReturnValue", this.parseType(returnType)) : null;
		} catch (ClassNotFoundException e) {
			throw new FormatterException("parse ReturnValue error", e);
		}

		// TODO:add error stack support
		String exception = obj.getString("Exception");
		if (exception != null && !exception.equals("")) {
			methodReturn.Exception = new Exception(exception);
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
		if (type.isArray())
			return String.format("%s[]", this.parseTypeName(type.getComponentType()));
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
		// array
		if (typeName.endsWith("[]"))
			typeName = String.format("[L%s;",
					this.parseType(this.getComponentTypeName(typeName)).getName());
		return Class.forName(typeName, false, this.getClass().getClassLoader());
	}

	private String getComponentTypeName(String typeName) {
		return typeName.substring(0, typeName.indexOf('['));
	}
}