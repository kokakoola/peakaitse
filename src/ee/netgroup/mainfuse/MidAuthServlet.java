package ee.netgroup.mainfuse;

import java.io.IOException;

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
			su.setRequestAttributes(req);
			req.setAttribute("challengeId", ref.challengeId);
			req.getRequestDispatcher("zzzmidpin.jsp").forward(req, resp);
			log.debug("Started authentication for "+phoneNo);
		} catch(CommunicationException mie) {
			su.showError(req, resp, mie.getCode());
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
