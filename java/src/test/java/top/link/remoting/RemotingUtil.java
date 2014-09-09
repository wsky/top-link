package top.link.remoting;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import top.link.channel.ClientChannelPooledSelector;
import top.link.channel.ClientChannelSelector;
import top.link.remoting.DynamicProxy;
import top.link.remoting.RemotingClientChannelHandler;

public class RemotingUtil {
	public static DynamicProxy connect(URI uri) {
		return connect(uri, new ClientChannelPooledSelector());
	}

	public static DynamicProxy connect(URI uri, ClientChannelSelector clientChannelSelector) {
		RemotingClientChannelHandler clientHandler = new RemotingClientChannelHandler(new AtomicInteger(0));
		return new DynamicProxy(uri, clientChannelSelector, clientHandler);
	}

	public static Object connect(URI uri, Class<?> serviceType) {
		return connect(uri).create(serviceType, uri);
	}

	public static Object connect(URI uri, Class<?> serviceType, ClientChannelSelector clientChannelSelector) {
		return connect(uri, clientChannelSelector).create(serviceType, uri);
	}
}
