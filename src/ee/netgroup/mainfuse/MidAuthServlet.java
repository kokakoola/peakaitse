package ee.netgroup.mainfuse;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Serves request that starts mobile id authentication with phone number
 * @author selgemar
 *
 */
@WebServlet(urlPatterns="/midAuth")
public class MidAuthServlet extends BaseServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("Incoming midAuth-request");
		String phoneNo = req.getParameter("phoneNo");
		if (!phoneNo.startsWith("372") && phoneNo.length() <= 8)
			phoneNo = "372" + phoneNo;
		try {
			MobileIdAuthReference ref = new MobileId().startAuthentication(
					phoneNo,
					su.getProperty("mobileId.message"),
					su.getProperty("mobileId.serviceName"),
					su.getProperty("mobileId.serviceUrl")
			);
			req.getSession(true).setAttribute("midRef", ref);
			log.debug("Started authentication for "+phoneNo);

			HashMap<String, Object> rspObj = new HashMap<>();
			rspObj.put("challengeId", ref.challengeId);
			su.setRequestAttributes(req, rspObj);
			resp.getWriter().write(new JSonSerializer().toJson(rspObj));
		} catch(CommunicationException mie) {
			su.showError(req, resp, mie.getCode());
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
