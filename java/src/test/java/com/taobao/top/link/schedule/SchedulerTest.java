package com.taobao.top.link.schedule;

import static org.junit.Assert.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.Text;

public class SchedulerTest {
	@Test
	public void queue_test() {
		Queue<String> queue = new ConcurrentLinkedQueue<String>();
		queue.add("123");
		assertNotNull(queue.peek());
		assertNotNull(queue.poll());
	}

	@Test
	public void semaphore_test() throws InterruptedException {
		final Semaphore semaphore = new Semaphore(0);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				semaphore.release();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				semaphore.release();
				semaphore.release();
			}
		}).start();
		semaphore.acquire();
		semaphore.acquire(2);
	}

	@Test
	public void start_stop_test() throws InterruptedException {
		Scheduler<String> scheduler = new Scheduler<String>();
		scheduler.start();
		Thread.sleep(500);
		scheduler.stop();
	}

	@Test
	public void schedule_test() throws InterruptedException, LinkException {
		Scheduler<String> scheduler = new Scheduler<String>();
		scheduler.start();
		final CountDownLatch latch = new CountDownLatch(1);
		scheduler.schedule("user", new Runnable() {
			@Override
			public void run() {
				latch.countDown();
			}
		});
		latch.await();
		scheduler.stop();
	}

	@Test
	public void drop_test() throws InterruptedException, LinkException {
		final Scheduler<String> scheduler = new Scheduler<String>();
		scheduler.setUserMaxPendingCount(10000);
		scheduler.start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						scheduler.schedule("user", new Runnable() {
							@Override
							public void run() {
							}
						});
					} catch (LinkException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		scheduler.drop("user");
		scheduler.stop();
	}

	@Test(expected = LinkException.class)
	public void got_max_test() throws InterruptedException, LinkException {
		Scheduler<String> scheduler = new Scheduler<String>();
		scheduler.setUserMaxPendingCount(10);
		scheduler.start();
		for (int i = 0; i < 100; i++) {
			try {
				scheduler.schedule("user", new Runnable() {
					@Override
					public void run() {
					}
				});
			} catch (LinkException e) {
				scheduler.stop();
				assertEquals(String.format(Text.SCHEDULE_GOT_MAX, 10), e.getMessage());
				throw e;
			}
		}
	}

	@Test
	public void schedule_sequence_test() throws InterruptedException, LinkException {
		final Scheduler<String> scheduler = new Scheduler<String>(new DefaultLoggerFactory(true, true, true, true, true));
		scheduler.setUserMaxPendingCount(10000);
		scheduler.start();
		int count = 1000;
		final CountDownLatch latch = new CountDownLatch(count);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			scheduler.schedule("user", new Runnable() {
				@Override
				public void run() {
					latch.countDown();
				}
			});

		}
		latch.await();
		System.out.println(count + " cost=" + (System.currentTimeMillis() - begin));
		scheduler.stop();
		// 1000 cost=36
	}

	@Test
	public void schedule_threaded_test() throws InterruptedException, LinkException {
		final Scheduler<String> scheduler = new Scheduler<String>(new DefaultLoggerFactory(true, true, true, true, true));
		scheduler.setUserMaxPendingCount(1000000);
		scheduler.start();
		final int count = 1000;
		int thread = 4;
		final CountDownLatch latch = new CountDownLatch(count * thread);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < thread; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < count; i++) {
						try {
							scheduler.schedule("user" + i, new Runnable() {
								@Override
								public void run() {
									latch.countDown();
								}
							});
						} catch (LinkException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
		latch.await();
		System.out.println(count * thread + " cost=" + (System.currentTimeMillis() - begin));
		scheduler.stop();
		// 1000 cost=182
	}
}
