package ee.netgroup.mainfuse;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves mobile-id poll-requests (checking the status of PIN entry from phone)
 * @author selgemar
 *
 */
@WebServlet(urlPatterns="/midStatus")
public class MidAuthStatusServlet extends BaseServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MobileIdAuthReference ref = (MobileIdAuthReference) req.getSession(true).getAttribute("midRef");
		String statusCode;
		try {
			if (ref != null) {
				statusCode = new MobileId().getStatus(ref.sessionCode, su.getProperty("mobileId.serviceUrl"));
				if ("USER_AUTHENTICATED".equals(statusCode)) {
					log.info("Successful mobile-id authentication by idCode="+ref.idCode);
					su.createSession(req, ref.idCode, ref.name, ref.surname);
				}
			}
			else statusCode = CommunicationException.ERR_MID_AUTH_NOT_STARTED;
		}
		catch(Exception x) {
			log.error("", x);
			statusCode = CommunicationException.ERR_SERVER;
		}
		HashMap<String, Object> rspObj = new HashMap<>();
		rspObj.put("status", statusCode);
		resp.getWriter().write(new JSonSerializer().toJson(rspObj));
	}

}
