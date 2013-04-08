package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;

public class DynamicProxyBeanTest {
	private static String beansXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">"
			+ "<beans>"
			+ "	<bean name=\"test\" class=\"com.taobao.top.link.remoting.DynamicProxyBean\">"
			+ "		<property name=\"interfaceName\" value=\"com.taobao.top.link.remoting.TestInterface\" />"
			+ "		<property name=\"uri\" value=\"ws://localhost:8889/test\" />"
			+ "	</bean>"
			+ "</beans>";

	private static BeanFactory beanFactory;

	@BeforeClass
	public static void init() {
		beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
		reader.loadBeanDefinitions(new ByteArrayResource(beansXml.getBytes()));
		
		RemotingConfiguration.configure().
			websocket(8889).
			addProcessor("test", new TestService());
	}
	
	@Test
	public void get_class_test() throws ClassNotFoundException {
		Class.forName("com.taobao.top.link.remoting.TestInterface");
		Class.forName("com.taobao.top.link.remoting.TestService");
	}

	@Test
	public void get_test() {
		Object proxy = beanFactory.getBean("test");
		assertNotNull(proxy);
		assertNotNull((TestInterface) proxy);
	}
	
	@Test
	public void invoke_test() {
		TestInterface testInterface = (TestInterface) beanFactory.getBean("test");
		assertEquals("hi", testInterface.echo("hi"));
	}
}
