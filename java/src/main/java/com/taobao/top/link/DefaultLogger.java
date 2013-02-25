package com.taobao.top.link;

public class DefaultLogger implements Logger {
	private String type;

	public DefaultLogger(String type) {
		this.type = type;
	}

	@Override
	public void debug(String message) {
		System.out.println(String.format("[DEBUG] [%s] - %s", this.type, message));
	}

	@Override
	public void debug(Throwable exception) {
		System.out.println(String.format("[DEBUG] [%s] - %s", this.type, exception));
	}

	@Override
	public void debug(String message, Throwable exception) {
		System.out.println(String.format("[DEBUG] [%s] - %s %s", this.type, message, exception));
	}

	@Override
	public void debug(String format, Object... args) {
		System.out.println(String.format("[DEBUG] [%s] - %s", this.type, String.format(format, args)));
	}

	@Override
	public void info(String message) {
		System.out.println(String.format("[INFO] [%s] - %s", this.type, message));
	}

	@Override
	public void info(Throwable exception) {
		System.out.println(String.format("[INFO] [%s] - %s", this.type, exception));
	}

	@Override
	public void info(String message, Throwable exception) {
		System.out.println(String.format("[INFO] [%s] - %s %s", this.type, message, exception));
	}

	@Override
	public void info(String format, Object... args) {
		System.out.println(String.format("[INFO] [%s] - %s", this.type, String.format(format, args)));
	}

	@Override
	public void warn(String message) {
		System.out.println(String.format("[WARN] [%s] - %s", this.type, message));
	}

	@Override
	public void warn(Throwable exception) {
		System.out.println(String.format("[WARN] [%s] - %s", this.type, exception));

	}

	@Override
	public void warn(String message, Throwable exception) {
		System.out.println(String.format("[WARN] [%s] - %s %s", this.type, message, exception));
	}

	@Override
	public void warn(String format, Object... args) {
		System.out.println(String.format("[WARN] [%s] - %s", this.type, String.format(format, args)));
	}

	@Override
	public void error(String message) {
		System.err.println(String.format("[ERROR] [%s] - %s", this.type, message));
	}

	@Override
	public void error(Throwable exception) {
		System.err.println(String.format("[ERROR] [%s] - %s", this.type, exception));
		exception.printStackTrace();
	}

	@Override
	public void error(String message, Throwable exception) {
		System.err.println(String.format("[ERROR] [%s] - %s %s", this.type, message, exception));
		exception.printStackTrace();
	}

	@Override
	public void error(String format, Object... args) {
		System.err.println(String.format("[ERROR] [%s] - %s", this.type, String.format(format, args)));
	}

	@Override
	public void fatal(String message) {
		System.err.println(String.format("[FATAL] [%s] - %s", this.type, message));
	}

	@Override
	public void fatal(Throwable exception) {
		System.err.println(String.format("[FATAL] [%s] - %s", this.type, exception));
		exception.printStackTrace();
	}

	@Override
	public void fatal(String message, Throwable exception) {
		System.err.println(String.format("[FATAL] [%s] - %s %s", this.type, message, exception));
		exception.printStackTrace();
	}

	@Override
	public void fatal(String format, Object... args) {
		System.err.println(String.format("[FATAL] [%s] - %s", this.type, String.format(format, args)));
	}
}
