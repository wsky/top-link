package com.taobao.top.link.logging;

import org.apache.log4j.LogManager;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;

public class Log4jLoggerFactory implements LoggerFactory {

	@Override
	public Logger create(String type) {
		return new Log4jLogger(LogManager.getLogger(type));
	}

	@Override
	public Logger create(Class<?> type) {
		return new Log4jLogger(LogManager.getLogger(type));
	}

	@Override
	public Logger create(Object object) {
		return new Log4jLogger(LogManager.getLogger(object.getClass()));
	}

}
