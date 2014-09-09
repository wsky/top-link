package top.link.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerChannel {
	protected Logger logger;
	protected ChannelHandler channelHandler;
	protected int port;
	protected int maxIdleTimeSeconds = 0;
	
	public ServerChannel(int port) {
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.port = port;
	}
	
	public void setMaxIdleTimeSeconds(int value) {
		this.maxIdleTimeSeconds = value;
	}
	
	public void setChannelHandler(ChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}
	
	public abstract void run();
	public abstract void stop();
}
