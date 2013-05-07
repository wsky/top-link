package com.taobao.top.link;

public class DefaultLogger implements Logger {
	private String type;
	private boolean isDebugEnable;
	private boolean isInfoEnable;
	private boolean isWarnEnable;
	private boolean isErrorEnable;
	private boolean isFatalEnable;

	public DefaultLogger(String type,
			boolean isDebugEnable,
			boolean isInfoEnable,
			boolean isWarnEnable,
			boolean isErrorEnable,
			boolean isFatalEnable) {
		this.type = type;
		this.isDebugEnable = isDebugEnable;
		this.isInfoEnable = isInfoEnable;
		this.isWarnEnable = isWarnEnable;
		this.isErrorEnable = isErrorEnable;
		this.isFatalEnable = isFatalEnable;
	}

	@Override
	public boolean isDebugEnable() {
		return this.isDebugEnable;
	}

	@Override
	public boolean isInfoEnable() {
		return this.isInfoEnable;
	}

	@Override
	public boolean isWarnEnable() {
		return this.isWarnEnable;
	}

	@Override
	public boolean isErrorEnable() {
		return this.isErrorEnable;
	}

	@Override
	public boolean isFatalEnable() {
		return this.isFatalEnable;
	}

	@Override
	public void debug(String message) {
		System.out.println(String.format("[DEBUG] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), message));
	}

	@Override
	public void debug(Throwable exception) {
		System.out.println(String.format("[DEBUG] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), exception));
	}

	@Override
	public void debug(String message, Throwable exception) {
		System.out.println(String.format("[DEBUG] [%s] [%s] - %s %s", this.type, Thread.currentThread().getName(), message, exception));
	}

	@Override
	public void debug(String format, Object... args) {
		System.out.println(String.format("[DEBUG] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), String.format(format, args)));
	}

	@Override
	public void info(String message) {
		System.out.println(String.format("[INFO] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), message));
	}

	@Override
	public void info(Throwable exception) {
		System.out.println(String.format("[INFO] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), exception));
	}

	@Override
	public void info(String message, Throwable exception) {
		System.out.println(String.format("[INFO] [%s] [%s] - %s %s", this.type, Thread.currentThread().getName(), message, exception));
	}

	@Override
	public void info(String format, Object... args) {
		System.out.println(String.format("[INFO] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), String.format(format, args)));
	}

	@Override
	public void warn(String message) {
		System.out.println(String.format("[WARN] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), message));
	}

	@Override
	public void warn(Throwable exception) {
		System.out.println(String.format("[WARN] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), exception));

	}

	@Override
	public void warn(String message, Throwable exception) {
		System.out.println(String.format("[WARN] [%s] [%s] - %s %s", this.type, Thread.currentThread().getName(), message, exception));
	}

	@Override
	public void warn(String format, Object... args) {
		System.out.println(String.format("[WARN] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), String.format(format, args)));
	}

	@Override
	public void error(String message) {
		System.err.println(String.format("[ERROR] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), message));
	}

	@Override
	public void error(Throwable exception) {
		System.err.println(String.format("[ERROR] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), exception));
		exception.printStackTrace();
	}

	@Override
	public void error(String message, Throwable exception) {
		System.err.println(String.format("[ERROR] [%s] [%s] - %s %s", this.type, Thread.currentThread().getName(), message, exception));
		exception.printStackTrace();
	}

	@Override
	public void error(String format, Object... args) {
		System.err.println(String.format("[ERROR] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), String.format(format, args)));
	}

	@Override
	public void fatal(String message) {
		System.err.println(String.format("[FATAL] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), message));
	}

	@Override
	public void fatal(Throwable exception) {
		System.err.println(String.format("[FATAL] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), exception));
		exception.printStackTrace();
	}

	@Override
	public void fatal(String message, Throwable exception) {
		System.err.println(String.format("[FATAL] [%s] [%s] - %s %s", this.type, Thread.currentThread().getName(), message, exception));
		exception.printStackTrace();
	}

	@Override
	public void fatal(String format, Object... args) {
		System.err.println(String.format("[FATAL] [%s] [%s] - %s", this.type, Thread.currentThread().getName(), String.format(format, args)));
	}
}
