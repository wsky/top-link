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
		byte[] data = "hi".getBytes("UTF-8");
		System.out.println(data.length);
		byte[] zipped = GZIPHelper.zip(data);
		System.out.println(zipped.length);
		for (byte b : zipped) {
			System.out.print(b);
			System.out.print(",");
		}
	}
	
	@Test
	public void unzip_test() throws IOException {
		// from c# gzip
		byte[] data = new byte[] { 31, (byte) 139, 8, 0, 0, 0, 0, 0, 4, 0, (byte) 237, (byte) 189, 7, 96, 28, 73,
				(byte) 150, 37, 38, 47, 109, (byte) 202, 123, 127, 74, (byte) 245, 74, (byte) 215, (byte) 224, 116,
				(byte) 161, 8, (byte) 128, 96, 19, 36, (byte) 216, (byte) 144, 64, 16, (byte) 236, (byte) 193,
				(byte) 136, (byte) 205, (byte) 230, (byte) 146, (byte) 236, 29, 105, 71, 35, 41, (byte) 171, 42,
				(byte) 129, (byte) 202, 101, 86, 101, 93, 102, 22, 64, (byte) 204, (byte) 237, (byte) 157, (byte) 188,
				(byte) 247, (byte) 222, 123, (byte) 239, (byte) 189, (byte) 247, (byte) 222, 123, (byte) 239, (byte) 189,
				(byte) 247, (byte) 186, 59, (byte) 157, 78, 39, (byte) 247, (byte) 223, (byte) 255, 63, 92, 102, 100, 1, 108,
				(byte) 246, (byte) 206, 74, (byte) 218, (byte) 201, (byte) 158, 33, (byte) 128, (byte) 170,
				(byte) 200, 31, 63, 126, 124, 31, 63, 34, (byte) 230, (byte) 197, (byte) 255, 3, (byte) 172, 42, (byte) 147,
				(byte) 216, 2, 0, 0, 0 };
		String str = new String(GZIPHelper.unzip(data), "UTF-8");
		System.out.println(str);
		assertEquals("hi", str);
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
