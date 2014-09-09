package top.link.netcat;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import top.link.netcat.CommandProcessor;
import top.link.netcat.NetCatCommandServerChannelHandler;
import top.link.netcat.NetCatOuputWriter;
import top.link.netcat.netty.NettyNetCatCommandServerChannel;

public class CommandTest {
	@Test
	public void parse_test() {
		String[] arr = new String[] {
				"cmd",
				"-a", "abc",
				"-b", "2",
				"-c", "2",
				"d"
		};
		Map<String, String> input = new NetCatCommandServerChannelHandler().parseInput(arr, 1);
		assertEquals(arr[2], input.get("a"));
		assertEquals(arr[4], input.get("b"));
		assertEquals(arr[6], input.get("c"));
		assertNull(input.get("d"));
	}

	public static void main(String[] args) {
		NettyNetCatCommandServerChannel serverChannel = new NettyNetCatCommandServerChannel(8888);
		serverChannel.addProcessor(new CommandProcessor() {
			public void process(Map<String, String> input, NetCatOuputWriter writer) {
				if (input.containsKey("error"))
					throw new NullPointerException("error");
				writer.write(input.toString());
			}

			public String getName() {
				return "echo";
			}
		});
		serverChannel.run();
		// echo "echo -i 1 -a abc"|nc localhost 8888
	}
}
