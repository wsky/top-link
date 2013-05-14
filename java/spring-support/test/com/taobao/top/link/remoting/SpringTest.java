package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;

public class SpringTest {
	private static String beansXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
			+ "<beans>"
			
			+ "	<bean name=\"headers\" class=\"com.taobao.top.link.remoting.HandshakingHeadersBean\">"
			+ "		<property name=\"uri\" value=\"ws://localhost:8889/api\" />"
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
			+ "		<property name=\"headers\">"
			+ "			<ref bean=\"headers\" />"
			+ "		</property>"
			+ "	</bean>"

			+ "	<bean name=\"sampleService\" class=\"com.taobao.top.link.remoting.SampleService\" />"

			+ "	<bean name=\"customServerHandler\" class=\"com.taobao.top.link.remoting.CustomServerChannelHandler\" />"

			+ "	<bean name=\"server\" class=\"com.taobao.top.link.remoting.SpringServerBean\">"
			+ "		<property name=\"port\" value=\"8889\" />"
			+ "		<property name=\"path\" value=\"api\" />"
			+ "		<property name=\"maxMessageSize\" value=\"1024\" />"
			+ "		<property name=\"maxBusinessThreadCount\" value=\"200\" />"
			+ "		<property name=\"serverHandler\">"
			+ "			<ref bean=\"customServerHandler\" />"
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
}
