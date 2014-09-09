package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;

import com.taobao.top.link.channel.websocket.WebSocketServerChannelSender;

public class SpringTest {
	private static String beansXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
			+ "<beans>"

			+ "	<bean name=\"headers\" class=\"com.taobao.top.link.remoting.HandshakingHeadersBean\">"
			+ "		<property name=\"headers\">"
			+ "			<map>"
			+ "				<entry key=\"id\"><value>test</value></entry>"
			+ "			</map>"
			+ "		</property>"
			+ "	</bean>"

			+ "	<bean name=\"test\" class=\"com.taobao.top.link.remoting.SpringServiceProxyBean\">"
			+ "		<property name=\"interfaceName\" value=\"com.taobao.top.link.remoting.SampleInterface\" />"
			+ "		<property name=\"uri\" value=\"ws://localhost:8889/api\" />"
			+ "		<property name=\"executionTimeout\" value=\"5000\" />"
			+ "		<property name=\"serialization\" value=\"json\" />"
			+ "		<property name=\"headers\">"
			+ "			<ref bean=\"headers\" />"
			+ "		</property>"
			+ "	</bean>"

			+ "	<bean name=\"sampleService\" class=\"com.taobao.top.link.remoting.SampleService\" />"

			+ "	<bean name=\"handshaker\" class=\"com.taobao.top.link.remoting.CustomHandshaker\" />"
			+ "	<bean name=\"callContext\" class=\"com.taobao.top.link.remoting.MethodCallContextBean\" />"

			+ "	<bean name=\"server\" class=\"com.taobao.top.link.remoting.SpringServerBean\">"
			+ "		<property name=\"port\" value=\"8889\" />"
			+ "		<property name=\"path\" value=\"api\" />"
			+ "		<property name=\"maxMessageSize\" value=\"1024\" />"
			+ "		<property name=\"minBusinessThreadCount\" value=\"20\" />"
			+ "		<property name=\"maxBusinessThreadCount\" value=\"200\" />"
			+ "		<property name=\"handshaker\">"
			+ "			<ref bean=\"handshaker\" />"
			+ "		</property>"
			+ "</bean>"

			+ "	<bean class=\"com.taobao.top.link.remoting.ServiceBean\">"
			+ "		<property name=\"interfaceName\" value=\"com.taobao.top.link.remoting.SampleInterface\" />"
			+ "		<property name=\"target\">"
			+ "			<ref bean=\"sampleService\" />"
			+ "		</property>"
			+ "	</bean>"
			+ "</beans>";

	private static ListableBeanFactory beanFactory;

	@BeforeClass
	public static void init() {
		beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
		reader.loadBeanDefinitions(new ByteArrayResource(beansXml.getBytes()));
		beanFactory.getBean("server");
	}

	@AfterClass
	public static void clear() {
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(new DefaultRemotingServerChannelHandler());
	}

	@Test
	public void get_class_test() throws ClassNotFoundException {
		Class.forName("com.taobao.top.link.remoting.SampleInterface");
		Class.forName("com.taobao.top.link.remoting.SampleService");
	}

	@Test
	public void bean_factory_test() throws ClassNotFoundException {
		assertEquals(1, beanFactory.getBeanNamesForType(ServiceBean.class).length);
	}

	@Test
	public void get_proxy_test() {
		Object proxy = beanFactory.getBean("test");
		assertNotNull(proxy);
		assertNotNull((SampleInterface) proxy);
	}

	@Test
	public void invoke_proxy_test() {
		SampleInterface sampleInterface = (SampleInterface) beanFactory.getBean("test");
		assertEquals("hi", sampleInterface.echo("hi"));
	}

	@Test
	public void call_context_bean_test() {
		assertNotNull((MethodCallContextBean) beanFactory.getBean("callContext"));
	}

	@Test
	public void context_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		final MethodCallContextBean bean = new MethodCallContextBean();

		MethodCallContext callContext = new MethodCallContext(null);
		callContext.setCallContext("key", new Object());

		MethodCallContextBean.setCurrentContext(callContext);
		assertNotNull(bean.get("key"));

		new Thread(new Runnable() {
			public void run() {
				MethodCallContextBean.setCurrentContext(
						new MethodCallContext(new WebSocketServerChannelSender(null)));
				assertNull(bean.get("key"));
				latch.countDown();
			}
		}).start();

		latch.await();
		assertNotNull(bean.get("key"));
	}
}
