package com.taobao.top.link.endpoint;

import java.util.HashMap;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Text;

public class SendCallback {
	private Object sync = new Object();
	private EndpointProxy endpointProxy;
	private LinkException error;
	private HashMap<String, String> _return;
	private boolean isComplete;

	public SendCallback(EndpointProxy endpointProxy) {
		this.endpointProxy = endpointProxy;
	}

	public EndpointProxy getTarget() {
		return this.endpointProxy;
	}

	public void setComplete() {
		this.isComplete = true;
		this.nofityReturn();
	}

	public LinkException getError() {
		return this.error;
	}

	public void setError(LinkException error) {
		this.error = error;
		this.setComplete();
	}

	public HashMap<String, String> getReturn() {
		return this._return;
	}

	public void setReturn(HashMap<String, String> _return) {
		this._return = _return;
		this.setComplete();
	}

	public void waitReturn(int timeout) throws LinkException {
		int i = 0, wait = 10;
		while (true) {
			if (this.isComplete)
				return;

			if (timeout > 0 && (i++) * wait >= timeout)
				throw new LinkException(Text.E_EXECUTE_TIMEOUT);

			synchronized (this.sync) {
				try {
					this.sync.wait(wait);
				} catch (InterruptedException e) {
					throw new LinkException(Text.E_UNKNOWN_ERROR, e);
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
