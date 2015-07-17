package ee.netgroup.mainfuse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import static ee.netgroup.mainfuse.MobileIdException.*;

public class MobileId {

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
		String rs = postSoapRequest(serviceUrl, xml);
		return getResponseField(rs, "Status");
	}

	private String postSoapRequest(String serviceUrl, String xml) throws Exception, UnsupportedEncodingException, IOException, ClientProtocolException {
		HttpClient h = createTrustingHttpClient();
		HttpPost p = new HttpPost(serviceUrl);
		p.setHeader("Content-Type", "text/xml;charset=UTF-8");
		p.setEntity(new StringEntity(xml));
		HttpResponse r = h.execute(p);
		InputStream is = r.getEntity().getContent();
		byte[] b = new byte[4096];
		int sz = is.read(b);
		String rs = new String(b, 0, sz, "utf8");
		return rs;
	}

	public String getStatus() {
		return null;
	}

	private HttpClient createTrustingHttpClient() throws Exception {
		SSLContext ctx = SSLContext.getInstance("SSL");
		X509TrustManager tm = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
			}
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
			}
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		ctx.init(null, new TrustManager[] {tm}, null);
		SSLSocketFactory ssf = new SSLSocketFactory(ctx);
		DefaultHttpClient dhc = new DefaultHttpClient();
		ClientConnectionManager mgr = dhc.getConnectionManager();
		SchemeRegistry registry = mgr.getSchemeRegistry();
		registry.register(new Scheme("https", ssf, 443));
		return new DefaultHttpClient(dhc.getConnectionManager(), dhc.getParams());
	}

	public String getResponseField(String response, String fieldName) {
		int sidx = response.indexOf("<"+fieldName);
		if (sidx < 0)
			return null;
		sidx = response.indexOf('>', sidx);
		if (sidx < 0)
			return null;
		++sidx;
		int eidx = response.indexOf("</"+fieldName+">", sidx);
		if (sidx < 0)
			return null;
		return response.substring(sidx, eidx);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("jee");
		Props p = new Props();
		MobileId mid = new MobileId();
		mid.startAuthentication("3725114741", p.get("mobileId.message"), p.get("mobileId.serviceName"), p.get("mobileId.serviceUrl"));
	}
}
