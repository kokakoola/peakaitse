package ee.netgroup.mainfuse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Props {

	private Properties p;

	public Props() throws IOException {
		p = new Properties();
		InputStream s = Props.class.getResourceAsStream("/resources/profile.properties");
		p.load(s);
		s.close();
	}

	public String get(String propertyName) {
		return p.getProperty(propertyName);
	}

	public Properties getProperties() {
		return p;
	}
}
