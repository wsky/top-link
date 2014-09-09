package top.link.remoting;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import top.link.channel.ClientChannelPooledSelector;
import top.link.channel.ClientChannelSelector;

public class RemotingService {
	private static ClientChannelSelector channelSelector;
	private static RemotingClientChannelHandler channelHandler;
	private static SerializationFactory serializationFactory;
	
	protected static void setChannelSelector(ClientChannelSelector selector) {
		channelSelector = selector;
	}
	
	protected static void setSerializationFactory(SerializationFactory serializationFactory) {
		RemotingService.serializationFactory = serializationFactory;
	}
	
	public static Object connect(URI remoteUri, Class<?> interfaceClass) {
		return connect(remoteUri, interfaceClass, null);
	}
	
	public static Object connect(URI remoteUri, Class<?> interfaceClass, String serializationFormat) {
		DynamicProxy proxy = connect(remoteUri);
		proxy.setSerializationFormat(serializationFormat);
		return proxy.create(interfaceClass, remoteUri);
	}
	
	public static DynamicProxy connect(URI remoteUri) {
		return new DynamicProxy(remoteUri, getChannelSelector(), getChannelHandler());
	}
	
	private synchronized static RemotingClientChannelHandler getChannelHandler() {
		if (channelHandler == null)
			channelHandler = new RemotingClientChannelHandler(new AtomicInteger(0));
		if (serializationFactory != null)
			channelHandler.setSerializationFactory(serializationFactory);
		return channelHandler;
	}
	
	private synchronized static ClientChannelSelector getChannelSelector() {
		if (channelSelector == null)
			channelSelector = new ClientChannelPooledSelector();
		return channelSelector;
	}
}