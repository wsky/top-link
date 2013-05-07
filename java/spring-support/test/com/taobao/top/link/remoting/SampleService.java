package com.taobao.top.link.remoting;

public class SampleService implements SampleInterface {
	@Override
	public String echo(String input) {
		return input;
	}
}
