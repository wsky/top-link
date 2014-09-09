package top.link.endpoint;

import java.util.HashMap;
import java.util.Map;

import top.link.LinkException;
import top.link.endpoint.Identity;

public class TopIdentity implements Identity {
	public String appKey;

	public TopIdentity(String appkey) {
		this.appKey = appkey;
	}

	@SuppressWarnings("unchecked")
	public Identity parse(Object data) throws LinkException {
		TopIdentity identity = new TopIdentity(null);
		HashMap<String, String> headers = (HashMap<String, String>) data;
		if (headers.get("appkey") != null && headers.get("appkey") != "") {
			identity.appKey = headers.get("appkey");
			return identity;
		}
		throw new LinkException(401, "id error");
	}

	@SuppressWarnings("unchecked")
	public void render(Object to) {
		((Map<String, String>) to).put("appkey", this.appKey);
	}

	public boolean equals(Identity id) {
		return this.appKey != null && this.appKey.equals(((TopIdentity) id).appKey);
	}

	@Override
	public String toString() {
		return this.appKey;
	}
}
