package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.Map;

import com.taobao.top.link.channel.websocket.WebSocketClient;

public class HandshakingHeadersBean {
	
	private Map<String, String> headers;

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	public void setUri(URI uri) {
		WebSocketClient.setHeaders(uri, this.headers);
	}
}
