import java.net.URI;

import com.taobao.top.link.remoting.RemotingService;
import com.taobao.top.link.remoting.DynamicProxy;
import com.taobao.top.link.remoting.MethodCall;

public class RemotingClientTest {
	public static void main(String[] args) throws Throwable {
		URI uri = new URI("ws://localhost:9000/");
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