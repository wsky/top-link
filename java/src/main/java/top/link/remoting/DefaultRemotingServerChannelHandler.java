package top.link.remoting;

import java.net.URI;
import java.util.HashMap;

import top.link.DefaultLoggerFactory;
import top.link.LoggerFactory;

public class DefaultRemotingServerChannelHandler extends RemotingServerChannelHandler {
	private HashMap<String, MethodCallProcessor> services;

	public DefaultRemotingServerChannelHandler() {
		super();
		this.services = new HashMap<String, MethodCallProcessor>();
	}

	public final void addProcessor(String objectUri, MethodCallProcessor processor) {
		this.services.put("/" + objectUri.toLowerCase(), processor);
	}

	@Override
	public final MethodReturn onMethodCall(MethodCall methodCall, MethodCallContext callContext) throws Throwable {
		// dispatch methodCall to service
		String objectUri = new URI(methodCall.Uri).getRawPath().trim();
		MethodCallProcessor processor = this.services.get(objectUri);
		if (processor == null)
			throw new NullPointerException(String.format(
					"processor not found for objectUri: %s", objectUri));
		return processor.process(methodCall, callContext);
	}
}
