package com.taobao.top.link;

import java.net.URI;

public class EndpointProxyHolder {
	public EndpointProxy get(URI uri) {
		return new EndpointProxy();
	}
}
