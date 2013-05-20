package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.FactoryBean;

import com.taobao.top.link.BufferManager;

// easy support spring bean
public class SpringServiceProxyBean implements FactoryBean {

	private URI uri;
	private Class<?> interfaceType;
	private int executionTimeout;

	public void setInterfaceName(String interfaceName) throws ClassNotFoundException {
		this.interfaceType = Class.forName(interfaceName);
	}

	public void setUri(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
	}

	public void setExecutionTimeout(String executionTimeout) {
		this.executionTimeout = Integer.parseInt(executionTimeout);
	}

	public void setHeaders(HandshakingHeadersBean headersBean) {
		headersBean.setUri(this.uri);
	}

	@Override
	public Object getObject() throws Exception {
		// TODO:find better way to init
		RemotingConfiguration.configure().
				loggerFactory(Util.getLoggerFactory(this)).
				serializer(new CrossLanguageJsonSerializer());
		// default set 2M max message size for client
		// TODO:change to growing buffer
		BufferManager.setBufferSize(1024 * 1024 * 2);
		DynamicProxy proxy = RemotingService.connect(this.uri);
		if (this.executionTimeout > 0)
			proxy.setExecutionTimeout(this.executionTimeout);
		return proxy.create(this.interfaceType, this.uri);
	}

	@Override
	public Class<?> getObjectType() {
		return this.interfaceType;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
