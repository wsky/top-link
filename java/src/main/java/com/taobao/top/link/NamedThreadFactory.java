package com.taobao.top.link;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
	static final AtomicInteger poolNumber = new AtomicInteger(1);

	final AtomicInteger threadNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final String prefix;
	final boolean isDaemon;
	final int priority;

	public NamedThreadFactory() {
		this("pool");
	}

	public NamedThreadFactory(String name) {
		this(name, false, Thread.NORM_PRIORITY);
	}

	public NamedThreadFactory(String prefix, boolean isDaemon, int priority) {
		SecurityManager s = System.getSecurityManager();
		this.group = (s != null) ? 
				s.getThreadGroup() : 
				Thread.currentThread().getThreadGroup();
		this.prefix = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
		this.isDaemon = isDaemon;
		this.priority = priority;
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, prefix + threadNumber.getAndIncrement(), 0);
		t.setDaemon(isDaemon);
		t.setPriority(priority);
		return t;
	}

}
