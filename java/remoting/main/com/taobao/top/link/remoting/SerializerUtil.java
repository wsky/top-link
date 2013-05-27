package com.taobao.top.link.remoting;

public class SerializerUtil {
	public static SerializationFactory getSerializationFactory(Object obj) {
		SerializationFactory factory = null;
		ClassLoader current = obj.getClass().getClassLoader();
		
		if (factory == null) {
			try {
				Class.forName("com.alibaba.fastjson.JSON", false, current);
				factory = new CrossLanguageSerializationFactory();
			} catch (ClassNotFoundException e) {
			}
		}
		
		if (factory == null)
			factory = new DefaultSerializationFactory();
		
		return factory;
	}
}