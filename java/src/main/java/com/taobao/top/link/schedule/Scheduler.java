package com.taobao.top.link.schedule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Text;

public class Scheduler<T> {
	private int max = 100;
	private Logger logger;
	private Object lock;
	private Semaphore semaphore;
	private Thread dispatcher;
	private boolean running;

	private Map<T, Queue<Runnable>> tasks;
	private ExecutorService threadPool;

	public Scheduler() {
		this(DefaultLoggerFactory.getDefault());
	}

	public Scheduler(LoggerFactory loggerFactory) {
		this.logger = loggerFactory.create(this);
		this.lock = new Object();
		this.semaphore = new Semaphore(0);
		this.tasks = new HashMap<T, Queue<Runnable>>();
		this.setThreadPool(Executors.newCachedThreadPool());
	}

	public void setUserMaxPendingCount(int max) {
		this.max = max;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public void start() {
		if (this.dispatcher != null)
			return;

		this.running = true;
		this.dispatcher = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					try {
						semaphore.tryAcquire(1, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						if (logger.isDebugEnabled())
							logger.debug(e);
					}
					dispatch();
				}
			}
		});
		this.dispatcher.start();
	}

	public void stop() throws InterruptedException {
		if (this.dispatcher == null)
			return;
		this.running = false;
		this.semaphore.release();
		this.dispatcher.join();
	}

	public void schedule(T t, Runnable task) throws LinkException {
		if (this.canRunImmediately(t, task)) {
			try {
				this.threadPool.execute(task);
				return;
			} catch (RejectedExecutionException e) {
			}
		}

		Queue<Runnable> queue = this.tasks.get(t);
		if (queue == null) {
			synchronized (this.lock) {
				if ((queue = this.tasks.get(t)) == null)
					this.tasks.put(t, queue = new ConcurrentLinkedQueue<Runnable>());
			}
		}

		if (queue.size() >= this.max)
			throw new LinkException(String.format(Text.SCHEDULE_GOT_MAX, this.max));

		try {
			queue.add(task);
		} catch (Exception e) {
			throw new LinkException(Text.SCHEDULE_TASK_REFUSED, e);
		}

		// if (this.semaphore.getQueueLength() > 0)
		this.semaphore.release();
	}

	public void drop(T t) {
		if (this.tasks.get(t) == null)
			return;
		this.tasks.get(t).clear();
		this.tasks.remove(t);
	}

	protected boolean canRunImmediately(T t, Runnable task) {
		return false;
	}

	private void dispatch() {
		boolean flag;
		int c = 0;
		do {
			flag = false;
			Iterator<Entry<T, Queue<Runnable>>> iterator = this.tasks.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<T, Queue<Runnable>> entry;
				try {
					entry = iterator.next();
				} catch (Exception e) {
					if (this.logger.isDebugEnabled())
						this.logger.debug(e);
					// rerun while java.util.ConcurrentModificationException
					flag = true;
					break;
				}
				if (entry == null)
					continue;

				Queue<Runnable> queue = entry.getValue();
				if (queue == null)
					continue;

				Runnable task = queue.peek();

				flag = flag ? flag : (queue.size() - 1 > 0);

				if (task == null)
					continue;

				try {
					this.threadPool.execute(task);
					queue.poll();
					c++;
				} catch (RejectedExecutionException e) {
					if (this.logger.isDebugEnabled())
						this.logger.debug(e);
					break;
				}
			}
		} while (flag);

		if (this.logger.isDebugEnabled() && c > 0)
			this.logger.debug("dispatch %s tasks", c);
	}
}
