package com.taobao.top.link.remoting;

public class CrossLanguageSerializationFactory extends DefaultSerializationFactory {
	private Serializer jsonSerializer = new CrossLanguageJsonSerializer();

	@Override
	public Serializer get(Object format) {
		if (format != null && "json".equals(format))
			return this.jsonSerializer;
		return super.get(format);
	}
}
