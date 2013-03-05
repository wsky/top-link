package com.taobao.top.link;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

// easy buffer pool
public class BufferManager {
	private static ConcurrentLinkedQueue<ByteBuffer> buffers = new ConcurrentLinkedQueue<ByteBuffer>();

	public static ByteBuffer getBuffer() {
		ByteBuffer buffer = buffers.poll();
		// TODO:change fixed capacity
		return buffer == null ? ByteBuffer.allocateDirect(1024 * 4) : buffer;
	}

	public static void returnBuffer(ByteBuffer buffer) {
		buffer.clear();
		buffers.add(buffer);
	}
}
