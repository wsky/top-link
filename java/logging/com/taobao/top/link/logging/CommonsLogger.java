package com.taobao.top.link.logging;

import org.apache.commons.logging.Log;

import com.taobao.top.link.Logger;

public class CommonsLogger implements Logger {
	private Log logger;

	public CommonsLogger(Log log) {
		this.logger = log;
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
		return this.logger.isWarnEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return this.logger.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		return this.logger.isFatalEnabled();
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
