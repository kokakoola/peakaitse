package ee.netgroup.mainfuse;

public class MobileIdException extends Exception {

	public static final String ERR_TECHNICAL_MISSING_CS = "MID_TECHNICAL_MISSING_CHALLENGE_OR_SESSION_CODE";
	public static final String ERR_TECHNICAL_STATUS_CODE = "MID_TECHNICAL_INVALID_STATUS";
	public static final String ERR_AUTH_NOT_STARTED = "MID_AUTH_NOT_STARTED";
	public static final String ERR_SERVER = "SEE_SERVER_LOG";

	private String code;

	public MobileIdException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
