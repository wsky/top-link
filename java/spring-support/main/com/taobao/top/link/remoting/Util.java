package com.taobao.top.link.remoting;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;

public class Util {
	public static LoggerFactory getLoggerFactory(Object obj) {
		LoggerFactory loggerFactory = null;
		ClassLoader current = obj.getClass().getClassLoader();

		// LogManager.getRootLogger().addAppender(new ConsoleAppender(new
		// PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
		try {
			Class.forName("org.apache.log4j.LogManager", false, current);
			loggerFactory = new Log4jLoggerFactory();
		} catch (ClassNotFoundException e) {
		}

		if (loggerFactory == null) {
			try {
				Class.forName("org.apache.commons.logging.Log", false, current);
				loggerFactory = new CommonsLoggerFactory();
			} catch (ClassNotFoundException e) {
			}
		}

		return loggerFactory == null ?
				DefaultLoggerFactory.getDefault() : loggerFactory;
	}
}
