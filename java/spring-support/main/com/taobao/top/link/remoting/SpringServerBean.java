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
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;

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
				loggerFactory(this.getLoggerFactory()).
				websocket(this.port).
				addProcessor(this.path, new SpringMethodCallProcessor(this.beanFactory)).
				businessThreadPool(new ThreadPoolExecutor(20,
						this.maxThreadCount,
						300,
						TimeUnit.SECONDS,
						new SynchronousQueue<Runnable>()));
	}

	private LoggerFactory getLoggerFactory() {
		LoggerFactory loggerFactory = null;

		try {
			Class.forName("org.apache.log4j.LogManager", 
					false, this.getClass().getClassLoader());
			loggerFactory = new Log4jLoggerFactory();
		} catch (ClassNotFoundException e) {
		}
		
		if (loggerFactory == null) {
			try {
				Class.forName("org.apache.commons.logging.Log", 
						false, this.getClass().getClassLoader());
				loggerFactory = new Log4jLoggerFactory();
			} catch (ClassNotFoundException e) {
			}
		}
		
		return loggerFactory == null ? 
				DefaultLoggerFactory.getDefault() : loggerFactory;
	}
}
