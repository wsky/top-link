package com.taobao.top.link;

public class ChannelException extends Exception {

	private static final long serialVersionUID = 3556204532218058452L;

	public ChannelException(String message) {
		super(message);
	}

	public ChannelException(String message, Exception innerException) {
		super(message, innerException);
	}
	
	public ChannelException(String message, Throwable innerException) {
		super(message, innerException);
	}

}
