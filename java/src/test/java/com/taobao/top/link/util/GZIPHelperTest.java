package com.taobao.top.link.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.taobao.top.link.util.GZIPHelper;

public class GZIPHelperTest {
	@Test
	public void zip_test() throws IOException {
		zip_test(1024);
		zip_test(1024);
		zip_test(1024 * 2);
		zip_test(1024 * 4);
		zip_test(1024 * 4 * 10);
	}

	@Test
	public void print() throws IOException {
		byte[] zipped = GZIPHelper.zip("hi".getBytes());
		for (byte b : zipped) {
			System.out.print(b);
			System.out.print(",");
		}
		System.out.println();
	}

	private void zip_test(int length) throws IOException {
		String str = "hello!";
		for (int i = 0; i < length; i++)
			str += i % 10;

		byte[] data = str.getBytes();
		System.out.println("unzip length=" + data.length);

		long begin = System.nanoTime();
		byte[] zipped = GZIPHelper.zip(data);
		System.out.println("zip length=" + zipped.length);
		System.out.println("cost=" + (float) (System.nanoTime() - begin) / (1000 * 1000));

		assertEquals(str, new String(GZIPHelper.unzip(zipped)));
	}
}
