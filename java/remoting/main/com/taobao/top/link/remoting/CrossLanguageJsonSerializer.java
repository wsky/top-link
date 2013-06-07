package com.taobao.top.link.remoting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

// design for cross-language
public class CrossLanguageJsonSerializer implements Serializer {
	private static final SerializerFeature[] features = {
			// SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullBooleanAsFalse,
			// wrapper.Args = methodCall.Args; maybe raise it
			// SerializerFeature.DisableCircularReferenceDetect
	};

	@Override
	public String getName() {
		return "json";
	}

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

			for (int i = 0; i < methodCall.Args.length; i++) {
				// said, java generics was not real type,
				// so HashMap.class did not work for json deserialize,
				// and HashMap<Object, Object>.class was wrong
				if (methodCall.MethodSignature[i].equals(HashMap.class))
					methodCall.Args[i] = JSON.parseObject(
							args.getJSONObject(i).toJSONString(),
							new TypeReference<HashMap<Object, Object>>() {
							});
				else
					methodCall.Args[i] = args.getObject(i, methodCall.MethodSignature[i]);
			}
		}
		return methodCall;
	}

	@Override
	public byte[] serializeMethodReturn(MethodReturn methodReturn) throws FormatterException {
		MethodReturnWrapper wrapper = new MethodReturnWrapper();
		wrapper.ReturnValue = methodReturn.ReturnValue;
		wrapper.Exception = methodReturn.Exception != null ?
				methodReturn.Exception.toString() :
				null;
		return JSON.toJSONBytes(wrapper, features);
	}

	@Override
	public MethodReturn deserializeMethodReturn(byte[] input, Class<?> returnType) throws FormatterException {
		JSONObject obj = (JSONObject) JSON.parse(input);
		MethodReturn methodReturn = new MethodReturn();
		methodReturn.ReturnValue = obj.get("ReturnValue") != null ?
				obj.getObject("ReturnValue", returnType) :
				null;
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
			return "";
		if (byte.class.equals(type))
			return "b";
		if (Byte.class.equals(type))
			return "B";
		if (double.class.equals(type))
			return "d";
		if (Double.class.equals(type))
			return "D";
		if (float.class.equals(type))
			return "f";
		if (Float.class.equals(type))
			return "F";
		if (int.class.equals(type))
			return "i";
		if (Integer.class.equals(type))
			return "I";
		if (long.class.equals(type))
			return "l";
		if (Long.class.equals(type))
			return "L";
		if (short.class.equals(type))
			return "s";
		if (Short.class.equals(type))
			return "S";
		if (Date.class.equals(type))
			return "t";
		if (Map.class.equals(type) || Map.class.isAssignableFrom(type))
			return "m";
		if (type.isArray())
			return String.format("[%s", this.parseTypeName(type.getComponentType()));
		return type.getName();
	}

	private Class<?> parseType(String typeName) throws ClassNotFoundException {
		if ("".equalsIgnoreCase(typeName))
			return String.class;
		if ("b".equalsIgnoreCase(typeName))
			return byte.class;
		if ("B".equalsIgnoreCase(typeName))
			return Byte.class;
		if ("d".equalsIgnoreCase(typeName))
			return double.class;
		if ("D".equalsIgnoreCase(typeName))
			return Double.class;
		if ("f".equalsIgnoreCase(typeName))
			return float.class;
		if ("F".equalsIgnoreCase(typeName))
			return Float.class;
		if ("i".equalsIgnoreCase(typeName))
			return int.class;
		if ("I".equalsIgnoreCase(typeName))
			return Integer.class;
		if ("l".equalsIgnoreCase(typeName))
			return long.class;
		if ("L".equalsIgnoreCase(typeName))
			return Long.class;
		if ("s".equalsIgnoreCase(typeName))
			return short.class;
		if ("S".equalsIgnoreCase(typeName))
			return Short.class;
		if ("t".equalsIgnoreCase(typeName))
			return Date.class;
		if ("m".equalsIgnoreCase(typeName))
			return HashMap.class;
		if (typeName.charAt(0) == '[')
			// java array: [Ljava.lang.String
			typeName = String.format("[L%s;",
					this.parseType(this.getComponentTypeName(typeName)).getName());
		return Class.forName(typeName, false, this.getClass().getClassLoader());
	}

	private String getComponentTypeName(String typeName) {
		return typeName.substring(1);
	}
}