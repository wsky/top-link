package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.top.link.LinkException;

public class JsonSerializerTest {
	// @Test
	public void null_test() {
		String json = JSON.toJSONString(new MethodReturn(), SerializerFeature.WriteMapNullValue, SerializerFeature.WriteClassName);
		System.out.println(json);
		JSON.parseObject(JSON.toJSONString(new MethodReturn(), SerializerFeature.WriteClassName), MethodReturn.class);
		JSON.parseObject(JSON.toJSONString(new MethodReturn(), SerializerFeature.WriteMapNullValue), MethodReturn.class);
	}

	@Test
	public void cross_methodCall_test() throws FormatterException {
		methodCall_test(new CrossLanguageJsonSerializer());
	}

	@Test
	public void cross_methodReturn_test() throws FormatterException {
		methodReturn_test(new CrossLanguageJsonSerializer());
	}

	// @Test
	// public void simple_methodCall_test() throws FormatterException {
	// methodCall_test(new SimpleJsonSerializer());
	// }
	//
	// @Test
	// public void simple_methodReturn_test() throws FormatterException {
	// methodReturn_test(new SimpleJsonSerializer());
	// }

	private void methodCall_test(Serializer serializer) throws FormatterException {
		MethodCall call1 = new MethodCall();
		call1.MethodName = "echo";
		call1.TypeName = "serviceType";
		call1.Uri = "uri";
		call1.Args = new Object[] {
				"abc",
				(byte) 1,
				(double) 1.1,
				(float) 1.2,
				1,
				1L,
				(short) 1,
				new Date(),
				this.getMap(),
				new MethodCall() };
		call1.MethodSignature = new Class<?>[] {
				String.class,
				byte.class,
				double.class,
				float.class,
				int.class,
				long.class,
				short.class,
				Date.class,
				HashMap.class,
				MethodCall.class };

		byte[] ret = serializer.serializeMethodCall(call1);
		System.out.println(new String(ret, Charset.forName("UTF-8")));
		MethodCall call2 = serializer.deserializeMethodCall(ret);
		assertEquals(call1.MethodName, call2.MethodName);
		assertEquals(call1.TypeName, call2.TypeName);
		assertEquals(call1.Uri, call2.Uri);
		assertEquals(call1.Args.length, call2.Args.length);
		for (int i = 0; i < call1.Args.length; i++) {
			assertEquals(call1.Args[i].getClass(), call2.Args[i].getClass());
			assertEquals(call1.MethodSignature[i], call2.MethodSignature[i]);
		}
		for (Object arg : call2.Args)
			System.out.println(String.format("%s|%s", arg.getClass(), arg));
	}

	private void methodReturn_test(Serializer serializer) throws FormatterException {
		MethodReturn _return1 = new MethodReturn();
		_return1.Exception = new LinkException("error", new NullPointerException());
		MethodReturn returnValue = new MethodReturn();
		returnValue.ReturnValue = "abc";
		_return1.ReturnValue = returnValue;

		byte[] ret = serializer.serializeMethodReturn(_return1);
		System.out.println(new String(ret, Charset.forName("UTF-8")));

		MethodReturn _return2 = serializer.deserializeMethodReturn(ret);
		assertEquals(_return1.ReturnValue.getClass(), _return2.ReturnValue.getClass());
		assertEquals(((MethodReturn) _return1.ReturnValue).ReturnValue, ((MethodReturn) _return2.ReturnValue).ReturnValue);
		System.err.println(_return2.Exception.getMessage());
		_return2.Exception.printStackTrace();
		// assertEquals(_return1.Exception.getMessage(),
		// _return2.Exception.getMessage());
		// assertEquals(_return1.Exception.getClass(),
		// _return2.Exception.getClass());
		// assertEquals(_return1.Exception.getCause().getClass(),
		// _return2.Exception.getCause().getClass());
		// assertEquals(_return1.Exception.getCause().getMessage(),
		// _return2.Exception.getCause().getMessage());
	}

	private HashMap<String, String> getMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("k", "k");
		return map;
	}
}
