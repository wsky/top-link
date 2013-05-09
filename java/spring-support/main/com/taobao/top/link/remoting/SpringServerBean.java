package com.taobao.top.link.remoting;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	private int maxThreadCount = 200;

	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setMaxMessageSize(String maxMessageSize) {
		this.maxMessageSize = Integer.parseInt(maxMessageSize);
	}

	public void setMaxBusinessThreadCount(String maxThreadCount) {
		this.maxThreadCount = Integer.parseInt(maxThreadCount);
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
				loggerFactory(Util.getLoggerFactory(this)).
				websocket(this.port).
				addProcessor(this.path, new SpringMethodCallProcessor(this.beanFactory)).
				businessThreadPool(new ThreadPoolExecutor(20,
						this.maxThreadCount,
						300,
						TimeUnit.SECONDS,
						new SynchronousQueue<Runnable>()));
	}
}
