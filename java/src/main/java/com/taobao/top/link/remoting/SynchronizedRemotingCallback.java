package com.taobao.top.link.remoting;

public class SynchronizedRemotingCallback extends RemotingCallback {
	public Object sync = new Object();
	
	private MethodReturn methodReturn;
	private boolean sucess;
	private Throwable failure;

	public boolean isSucess() {
		return this.sucess;
	}

	public Throwable getFailure() {
		return this.failure;
	}
	
	public MethodReturn getMethodReturn() {
		return this.methodReturn;
	}

	@Override
	public void onMethodReturn(MethodReturn methodReturn) {
		this.sucess = true;
		this.methodReturn = methodReturn;
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