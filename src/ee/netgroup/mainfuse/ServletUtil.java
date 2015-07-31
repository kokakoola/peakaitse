package ee.netgroup.mainfuse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.BiConsumer;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ServletUtil {

	private static Props props;

	public ServletUtil() throws IOException {
		if (props == null) {
			props = new Props();
			PropertyConfigurator.configureAndWatch(props.get("log4j.filepath"));
			Logger.getLogger(ServletUtil.class).info("Mainfuse application up & running");
		}
	}

	public void setRequestAttributes(final HttpServletRequest req, final HashMap<String, Object> jsonObj) {
		props.getProperties().forEach(new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object t, Object u) {
				String atrname = (String) t;
				if (atrname.startsWith("url.")) {
					req.setAttribute(atrname, u);
					if (jsonObj != null)
						jsonObj.put(atrname.substring(4)+"Url", u);
				}
			}
		});
	}

	public void showError(HttpServletRequest req, HttpServletResponse resp, String errorCode) throws ServletException, IOException {
		req.setAttribute("errorCode", errorCode);
		req.getRequestDispatcher("zzzloginerror.jsp").forward(req, resp);
	}

	public void createSession(HttpServletRequest req, String idCode, String name, String surname) {
		HttpSession session = req.getSession(false);
		if (session != null)
			session.invalidate();
		session = req.getSession(true);
		session.setAttribute("idCode", idCode);
		session.setAttribute("name", name);
		session.setAttribute("surname", surname);
	}

	public boolean hasValidSession(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		return (session != null && session.getAttribute("idCode") != null);
	}

	public String getIdCode(HttpServletRequest req) {
		return (String) req.getSession(false).getAttribute("idCode");
	}

	public String getName(HttpServletRequest req) {
		return (String) req.getSession(false).getAttribute("name");
	}

	public String getSurname(HttpServletRequest req) {
		return (String) req.getSession(false).getAttribute("surname");
	}

	public String getProperty(String propertyName) {
		return props.get(propertyName);
	}

	public String readResource(String filename) throws IOException {
		if (filename.charAt(0) != '/')
			filename = "/" + filename;
		InputStream s = MobileId.class.getResourceAsStream(filename);
		byte[] b = new byte[4192];
		int sz = s.read(b);
		s.close();
		return new String(b, 0, sz);

	}

	public Props getAllProps() {
		return props;
	}
}
