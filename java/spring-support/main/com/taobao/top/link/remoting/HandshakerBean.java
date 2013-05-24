package com.taobao.top.link.remoting;

import java.util.List;
import java.util.Map.Entry;

// just for websocket
public interface HandshakerBean {
	public void onHandshake(List<Entry<String, String>> headers) throws Exception;
}