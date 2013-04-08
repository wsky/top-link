package com.taobao.top.link.remoting;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringServerBean implements InitializingBean, BeanFactoryAware, ApplicationContextAware {
	private int port;
	private String path;
	private ListableBeanFactory beanFactory;

	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}

	public void setPath(String path) {
		this.path = path;
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
		RemotingConfiguration.
				configure().
				websocket(this.port).
				addProcessor(this.path, new SpringMethodCallProcessor(this.beanFactory));
	}
}
