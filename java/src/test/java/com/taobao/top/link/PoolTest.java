package com.taobao.top.link;

import static org.junit.Assert.*;

import org.junit.Test;

public class PoolTest {
	@Test
	public void check_test() {
		TestPool pool = new TestPool(0, 0);
		Object o = pool.chekOut();
		assertNotNull(o);
		pool.checkIn(o);
		assertEquals(o, pool.chekOut());
		assertEquals(1, pool.size());
	}

	@Test
	public void check_more_test() {
		TestPool pool = new TestPool(10, 0);
		assertNotNull(pool.chekOut());
		assertNotNull(pool.chekOut());
		assertNotNull(pool.chekOut());
		assertEquals(3, pool.size());
	}

	@Test
	public void maxSize_and_not_wait_test() {
		TestPool pool = new TestPool(1, 0);
		assertNotNull(pool.chekOut());
		assertNull(pool.chekOut());
		assertEquals(1, pool.size());
	}

	@Test
	public void maxSize_and_wait_test() {
		TestPool pool = new TestPool(1, 10);
		assertNotNull(pool.chekOut());
		assertNull(pool.chekOut());
		assertEquals(1, pool.size());
	}

	@Test
	public void maxSize_and_wait_then_checkin_test() throws InterruptedException {
		final TestPool pool = new TestPool(1, 5000);
		final Object o = pool.chekOut();
		assertNotNull(o);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pool.checkIn(o);
				System.out.println("checkin");
			}
		});
		t.start();
		assertNotNull(pool.chekOut());
		assertEquals(1, pool.size());
		t.join();
	}

//	@Test
//	public void concurrent_check_test() throws InterruptedException {
//		final TestPool pool = new TestPool(5, 1000);
//		final Object[] out = new Object[5];
//
//		Thread checkout = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				for (int i = 0; i < 10000; i++) {
//					Object o = pool.chekOut();
//					assertNotNull(o);
//					out[10000 % 5] = o;
//				}
//			}
//		});
//
//		Thread checkin = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (true) {
//					for (int i = 0; i < 5; i++) {
//						if (out[i] != null) {
//							pool.checkIn(out[i]);
//							out[i] = null;
//						}
//					}
//				}
//			}
//		});
//
//		checkout.start();
//		checkin.start();
//
//		Thread.sleep(10000);
//		// checkIn maybe checkin duplicate object
//		// assertEquals(5, pool.size());
//	}

	public class TestPool extends Pool<Object> {
		public TestPool(int maxSize, int waitTimeout) {
			super(maxSize, waitTimeout);
		}

		@Override
		public Object create() {
			return new Object();
		}

		@Override
		public boolean validate(Object t) {
			return true;
		}
	}
}
