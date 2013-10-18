package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

public class CompressTest {
	@Test
	public void zip_test() {
		zip_test(1024);
		zip_test(1024);
		zip_test(1024 * 2);
		zip_test(1024 * 4);
		zip_test(1024 * 4 * 10);
	}

	@Test
	public void print() {
		byte[] zipped = gzip("hi".getBytes());
		for (byte b : zipped) {
			System.out.print(b);
			System.out.print(",");
		}
		System.out.println();
	}

	private void zip_test(int length) {
		String str = "hello!";
		for (int i = 0; i < length; i++)
			str += i % 10;

		byte[] data = str.getBytes();
		System.out.println("unzip length=" + data.length);

		long begin = System.nanoTime();
		byte[] zipped = gzip(data);
		System.out.println("zip length=" + zipped.length);
		System.out.println("cost=" + (float) (System.nanoTime() - begin) / (1000 * 1000));

		assertEquals(str, new String(ungzip(zipped)));
	}

	public static byte[] gzip(byte[] bytes) {
		ByteArrayOutputStream baos = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			baos = new ByteArrayOutputStream();
			GZIPOutputStream gzos = new GZIPOutputStream(baos);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bais.read(buffer)) >= 0) {
				gzos.write(buffer, 0, len);
			}
			gzos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (baos.toByteArray());
	}

	public static byte[] ungzip(byte[] gbytes) {
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(gbytes);
		try {
			baos = new ByteArrayOutputStream();
			GZIPInputStream gzis = new GZIPInputStream(bais);
			byte[] bytes = new byte[1024];
			int len;
			while ((len = gzis.read(bytes)) > 0) {
				baos.write(bytes, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (baos.toByteArray());
	}
}
