package ee.netgroup.mainfuse;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns="/idAuth")
public class IdAuthServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
		String n = certs[0].getSubjectDN().getName();
		for(String kv : n.split(",")) {
			int idx = kv.indexOf('=');
			if ( idx > 0)
				req.setAttribute(kv.substring(0,  idx).trim(), kv.substring(idx + 1).trim());
		}
		req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
	}

}
