package ee.netgroup.mainfuse;

public class CommunicationException extends Exception {

	public static final String ERR_MID_TECHNICAL_MISSING_CS = "MID_TECHNICAL_MISSING_CHALLENGE_OR_SESSION_CODE";
	public static final String ERR_MID_TECHNICAL_STATUS_CODE = "MID_TECHNICAL_INVALID_STATUS";
	public static final String ERR_MID_AUTH_NOT_STARTED = "MID_AUTH_NOT_STARTED";
	public static final String ERR_SERVER = "SEE_SERVER_LOG";

	private String code;

	public CommunicationException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
