package com.taobao.top.link.remoting;

public interface SerializationFactory {
	public Serializer get(Object format);
}