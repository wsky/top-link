package top.link.remoting;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Entity implements Serializable {
	private String String;
	private long Long;
	private Date Date;
	private HashMap<String, String> Map;
	private String[] array;

	public String getString() {
		return this.String;
	}

	public void setString(String string) {
		this.String = string;
	}

	public long getLong() {
		return this.Long;
	}

	public void setLong(long Long) {
		this.Long = Long;
	}

	public Date getDate() {
		return this.Date;
	}

	public void setDate(Date date) {
		this.Date = date;
	}

	public HashMap<String, String> getMap() {
		return this.Map;
	}

	public void setMap(HashMap<String, String> map) {
		this.Map = map;
	}

	public String[] getArray() {
		return this.array;
	}

	public void setArray(String[] array) {
		this.array = array;
	}
}