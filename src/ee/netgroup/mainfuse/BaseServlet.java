package ee.netgroup.mainfuse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

abstract public class BaseServlet extends HttpServlet {

	protected ServletUtil su;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			su = new ServletUtil();
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
