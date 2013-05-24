package com.taobao.top.link.remoting;

import java.util.List;
import java.util.Map.Entry;

import com.taobao.top.link.LinkException;

public class CustomHandshaker implements HandshakerBean {
	@Override
	public void onHandshake(List<Entry<String, String>> headers) throws Exception {
		for (Entry<String, String> entry : headers) {
			if (entry.getKey().equalsIgnoreCase("id"))
				return;
		}
		throw new LinkException("miss id, 不支持");
	}
}
