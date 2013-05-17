package com.taobao.top.link.remoting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

// simple design dependent json implementation(alibaba.fastjson)
public class SimpleJsonSerializer implements Serializer {
	private static final SerializerFeature[] features = {
			SerializerFeature.WriteClassName,
			// SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullBooleanAsFalse,
	};
	private PropertyFilter filter;

	public SimpleJsonSerializer() {
		this.filter = new PropertyFilter() {
			@Override
			public boolean apply(Object source, String name, Object value) {
				return !"MethodSignature".equals(name);
			}
		};
	}

	@Override
	public byte[] serializeMethodCall(MethodCall methodCall) throws FormatterException {
		SerializeWriter out = new SerializeWriter();
		try {
			JSONSerializer serializer = new JSONSerializer(out);
			serializer.getPropertyFilters().add(this.filter);
			for (SerializerFeature feature : features)
				serializer.config(feature, true);
			serializer.write(methodCall);
			return out.toBytes("UTF-8");
		} finally {
			out.close();
		}
	}

	@Override
	public MethodCall deserializeMethodCall(byte[] input) throws FormatterException {
		MethodCall methodCall = JSON.parseObject(input, MethodCall.class);
		if (methodCall.Args != null) {
			methodCall.MethodSignature = new Class<?>[methodCall.Args.length];
			for (int i = 0; i < methodCall.MethodSignature.length; i++)
				methodCall.MethodSignature[i] = methodCall.Args[i].getClass();
		}
		return methodCall;
	}

	@Override
	public byte[] serializeMethodReturn(MethodReturn methodReturn) throws FormatterException {
		return JSON.toJSONBytes(methodReturn, features);
	}

	@Override
	public MethodReturn deserializeMethodReturn(byte[] input) throws FormatterException {
		return JSON.parseObject(input, MethodReturn.class);
	}
}