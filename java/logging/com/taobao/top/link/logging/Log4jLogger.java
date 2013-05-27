package com.taobao.top.link.logging;

import org.apache.log4j.Level;

import com.taobao.top.link.Logger;

public class Log4jLogger implements Logger {

	private org.apache.log4j.Logger logger;

	public Log4jLogger(org.apache.log4j.Logger logger) {
		this.logger = logger;
	}

	@Override
	public boolean isDebugEnabled() {
		return this.logger.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return this.logger.isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return this.logger.isEnabledFor(Level.WARN);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.logger.isEnabledFor(Level.ERROR);
	}

	@Override
	public boolean isFatalEnabled() {
		return this.logger.isEnabledFor(Level.FATAL);
	}

	@Override
	public void debug(String message) {
		this.logger.debug(message);
	}

	@Override
	public void debug(Throwable exception) {
		this.logger.debug("", exception);
	}

	@Override
	public void debug(String message, Throwable exception) {
		this.logger.debug(message, exception);
	}

	@Override
	public void debug(String format, Object... args) {
		this.logger.debug(String.format(format, args));
	}

	@Override
	public void info(String message) {
		this.logger.info(message);
	}

	@Override
	public void info(Throwable exception) {
		this.logger.info("", exception);
	}

	@Override
	public void info(String message, Throwable exception) {
		this.logger.info(message, exception);
	}

	@Override
	public void info(String format, Object... args) {
		this.logger.info(String.format(format, args));
	}

	@Override
	public void warn(String message) {
		this.logger.warn(message);
	}

	@Override
	public void warn(Throwable exception) {
		this.logger.warn("", exception);
	}

	@Override
	public void warn(String message, Throwable exception) {
		this.logger.warn(message, exception);
	}

	@Override
	public void warn(String format, Object... args) {
		this.logger.warn(String.format(format, args));
	}

	@Override
	public void error(String message) {
		this.logger.error(message);
	}

	@Override
	public void error(Throwable exception) {
		this.logger.error("", exception);
	}

	@Override
	public void error(String message, Throwable exception) {
		this.logger.error(message, exception);
	}

	@Override
	public void error(String format, Object... args) {
		this.logger.error(String.format(format, args));
	}

	@Override
	public void fatal(String message) {
		this.logger.fatal(message);
	}

	@Override
	public void fatal(Throwable exception) {
		this.logger.fatal("", exception);
	}

	@Override
	public void fatal(String message, Throwable exception) {
		this.logger.fatal(message, exception);
	}

	@Override
	public void fatal(String format, Object... args) {
		this.logger.fatal(String.format(format, args));
	}
}
