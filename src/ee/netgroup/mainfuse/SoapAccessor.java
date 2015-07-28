package ee.netgroup.mainfuse;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import javax.net.ssl.*;

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

abstract class SoapAccessor {

	private static final Logger log = Logger.getLogger(SoapAccessor.class);

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

	protected String postSoapRequest(String serviceUrl, String xml) throws Exception, UnsupportedEncodingException, IOException, ClientProtocolException {
		HttpClient h = createTrustingHttpClient();
		HttpPost p = new HttpPost(serviceUrl);
		p.setHeader("Content-Type", "text/xml;charset=UTF-8");
		p.setEntity(new StringEntity(xml));
		log.debug("Sending request to "+serviceUrl);
		HttpResponse r = h.execute(p);
		StringBuffer sb = new StringBuffer();
		BufferedReader bfr = new BufferedReader(new InputStreamReader(r.getEntity().getContent(), "utf8"));
		String line;
		while((line = bfr.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		bfr.close();
		String ret = sb.toString();
		log.debug("Got "+ret.length()+" bytes response from "+serviceUrl);
		return ret;
	}

	public static String getResponseField(String response, String fieldName) {
		int spos = 0;
		int sidx;
		do {
			sidx = response.indexOf("<"+fieldName, spos);
			if (sidx < 0)
				return null;
			switch(response.charAt(sidx + 1 + fieldName.length())) {
			case ' ': case '>': case '\n': case '\t': case '\r':
				spos = -1;
				break;
			default:
				spos = sidx + 1 + fieldName.length();
				break;
			}
		} while(spos >= 0);
		sidx = response.indexOf('>', sidx);
		if (sidx < 0)
			return null;
		++sidx;
		int eidx = response.indexOf("</"+fieldName+">", sidx);
		if (sidx < 0)
			return null;
		return response.substring(sidx, eidx);
	}

	public static Collection<String> getImmediateSubitems(String xml, String parentElementName) {
		ArrayList<String> ret = new ArrayList<>();
		int sidx = xml.indexOf("<" + parentElementName);
		if (sidx < 0)
			return ret;
		int eidx = xml.indexOf("</" + parentElementName, sidx);
		if (eidx < 0)
			return ret;

		// Skip parent element tag
		++sidx;
		while(sidx < eidx && xml.charAt(sidx) != '<') ++sidx;

		// Extract subitems
		String subxml = xml.substring(sidx, eidx);

		int pos = 0, spos = 0;
		int depth = 0;
		int len = subxml.length();
		while(pos < len) {
			char c = subxml.charAt(pos++);
			switch(c) {
			case '<':
				if (subxml.charAt(pos) == '/')
					--depth;
				else ++depth;
				break;
			case '>':
				if (subxml.charAt(pos - 2) == '/')
					--depth;
				if (depth == 0) {
					String subelement = subxml.substring(spos, pos);
					spos = pos;
					ret.add(subelement.trim());
				}
				break;
			}
		}
		return ret;
	}

}
