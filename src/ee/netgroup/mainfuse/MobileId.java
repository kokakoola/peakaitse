package ee.netgroup.mainfuse;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import static ee.netgroup.mainfuse.MobileIdException.*;

public class MobileId extends SoapAccessor {

	private static Logger log = Logger.getLogger(MobileId.class);
	private static String authReqTpt;//MobileAuthenticate request template
	private static String statusReqTpt;//GetMobileAuthenticateStatus request template

	public MobileId() {
		if (authReqTpt == null) try {
			authReqTpt = new ServletUtil().readResource("/resources/MobileAuthenticate.request");
			statusReqTpt = new ServletUtil().readResource("/resources/GetMobileAuthStatus.request");
		}
		catch(Exception x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * @param phoneNo 3725556666
	 * @param greetingText message displayed on the phone display
	 * @param serviceName will be probably agreed with SK (in test environment, it must be "Testimine")
	 * @param serviceUrl mobile id service url
	 * @return session code (used to request the status)
	 */
	public MobileIdAuthReference startAuthentication(String phoneNo, String greetingText, String serviceName, String serviceUrl) throws Exception {
		String xml = MessageFormat.format(authReqTpt, phoneNo, greetingText, serviceName);
		log.debug(xml);
		String rs = postSoapRequest(serviceUrl, xml);
		if (!"OK".equalsIgnoreCase(getResponseField(rs, "Status"))) {
			log.debug("MobileAuthenticate failure response: " + rs);
			throw new MobileIdException(ERR_TECHNICAL_STATUS_CODE, "Service MobileAuthenticate failed with code " + getResponseField(rs, "faultcode") + " and message: " + getResponseField(rs, "message"));
		}
		MobileIdAuthReference mair = new MobileIdAuthReference();
		mair.sessionCode = getResponseField(rs, "Sesscode");
		mair.challengeId = getResponseField(rs, "ChallengeID");
		mair.idCode = getResponseField(rs, "UserIDCode");
		mair.name = getResponseField(rs, "UserGivenname");
		mair.surname = getResponseField(rs, "UserSurname");
		if (mair.sessionCode == null || mair.challengeId == null) {
			log.debug("MobileAuthenticate returned no sessionCode/challengeId");
			throw new MobileIdException(ERR_TECHNICAL_MISSING_CS, "MobileAuthenticate returned no sessionCode/challengeId");
		}
		return mair;
	}

	public String getStatus(String sessionCode, String serviceUrl) throws Exception {
		String xml = MessageFormat.format(statusReqTpt, sessionCode);
		log.debug(xml);
		String rs = postSoapRequest(serviceUrl, xml);
		return getResponseField(rs, "Status");
	}

	public String getStatus() {
		return null;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("jee");
		Props p = new Props();
		MobileId mid = new MobileId();
		mid.startAuthentication("3725114741", p.get("mobileId.message"), p.get("mobileId.serviceName"), p.get("mobileId.serviceUrl"));
	}
}
