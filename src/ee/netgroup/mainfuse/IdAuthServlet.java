package ee.netgroup.mainfuse;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * Serves id card authentication request.
 * Due to the J2EE server architecture, this servlet is only being invoked upon successful authentication.
 * Unsuccessful authentication cannot be caught.
 *
 * @author selgemar
 *
 */
@WebServlet(urlPatterns="/idAuth")
public class IdAuthServlet extends BaseServlet {

	private static Logger log = Logger.getLogger(IdAuthServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
		if (certs == null) {
			su.showError(req, resp, "invalidIdCard");
			return;
		}
		String n = certs[0].getSubjectDN().getName();
		String name = null, surname = null, idCode = null;
		for(String kv : n.split(",")) {
			int idx = kv.indexOf('=');
			if ( idx > 0) {
				String key = kv.substring(0,  idx).trim();
				String val = kv.substring(idx + 1).trim();
				if ("SERIALNUMBER".equals(key))
					idCode = val;
				else if ("GIVENNAME".equals(key))
					name = val;
				else if ("SURNAME".equals(key))
					surname = val;
			}
		}
		log.debug("Successful id-card authentication by idCode="+idCode);
		su.createSession(req, idCode, name, surname);
		su.setRequestAttributes(req);
		req.getRequestDispatcher("zzzidok.jsp").forward(req, resp);
	}

}
