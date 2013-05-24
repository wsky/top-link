package com.taobao.top.link.remoting;

import java.util.HashMap;

import org.springframework.beans.factory.ListableBeanFactory;

public class SpringMethodCallProcessor implements MethodCallProcessor {
	private HashMap<String, Object> services;

	public SpringMethodCallProcessor(ListableBeanFactory beanFactory) {
		this.services = new HashMap<String, Object>();
		this.readServices(beanFactory);
	}

	@Override
	public MethodReturn process(MethodCall methodCall, MethodCallContext callContext) throws Throwable {
		Object target = services.get(methodCall.TypeName);
		MethodReturn methodReturn = new MethodReturn();
		methodReturn.ReturnValue = target.getClass()
				.getMethod(methodCall.MethodName, methodCall.MethodSignature)
				.invoke(target, methodCall.Args);
		return methodReturn;
	}

	public void register(String serviceInterface, Object serviceObject) {
		this.services.put(serviceInterface, serviceObject);
	}

	private void readServices(ListableBeanFactory beanFactory) {
		String[] names = beanFactory.getBeanNamesForType(ServiceBean.class);
		for (String n : names) {
			ServiceBean s = (ServiceBean) beanFactory.getBean(n);
			this.register(s.getInterfaceName(), s.getTarget());
		}
	}
}
