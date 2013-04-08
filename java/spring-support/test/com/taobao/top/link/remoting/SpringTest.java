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
			+ "	<bean name=\"test\" class=\"com.taobao.top.link.remoting.SpringServiceProxyBean\">"
			+ "		<property name=\"interfaceName\" value=\"com.taobao.top.link.remoting.TestInterface\" />"
			+ "		<property name=\"uri\" value=\"ws://localhost:8889/api\" />"
			+ "	</bean>"

			+ "	<bean name=\"testService\" class=\"com.taobao.top.link.remoting.TestService\" />"

			+ "	<bean class=\"com.taobao.top.link.remoting.ServiceBean\">"
			+ "		<property name=\"interfaceName\" value=\"com.taobao.top.link.remoting.TestInterface\" />"
			+ "		<property name=\"target\">"
			+ "			<ref bean=\"testService\" />"
			+ "		</property>"
			+ "	</bean>"
			+ "</beans>";

	private static ListableBeanFactory beanFactory;

	@BeforeClass
	public static void init() {
		beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
		reader.loadBeanDefinitions(new ByteArrayResource(beansXml.getBytes()));
		
		RemotingConfiguration.
				configure().
				websocket(8889).
				addProcessor("api",
						new SpringMethodCallProcessor(beanFactory));
	}

	@Test
	public void get_class_test() throws ClassNotFoundException {
		Class.forName("com.taobao.top.link.remoting.TestInterface");
		Class.forName("com.taobao.top.link.remoting.TestService");
	}

	@Test
	public void bean_factory_test() throws ClassNotFoundException {
		assertEquals(1, beanFactory.getBeanNamesForType(ServiceBean.class).length);
	}

	@Test
	public void get_proxy_test() {
		Object proxy = beanFactory.getBean("test");
		assertNotNull(proxy);
		assertNotNull((TestInterface) proxy);
	}

	@Test
	public void invoke_proxy_test() {
		TestInterface testInterface = (TestInterface) beanFactory.getBean("test");
		assertEquals("hi", testInterface.echo("hi"));
	}
}
