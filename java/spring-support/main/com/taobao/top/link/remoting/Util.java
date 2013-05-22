package com.taobao.top.link.remoting;

//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.PatternLayout;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;

public class Util {
	static {
		//LogManager.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
	}

	public static LoggerFactory getLoggerFactory(Object obj) {
		LoggerFactory loggerFactory = null;
		ClassLoader current = obj.getClass().getClassLoader();

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
