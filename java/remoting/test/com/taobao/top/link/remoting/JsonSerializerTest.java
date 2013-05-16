package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.Date;

import org.junit.Test;

public class JsonSerializerTest {
	private Serializer serializer = new FastJsonSerializer();

	@Test
	public void methodCall_test() throws FormatterException, ClassNotFoundException {
		MethodCall call1 = new MethodCall();
		call1.MethodName = "echo";
		call1.TypeName = "serviceType";
		call1.Uri = "uri";
		call1.Args = new Object[] { "abc", 1, 1L, new Date(), new MethodReturn() };
		call1.MethodSignature = new Class<?>[] { 
				String.class, 
				Integer.class, 
				Long.class, 
				Date.class, 
				MethodReturn.class };

		byte[] ret = serializer.serializeMethodCall(call1);
		System.out.println(new String(ret, Charset.forName("UTF-8")));

		MethodCall call2 = serializer.deserializeMethodCall(ret);
		assertEquals(call1.MethodName, call2.MethodName);
		assertEquals(call1.TypeName, call2.TypeName);
		assertEquals(call1.Uri, call2.Uri);
		assertEquals(call1.Args.length, call2.Args.length);
		for (int i = 0; i < call1.Args.length; i++)
			assertEquals(call1.Args[i].getClass(), call2.Args[i].getClass());
		for (Object arg : call2.Args)
			System.out.println(String.format("%s|%s", arg.getClass(), arg));
	}
}
