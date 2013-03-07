package com.taobao.top.link;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

// easy buffer pool
public class BufferManager {
	private static ConcurrentLinkedQueue<ByteBuffer> buffers = new ConcurrentLinkedQueue<ByteBuffer>();

	public static ByteBuffer getBuffer() {
		ByteBuffer buffer = buffers.poll();
		// TODO:change fixed capacity
		return buffer == null ? ByteBuffer.allocate(1024 * 4) : buffer;
		// direct buffer efficiently for netty ?
		// https://github.com/wsky/top-link/issues/12#issuecomment-14550453
		// -XX:MaxDirectMemorySize=10m -XX:+PrintGC
		// return buffer == null ? ByteBuffer.allocateDirect(1024 * 4) : buffer;
	}

	public static void returnBuffer(ByteBuffer buffer) {
		buffer.clear();
		buffers.add(buffer);
	}
}
