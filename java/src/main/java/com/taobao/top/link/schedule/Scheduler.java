package com.taobao.top.link.schedule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
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
	protected int max = 100;
	protected Logger logger;
	private Object lock;
	private Semaphore semaphore;
	private Thread dispatcher;
	protected boolean running;
	private Timer checker;

	private Map<T, Queue<Runnable>> tasks;
	private ExecutorService threadPool;

	public Scheduler() {
		this(DefaultLoggerFactory.getDefault());
	}

	public Scheduler(LoggerFactory loggerFactory) {
		this.logger = loggerFactory.create(this);
		this.lock = new Object();
		this.semaphore = new Semaphore(0);
		this.tasks = this.createStore();
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
		this.prepareChecker(10000, 10000);

		if (this.logger.isDebugEnabled())
			this.logger.debug(Text.SCHEDULE_START);
	}

	public void stop() throws InterruptedException {
		if (this.dispatcher == null)
			return;

		this.stopChecker();
		this.checker = null;

		this.disposeDispatcher();
		this.dispatcher = null;

		if (this.logger.isDebugEnabled())
			this.logger.debug(Text.SCHEDULE_STOP);
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
					this.tasks.put(t, queue = this.createTaskQueue(t));
			}
		}

		if (this.haveReachMaxPendingCount(t, queue, task))
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

	protected Map<T, Queue<Runnable>> createStore() {
		return new HashMap<T, Queue<Runnable>>();
	}

	protected Queue<Runnable> createTaskQueue(T t) {
		return new ArrayBlockingQueue<Runnable>(this.max, false);
	}

	protected boolean canRunImmediately(T t, Runnable task) {
		return false;
	}

	// can override here to control pending count of t
	protected boolean haveReachMaxPendingCount(T t, Queue<Runnable> queue, Runnable task) {
		return queue.size() >= this.max;
	}

	protected final void dispatch() {
		boolean flag;
		int c = 0;
		do {
			flag = false;
			Entry<T, Queue<Runnable>> entry;
			Iterator<Entry<T, Queue<Runnable>>> iterator = this.tasks.entrySet().iterator();
			while (iterator.hasNext()) {
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

				Runnable task = this.peek(queue);

				flag = flag ? flag : (queue.size() - 1 > 0);

				if (task == null)
					continue;

				try {
					this.threadPool.execute(task);
					this.poll(queue);
					c++;
				} catch (RejectedExecutionException e) {
					if (this.logger.isDebugEnabled())
						this.logger.debug(e);
					break;
				}
			}
		} while (flag);

		if (this.logger.isDebugEnabled() && c > 0)
			this.logger.debug(Text.SCHEDULE_TASK_DISPATCHED, c);
	}

	// peek task to run
	protected Runnable peek(Queue<Runnable> queue) {
		return queue.peek();
	}

	// drop finished task
	protected void poll(Queue<Runnable> queue) {
		queue.poll();
	}

	protected final void disposeDispatcher() throws InterruptedException {
		this.running = false;
		this.semaphore.release();
		this.dispatcher.join();
	}

	// necessarily?
	protected final void prepareChecker(long delay, long period) {
		this.stopChecker();
		this.checker = new Timer();
		this.checker.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!running || dispatcher.isAlive())
					return;
				logger.fatal(Text.SCHEDULE_DISPATCHER_DOWN);
				try {
					stop();
					start();
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}, delay, period);
	}

	protected final void stopChecker() {
		if (this.checker == null)
			return;
		this.checker.cancel();
	}
}
