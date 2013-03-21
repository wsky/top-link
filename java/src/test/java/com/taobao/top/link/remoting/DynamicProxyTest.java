package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

// high-level abstract remoting test
public class DynamicProxyTest {
	@Test
	public void dynamicProxy_test() throws Throwable {
		String uriString = "ws://localhost:9020/";
		URI uri = new URI(uriString);
		this.runDefaultServer(uri);

		// java.lang.IllegalArgumentException:
		// com.taobao.top.link.remoting.DynamicProxyTest$SampleService is not an
		// interface
		// SampleService sampleService = (SampleService)
		// RemotingService.connect(uri, SampleService.class);
		URI remoteUri = new URI(uriString + "sample");
		SampleServiceInterface sampleService = (SampleServiceInterface)
				RemotingService.connect(remoteUri, SampleServiceInterface.class);
		assertEquals("hi", sampleService.echo("hi"));
	}

	@Test(expected = NullPointerException.class)
	public void objectUri_empty_or_not_matched_processor_test() throws Throwable {
		String uriString = "ws://localhost:9021/";
		URI uri = new URI(uriString);
		this.runDefaultServer(uri);

		URI remoteUri = new URI(uriString + "sample_wrong");
		SampleServiceInterface sampleService = (SampleServiceInterface)
				RemotingService.connect(remoteUri, SampleServiceInterface.class);
		try {
			sampleService.echo("hi");
		} catch (Exception e) {
			assertEquals("processor not found for objectUri: /sample_wrong", e.getMessage());
			throw e;
		}
	}

	@Test(expected = RemotingException.class)
	public void invoke_throw_not_UndeclaredThrowable_test() throws RemotingException, Exception {
		URI uri = new URI("ws://localhost:9022/sample");
		this.runDefaultServer(uri);

		DynamicProxy proxy = RemotingService.connect(uri);
		MethodCall methodCall = new MethodCall();
		methodCall.Args = new Object[] { "hi" };
		MethodReturn methodReturn = proxy.invoke(methodCall);
		throw new RemotingException("", methodReturn.Exception);
	}

	private RemotingConfiguration runDefaultServer(URI uri) {
		return RemotingConfiguration.
				configure().
				websocket(uri.getPort()).
				addProcessor("sample", new SampleService());
	}

	public interface SampleServiceInterface {
		public String echo(String input);
	}

	public class SampleService extends DefaultMethodCallProcessor implements SampleServiceInterface {
		@Override
		public String echo(String input) {
			return input;
		}
	}
}
