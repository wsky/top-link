package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.FactoryBean;

// easy support spring bean
public class DynamicProxyBean implements FactoryBean {

	private URI uri;
	private Class<?> interfaceType;

	public void setInterfaceName(String interfaceName) throws ClassNotFoundException {
		this.interfaceType = Class.forName(interfaceName);
	}

	public void setUri(String uri) throws URISyntaxException {
		this.uri = new URI(uri);
	}

	@Override
	public Object getObject() throws Exception {
		return RemotingService.connect(this.uri, this.interfaceType);
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
