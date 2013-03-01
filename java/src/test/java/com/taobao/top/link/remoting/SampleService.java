package com.taobao.top.link.remoting;

public class SampleService implements SampleServiceInterface {
	@Override
	public String echo(String input) {
		return input;
	}
}
