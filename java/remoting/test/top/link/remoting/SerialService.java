package top.link.remoting;

import java.util.Date;
import java.util.HashMap;

import top.link.remoting.DefaultMethodCallProcessor;

public class SerialService extends DefaultMethodCallProcessor implements SerialInterface {
	public Entity echo(String arg1,
			byte arg2,
			double arg3,
			float arg4,
			int arg5,
			long arg6,
			short arg7,
			Date arg8,
			HashMap<String, String> arg9,
			Entity arg10,
			String[] arg11) {
		return arg10;
	}
}
