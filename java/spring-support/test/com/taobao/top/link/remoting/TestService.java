package com.taobao.top.link.remoting;

public class TestService implements TestInterface {
	@Override
	public String echo(String input) {
		return input;
	}
}
