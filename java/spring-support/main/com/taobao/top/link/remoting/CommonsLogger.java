package com.taobao.top.link.remoting;

import org.apache.commons.logging.Log;

import com.taobao.top.link.Logger;

public class CommonsLogger implements Logger {
	private Log log;

	public CommonsLogger(Log log) {
		this.log = log;
	}

	@Override
	public boolean isDebugEnabled() {
		return this.log.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return this.log.isInfoEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return this.log.isWarnEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return this.log.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		return this.log.isFatalEnabled();
	}

	@Override
	public void debug(String message) {
		this.log.debug(message);
	}

	@Override
	public void debug(Throwable exception) {
		this.log.debug("", exception);
	}

	@Override
	public void debug(String message, Throwable exception) {
		this.log.debug(message, exception);
	}

	@Override
	public void debug(String format, Object... args) {
		this.log.debug(String.format(format, args));
	}

	@Override
	public void info(String message) {
		this.log.info(message);
	}

	@Override
	public void info(Throwable exception) {
		this.log.info("", exception);
	}

	@Override
	public void info(String message, Throwable exception) {
		this.log.info(message, exception);
	}

	@Override
	public void info(String format, Object... args) {
		this.log.info(String.format(format, args));
	}

	@Override
	public void warn(String message) {
		this.log.warn(message);
	}

	@Override
	public void warn(Throwable exception) {
		this.log.warn("", exception);
	}

	@Override
	public void warn(String message, Throwable exception) {
		this.log.warn(message, exception);
	}

	@Override
	public void warn(String format, Object... args) {
		this.log.warn(String.format(format, args));
	}

	@Override
	public void error(String message) {
		this.log.error(message);
	}

	@Override
	public void error(Throwable exception) {
		this.log.error("", exception);
	}

	@Override
	public void error(String message, Throwable exception) {
		this.log.error(message, exception);
	}

	@Override
	public void error(String format, Object... args) {
		this.log.error(String.format(format, args));
	}

	@Override
	public void fatal(String message) {
		this.log.fatal(message);
	}

	@Override
	public void fatal(Throwable exception) {
		this.log.fatal("", exception);
	}

	@Override
	public void fatal(String message, Throwable exception) {
		this.log.fatal(message, exception);
	}

	@Override
	public void fatal(String format, Object... args) {
		this.log.fatal(String.format(format, args));
	}

}
