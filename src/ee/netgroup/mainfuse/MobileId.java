package ee.netgroup.mainfuse;

import java.io.InputStream;
import java.text.MessageFormat;

public class MobileId {

	private static String authReqTpt;//MobileAuthenticate request template

	public MobileId() {
		if (authReqTpt == null) try {
			InputStream s = MobileId.class.getResourceAsStream("/resources/MobileAuthenticate.request");
			byte[] b = new byte[4192];
			int sz = s.read(b);
			s.close();
			authReqTpt = new String(b, 0, sz);
		}
		catch(Exception x) {
			throw new RuntimeException(x);
		}
	}

	public void startAuthentication(String phoneNo, String greetingText, String serviceName, String serviceUrl) {
		String request = MessageFormat.format(authReqTpt, phoneNo, greetingText, serviceName);
	}
}
