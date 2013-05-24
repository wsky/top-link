package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.Map;

import com.taobao.top.link.channel.websocket.WebSocketClientHelper;

public class HandshakingHeadersBean {
	
	protected Map<String, String> headers;

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	protected final void setUri(URI uri) {
		WebSocketClientHelper.setHeaders(uri, this.headers);
	}
}
