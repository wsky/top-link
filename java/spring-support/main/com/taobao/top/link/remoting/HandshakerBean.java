package com.taobao.top.link.remoting;

import java.util.List;
import java.util.Map.Entry;

public interface HandshakerBean {
	public void onHandshake(List<Entry<String, String>> headers, ContextBean context) throws Exception;
}