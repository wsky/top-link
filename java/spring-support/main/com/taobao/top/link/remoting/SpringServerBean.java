package com.taobao.top.link.remoting;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.taobao.top.link.BufferManager;

public class SpringServerBean implements InitializingBean, BeanFactoryAware, ApplicationContextAware {
	private ListableBeanFactory beanFactory;
	private int port;
	private String path;
	private int maxMessageSize;

	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setMaxMessageSize(String maxMessageSize) {
		this.maxMessageSize = Integer.parseInt(maxMessageSize);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.beanFactory = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.maxMessageSize > 0)
			BufferManager.setBufferSize(this.maxMessageSize);
		
		RemotingConfiguration.
				configure().
				websocket(this.port).
				addProcessor(this.path, new SpringMethodCallProcessor(this.beanFactory));
	}
}
