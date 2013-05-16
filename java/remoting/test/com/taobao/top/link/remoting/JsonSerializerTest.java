package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.taobao.top.link.LinkException;

public class JsonSerializerTest {
	private Serializer serializer = new FastJsonSerializer();

	@Test
	public void methodCall_test() throws FormatterException, ClassNotFoundException {
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
				Byte.class,
				Double.class,
				Float.class,
				Integer.class,
				Long.class,
				Short.class,
				Date.class,
				HashMap.class,
				MethodCall.class };

		byte[] ret = serializer.serializeMethodCall(call1);
		System.out.println(new String(ret, Charset.forName("UTF-8")));
		// {"Args":["\"abc\"","1","1","1368700907997","{}"],"MethodName":"echo","MethodSignature":["java.lang.String","java.lang.Integer","java.lang.Long","java.util.Date","com.taobao.top.link.remoting.MethodCall"],"TypeName":"serviceType","Uri":"uri"}
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

	@Test
	public void methodReturn_test() throws FormatterException {
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

	private Map<String, String> getMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("k", "k");
		return map;
	}
}
