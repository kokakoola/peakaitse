package ee.netgroup.mainfuse;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/publish")
public class MimePublishServlet extends BaseServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("mime-publish service, method="+req.getMethod()+", contentLen="+req.getContentLength());
		char[] c = new char[1000000];
		int sz = req.getReader().read(c);
		String s = "Read amount="+sz;
		if (sz > 0)
			s += "\nContent:\n=============\n"+new String(c, 0, sz)+"\n=============\n";
		Enumeration<String> pn = req.getParameterNames();
		while(pn.hasMoreElements()) {
			String n = pn.nextElement();
			s += "\nParameter "+n+"="+req.getParameter(n);
		}
		log.debug(s);
		new MimeAccessor().setMimeResponse(s);
	}

}
