package ee.netgroup.mainfuse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/mime")
public class MimeServlet extends BaseServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String xmlMime = new ServletUtil().readResource("/resources/GetElectricityUsagePoints.request");
		try {
			String response = new MimeAccessor().runMimeRequest(xmlMime);
			resp.setContentType("text/plain");
			resp.getWriter().print(response);
		}
		catch(Exception x) {
			log.error("", x);
			throw new ServletException(x);
		}

	}

}
