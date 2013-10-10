package com.taobao.top.link;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// https://gist.github.com/wsky/5538632
// easy timer task, support delay and reset
public class ResetableTimer {
	private volatile boolean running;
	private Thread boss;
	private ExecutorService threadPool;

	private Runnable task;
	private int period;
	protected long lastTime;

	public ResetableTimer(int periodMillisecond) {
		this(periodMillisecond, null);
	}

	public ResetableTimer(int periodMillisecond, Runnable task) {
		this.period = periodMillisecond;
		this.delay(0 - this.period);
		this.setTask(task);
		this.threadPool = Executors.newSingleThreadExecutor();
	}

	public void setTask(Runnable task) {
		this.task = task;
	}

	public void start() {
		if (this.boss != null)
			return;
		this.running = true;
		this.boss = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					long split = System.currentTimeMillis() - lastTime;
					if (split >= period && task != null) {
						try {
							threadPool.execute(task);
						} catch (Exception e) {
							e.printStackTrace();
						}
						delay();
					}
					try {
						Thread.sleep(split >= period ? period : period - split);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		this.boss.start();
	}

	public void stop() throws InterruptedException {
		this.running = false;
		this.boss.join();
		this.boss = null;
		this.threadPool.shutdown();
	}

	public void delay() {
		this.delay(0);
	}

	public void delay(int delayMillisecond) {
		this.lastTime = System.currentTimeMillis() + delayMillisecond;
	}
}
