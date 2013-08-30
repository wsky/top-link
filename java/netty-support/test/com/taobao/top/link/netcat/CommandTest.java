package com.taobao.top.link.netcat;

import static org.junit.Assert.*;

import java.util.Map;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.junit.Test;

import com.taobao.top.link.channel.tcp.TcpServerChannel;

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
		TcpServerChannel serverChannel = new TcpServerChannel(8888) {
			@Override
			protected void prepareCodec(ChannelPipeline pipeline) {
				pipeline.addLast("decoder", new StringDecoder());
			}
		};
		NetCatCommandServerChannelHandler handler = new NetCatCommandServerChannelHandler();
		handler.addProcessor(new CommandProcessor() {
			@Override
			public void process(Map<String, String> input, NetCatOuputWriter writer) {
				writer.write(input.toString());
			}

			@Override
			public String getName() {
				return "echo";
			}
		});
		serverChannel.setChannelHandler(handler);
		serverChannel.run();
		// echo "echo -i 1 -a abc"|nc localhost 8888
	}
}
