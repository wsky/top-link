package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.HashMap;

public class DefaultRemotingServerChannelHandler extends RemotingServerChannelHandler {
	@Override
	public MethodReturn onMethodCall(MethodCall methodCall) throws Throwable {
		// dispatch methodCall to service
		String objectUri = new URI(methodCall.Uri).getRawPath().trim();
		MethodCallProcessor processor = this.services.get(objectUri);
		if (processor == null)
			throw new NullPointerException(String.format(
					"processor not found for objectUri: %s", objectUri));
		return processor.process(methodCall);
	}

	private HashMap<String, MethodCallProcessor> services;

	public DefaultRemotingServerChannelHandler() {
		this.services = new HashMap<String, MethodCallProcessor>();
	}

	public void addProcessor(String objectUri, MethodCallProcessor processor) {
		this.services.put("/" + objectUri.toLowerCase(), processor);
	}
}
