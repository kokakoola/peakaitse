package ee.netgroup.mainfuse;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns="/idAuth")
public class IdAuthServlet extends HttpServlet {

	private static Logger log = LoggerFactory.getLogger(IdAuthServlet.class);

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.info("IdAuthServlet::doGet");
		X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
		String n = certs[0].getSubjectDN().getName();
		System.out.println("Cert common name: "+n);
		log.info("Cert common name: "+n);
		log.debug("Natuke debug logi kah");
		for(String kv : n.split(",")) {
			int idx = kv.indexOf('=');
			if ( idx > 0)
				req.setAttribute(kv.substring(0,  idx).trim(), kv.substring(idx + 1).trim());
		}
		req.getRequestDispatcher("dashboard.jsp").forward(req, resp);
	}

}
