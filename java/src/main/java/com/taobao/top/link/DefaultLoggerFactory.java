package com.taobao.top.link;

public class DefaultLoggerFactory implements LoggerFactory {
	private static LoggerFactory _default;

	static {
		setDefault(false, true, true, true, true);
	}

	public static LoggerFactory getDefault() {
		return _default;
	}

	public static void setDefault(boolean isDebugEnable,
			boolean isInfoEnable,
			boolean isWarnEnable,
			boolean isErrorEnable,
			boolean isFatalEnable) {
		_default = new DefaultLoggerFactory(isDebugEnable,
				isInfoEnable,
				isWarnEnable,
				isErrorEnable,
				isFatalEnable);
	}

	private boolean isDebugEnable;
	private boolean isInfoEnable;
	private boolean isWarnEnable;
	private boolean isErrorEnable;
	private boolean isFatalEnable;

	public DefaultLoggerFactory() {
		this(false, true, true, true, true);
	}

	public DefaultLoggerFactory(boolean isDebugEnable,
			boolean isInfoEnable,
			boolean isWarnEnable,
			boolean isErrorEnable,
			boolean isFatalEnable) {
		this.isDebugEnable = isDebugEnable;
		this.isInfoEnable = isInfoEnable;
		this.isWarnEnable = isWarnEnable;
		this.isErrorEnable = isErrorEnable;
		this.isFatalEnable = isFatalEnable;
	}

	@Override
	public Logger create(String type) {
		return new DefaultLogger(type,
				this.isDebugEnable,
				this.isInfoEnable,
				this.isWarnEnable,
				this.isErrorEnable,
				this.isFatalEnable);
	}

	@Override
	public Logger create(Class<?> type) {
		return new DefaultLogger(type.getSimpleName(),
				this.isDebugEnable,
				this.isInfoEnable,
				this.isWarnEnable,
				this.isErrorEnable,
				this.isFatalEnable);
	}

	@Override
	public Logger create(Object object) {
		return new DefaultLogger(object.getClass().getSimpleName(),
				this.isDebugEnable,
				this.isInfoEnable,
				this.isWarnEnable,
				this.isErrorEnable,
				this.isFatalEnable);
	}

}
