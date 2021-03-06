package top.link.remoting.serialization;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;

import top.link.LinkException;
import top.link.remoting.FormatterException;
import top.link.remoting.MethodCall;
import top.link.remoting.MethodReturn;
import top.link.remoting.Serializer;
import top.link.remoting.serialization.CrossLanguageJsonSerializer;

public class JsonSerializerTest {
	@Test
	public void cross_methodCall_test() throws FormatterException, UnsupportedEncodingException {
		methodCall_test(new CrossLanguageJsonSerializer());
	}
	
	@Test
	public void cross_methodReturn_test() throws FormatterException, UnsupportedEncodingException {
		methodReturn_test(new CrossLanguageJsonSerializer());
	}
	
	@SuppressWarnings("unchecked")
	private void methodCall_test(Serializer serializer) throws FormatterException, UnsupportedEncodingException {
		MethodCall call1 = new MethodCall();
		call1.MethodName = "echo";
		call1.TypeName = "serviceType";
		call1.Uri = "uri";
		call1.Args = new Object[] {
				"abc中文",// unicode support?
				(byte) 1,
				(double) 1.1,
				(float) 1.2,
				1,
				1L,
				(short) 1,
				new Date(),
				getMap(),
				getEntity(),
				new String[] { "abc" } };
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
				Entity.class,
				String[].class };
		
		byte[] ret = serializer.serializeMethodCall(call1);
		System.out.println(new String(ret, "UTF-8"));
		MethodCall call2 = serializer.deserializeMethodCall(ret);
		assertEquals(call1.MethodName, call2.MethodName);
		assertEquals(call1.TypeName, call2.TypeName);
		assertEquals(call1.Uri, call2.Uri);
		assertEquals(call1.Args.length, call2.Args.length);
		for (int i = 0; i < call1.Args.length; i++) {
			assertEquals(call1.Args[i].getClass(), call2.Args[i].getClass());
			assertEquals(call1.MethodSignature[i], call2.MethodSignature[i]);
			if (call1.MethodSignature[i] == HashMap.class)
				assertEquals(
						((HashMap<String, String>) call1.Args[i]).size(),
						((HashMap<String, String>) call2.Args[i]).size());
		}
		for (Object arg : call2.Args)
			System.out.println(String.format("%s|%s", arg.getClass(), arg));
	}
	
	private void methodReturn_test(Serializer serializer) throws FormatterException, UnsupportedEncodingException {
		MethodReturn _return1 = new MethodReturn();
		_return1.Exception = new LinkException("error", new NullPointerException());
		_return1.ReturnValue = getEntity();
		
		byte[] ret = serializer.serializeMethodReturn(_return1);
		System.out.println(new String(ret, "UTF-8"));
		
		MethodReturn _return2 = serializer.deserializeMethodReturn(ret, Entity.class);
		
		System.err.println(_return2.Exception.getMessage());
		_return2.Exception.printStackTrace();
		
		assertEquals(_return1.ReturnValue.getClass(), _return2.ReturnValue.getClass());
		assertEquals(((Entity) _return1.ReturnValue).getString(), ((Entity) _return2.ReturnValue).getString());
		System.out.println(((Entity) _return2.ReturnValue).getMap());
		assertEquals(((Entity) _return1.ReturnValue).getMap(), ((Entity) _return2.ReturnValue).getMap());
		assertEquals(((Entity) _return1.ReturnValue).getMap().size(), ((Entity) _return2.ReturnValue).getMap().size());
		assertEquals(((Entity) _return1.ReturnValue).getArray()[0], ((Entity) _return2.ReturnValue).getArray()[0]);
	}
	
	private HashMap<String, String> getMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("k", "k");
		return map;
	}
	
	private Entity getEntity() {
		Entity e = new Entity();
		e.setString("string");
		e.setMap(getMap());
		e.setArray(new String[] { "abc" });
		return e;
	}
}
