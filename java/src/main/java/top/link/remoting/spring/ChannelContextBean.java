package top.link.remoting.spring;

public interface ChannelContextBean {
	public Object get(Object key);
	public void set(Object key, Object value);
}
