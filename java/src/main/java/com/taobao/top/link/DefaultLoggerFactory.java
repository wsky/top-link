package com.taobao.top.link;

public class DefaultLoggerFactory implements LoggerFactory {

	@Override
	public Logger create(String type) {
		return new DefaultLogger(type);
	}

	@Override
	public Logger create(Class<?> type) {
		return new DefaultLogger(type.getSimpleName());
	}

	@Override
	public Logger create(Object object) {
		return new DefaultLogger(object.getClass().getSimpleName());
	}

}
