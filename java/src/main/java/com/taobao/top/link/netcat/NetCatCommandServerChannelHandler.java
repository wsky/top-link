package com.taobao.top.link.netcat;

import java.util.HashMap;
import java.util.Map;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.SimpleChannelHandler;
import com.taobao.top.link.logging.LogUtil;

public class NetCatCommandServerChannelHandler extends SimpleChannelHandler {
	private Logger logger;
	private Map<String, CommandProcessor> processors;

	public NetCatCommandServerChannelHandler() {
		this(LogUtil.getLoggerFactory(new Object()));
	}

	public NetCatCommandServerChannelHandler(LoggerFactory loggerFactory) {
		this.logger = loggerFactory.create(this);
		this.processors = new HashMap<String, CommandProcessor>();
	}

	public void addProcessor(CommandProcessor processor) {
		this.processors.put(processor.getName(), processor);
	}

	@Override
	public void onMessage(final ChannelContext context) {
		String line = (String) context.getMessage();

		if (this.logger.isDebugEnabled())
			this.logger.debug("command: ", line);

		if (line == null)
			return;

		String[] arr = line.trim().split(" ");

		if (arr.length == 0)
			return;
		CommandProcessor processor = this.processors.get(arr[0]);
		if (processor == null) {
			this.logger.warn("processor not found for command: %s", arr[0]);
			return;
		}

		NetCatOuputWriter writer = new NetCatOuputWriter() {
			@Override
			public void write(String value) {
				byte[] data = (value + "\n").getBytes();
				try {
					context.reply(data, 0, data.length);
				} catch (ChannelException e) {
					logger.error(e);
				}
			}
		};
		
		try {
			processor.process(this.parseInput(arr, 1), writer);
		} catch (Exception e) {
			writer.write(e.getMessage());
		}
	}

	protected Map<String, String> parseInput(String[] input, int from) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = from; i < input.length; i += 2) {
			if (i >= input.length)
				break;
			map.put(input[i].startsWith("-") ? input[i].substring(1) : input[i],
					i + 1 < input.length ? input[i + 1] : null);
		}
		return map;
	}
}
