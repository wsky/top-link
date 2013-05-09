package com.taobao.top.link.remoting;

public class SampleService extends DefaultMethodCallProcessor implements SampleInterface {
	@Override
	public String echo(String input) {
		return input;
	}
}
