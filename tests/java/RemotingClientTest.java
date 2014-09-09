import java.net.URI;

import top.link.remoting.RemotingService;
import top.link.remoting.DynamicProxy;
import top.link.remoting.MethodCall;

public class RemotingClientTest {
	public static void main(String[] args) throws Throwable {
		String host = args.length > 0 ? args[0] : "localhost";
		URI uri = new URI("ws://"+ host +":9000/");
		DynamicProxy proxy = RemotingService.connect(uri);

		long total = 100000;
		long begin = System.currentTimeMillis();
		for (int i = 0; i < total; i++) {
			proxy.invoke(new MethodCall());
		}
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms", total, cost,
				((float) total / (float) cost) * 1000,
				(float) cost / (float) total));
	}
}