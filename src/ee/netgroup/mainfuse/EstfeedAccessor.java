package ee.netgroup.mainfuse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Hashtable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class EstfeedAccessor {

	private static final Logger log = Logger.getLogger(EstfeedAccessor.class);
	private static String usagePtReqTpt;
	private static String usagePtRespTpt;
	private static long transactionCounter = 0;
	private static Hashtable<Long, TransactionDetails> transactionMap = new Hashtable<>();
	private long msTimeout;

	private class TransactionDetails {
		Object mutex = new Object();
		String response;
	}

	public EstfeedAccessor(ServletUtil su) {
		if (usagePtReqTpt == null) try {
			usagePtReqTpt = su.readResource("/resources/GetElectricityUsagePoints.request");
			usagePtRespTpt = su.readResource("/resources/GetElectricityUsagePoints.response");
			try {
				msTimeout = Long.parseLong(su.getAllProps().get("estfeed.timeoutSeconds")) * 1000;
			}
			catch(Exception x) {
				msTimeout = 30000;
			}
		}
		catch(Exception x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * @param userCode id code or company registration code
	 */
	public void getUsagePointsList(String userCode) throws Exception {
		long tranId = ++transactionCounter;
		String xml = MessageFormat.format(
				usagePtReqTpt,
				tranId,
				userCode);
		runPublishSubscribeRequest(xml, tranId);
	}

	private String runPublishSubscribeRequest(String request, long transactionId) throws Exception {
		HttpClient h = new DefaultHttpClient();
		HttpPost p = new HttpPost("http://radon.netgroupdigital.com:8080/EstfeedServlet");
		p.setHeader("Content-Type", "text/xml;charset=UTF-8");
		p.setEntity(new StringEntity(request));

		log.debug("Running MIME request");
		HttpResponse r = h.execute(p);
		log.debug("Got sync MIME response");
		StringBuffer sb = new StringBuffer();
		BufferedReader bfr = new BufferedReader(new InputStreamReader(r.getEntity().getContent(), "utf8"));
		String line;
		while((line = bfr.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		bfr.close();
		String syncRsp = sb.toString();

		try {
			TransactionDetails td = new TransactionDetails();
			transactionMap.put(transactionId, td);
			synchronized (td.mutex) {
				td.mutex.wait(msTimeout);
			}
			return transactionMap.get(transactionId).response;
		}
		finally {
			transactionMap.remove(transactionId);
		}
	}

	public String handleMimeResponse(String response) {
		int sidx = response.indexOf("<transactionId>");
		if (sidx < 0)
			return null;
		int eidx = response.indexOf("</transactionId>", sidx);
		if (eidx < 0)
			return null;
		Long tranId = new Long(response.substring(sidx + 15, eidx));

		TransactionDetails td = transactionMap.get(tranId);
		if (td == null)
			return null;
		td.response = response;
		synchronized (td.mutex) {
			td.mutex.notify();
		}
		String xml = MessageFormat.format(
				usagePtRespTpt,
				tranId);
		return xml;
	}
}
