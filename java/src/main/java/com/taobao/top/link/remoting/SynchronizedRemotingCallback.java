package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;

public class SynchronizedRemotingCallback extends RemotingCallback {
	public Object sync = new Object();
	
	private ByteBuffer resultBuffer;
	private boolean sucess;
	private Throwable failure;

	public boolean isSucess() {
		return this.sucess;
	}

	public Throwable getFailure() {
		return this.failure;
	}
	
	public ByteBuffer getResult() {
		return this.resultBuffer;
	}

	@Override
	public void onReceive(ByteBuffer buffer) {
		this.sucess = true;
		this.resultBuffer = buffer;
		this.nofityCall();
	}

	@Override
	public void onException(Throwable exception) {
		this.sucess = false;
		this.failure = exception;
		this.nofityCall();
	}

	private void nofityCall() {
		// TODO:anyother sync way?
		synchronized (this.sync) {
			this.sync.notify();
		}
	}
}