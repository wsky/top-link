package top.link.remoting.spring;

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

import top.link.BufferManager;
import top.link.remoting.RemotingConfiguration;
import top.link.remoting.serialization.SerializerUtil;

public class SpringServerBean implements InitializingBean, BeanFactoryAware, ApplicationContextAware {
	private ListableBeanFactory beanFactory;
	private int port;
	private String path;
	private int maxMessageSize;
	private int minThreadCount = 20;
	private int maxThreadCount = 200;
	private boolean disableBusinessThread;
	private HandshakerBean handshaker;

	public void setPort(int value) {
		this.port = value;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setMaxMessageSize(int value) {
		this.maxMessageSize = value;
	}

	public void setMinBusinessThreadCount(int value) {
		this.minThreadCount = value;
	}

	public void setMaxBusinessThreadCount(int value) {
		this.maxThreadCount = value;
	}

	public void setDisableBusinessThread(boolean value) {
		this.disableBusinessThread = value;
	}

	public void setHandshaker(HandshakerBean handshaker) {
		this.handshaker = handshaker;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.beanFactory = applicationContext;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.maxMessageSize > 0)
			BufferManager.setBufferSize(this.maxMessageSize);

		RemotingConfiguration.
				configure().
				SerializationFactory(SerializerUtil.getSerializationFactory(this)).
				defaultServerChannelHandler(new SpringRemotingServerChannelHandler(this.handshaker)).
				websocket(this.port).
				addProcessor(this.path, new SpringMethodCallProcessor(this.beanFactory)).
				businessThreadPool(this.disableBusinessThread ? null : new ThreadPoolExecutor(
						this.minThreadCount,
						this.maxThreadCount,
						300, TimeUnit.SECONDS,
						new SynchronousQueue<Runnable>()));
	}
}
