package ee.netgroup.mainfuse;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Serves mobile-id poll-requests (checking the status of PIN entry from phone)
 * @author selgemar
 *
 */
@WebServlet(urlPatterns="/midStatus")
public class MidAuthStatusServlet extends BaseServlet {

	private static Logger log = Logger.getLogger(MidAuthStatusServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MobileIdAuthReference ref = (MobileIdAuthReference) req.getSession(true).getAttribute("midRef");
		String statusCode;
		try {
			if (ref != null) {
				statusCode = new MobileId().getStatus(ref.sessionCode, su.getProperty("mobileId.serviceUrl"));
				if ("USER_AUTHENTICATED".equals(statusCode)) {
					log.debug("Successful mobile-id authentication by idCode="+ref.idCode);
					su.createSession(req, ref.idCode, ref.name, ref.surname);
				}
			}
			else statusCode = MobileIdException.ERR_AUTH_NOT_STARTED;
		}
		catch(Exception x) {
			log.error("", x);
			statusCode = MobileIdException.ERR_SERVER;
		}
		HashMap<String, Object> rspObj = new HashMap<>();
		rspObj.put("status", statusCode);
		resp.getWriter().write(new JSonSerializer().toJson(rspObj));
	}

}
