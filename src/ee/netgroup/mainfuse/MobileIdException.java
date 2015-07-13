package ee.netgroup.mainfuse;

public class MobileIdException extends Exception {

	private String code;

	public MobileIdException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
