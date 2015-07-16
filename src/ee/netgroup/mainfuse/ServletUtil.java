package ee.netgroup.mainfuse;

import java.io.IOException;
import java.util.function.BiConsumer;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class ServletUtil {

	private static Props props;

	public ServletUtil() throws IOException {
		if (props == null)
			props = new Props();
	}

	public void setRequestAttributes(final HttpServletRequest req) {
		props.getProperties().forEach(new BiConsumer<Object, Object>() {
			@Override
			public void accept(Object t, Object u) {
				String atrname = (String) t;
				if (atrname.startsWith("url."))
					req.setAttribute(atrname, u);
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
}
