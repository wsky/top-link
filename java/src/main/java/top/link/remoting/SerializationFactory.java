package top.link.remoting;

public interface SerializationFactory {
	public Serializer get(Object format);
}