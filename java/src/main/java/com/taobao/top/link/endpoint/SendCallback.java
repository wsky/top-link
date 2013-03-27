package com.taobao.top.link.endpoint;

import com.taobao.top.link.LinkException;

public class SendCallback {
	private Object sync = new Object();
	private EndpointProxy endpointProxy;
	private LinkException error;
	private Object _return;

	public SendCallback(EndpointProxy endpointProxy) {
		this.endpointProxy = endpointProxy;
	}

	public EndpointProxy getTarget() {
		return this.endpointProxy;
	}

	public void setComplete() {
		this.nofityReturn();
	}

	public LinkException getError() {
		return this.error;
	}

	public void setError(LinkException error) {
		this.error = error;
		this.nofityReturn();
	}

	public Object getReturn() {
		return this._return;
	}

	public void setReturn(Object _return) {
		this._return = _return;
		this.nofityReturn();
	}

	public void waitReturn(int timeoutSecond) throws LinkException {
		int i = 0, wait = 10;
		while (true) {
			if (this.error != null || this._return != null)
				return;

			if (timeoutSecond > 0 && (i++) * wait >= timeoutSecond)
				throw new LinkException("execution timeout");

			synchronized (this.sync) {
				try {
					this.sync.wait(wait);
				} catch (InterruptedException e) {
					throw new LinkException("uknown error", e);
				}
			}
		}
	}

	private void nofityReturn() {
		synchronized (this.sync) {
			this.sync.notifyAll();
		}
	}
}
