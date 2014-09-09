package top.link.remoting;

import top.link.remoting.DefaultMethodCallProcessor;

public class SampleService extends DefaultMethodCallProcessor implements SampleInterface {
	public String echo(String input) {
		return input;
	}
}
