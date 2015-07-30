package ee.netgroup.mainfuse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class EstfeedAccessor {

	private static final Logger log = Logger.getLogger(EstfeedAccessor.class);
	private static String serviceUrl;
	private static String usagePtReqTpt;
	private static String usageHistReqTpt;
	private static String usagePtRespTpt;
	private static String usageHistRespTpt;
	private static long usagePtTimeout;
	private static long usageHistTimeout;
	private static long transactionCounter = 0;
	private static Hashtable<Long, TransactionDetails> transactionMap = new Hashtable<>();

	private class TransactionDetails {
		Object mutex = new Object();
		String responseTemplate;
		String response;
	}

	public EstfeedAccessor(ServletUtil su) {
		if (serviceUrl == null) try {
			serviceUrl = su.getProperty("estfeed.serviceUrl");
			usagePtReqTpt = su.readResource("/resources/GetElectricityUsagePoints.request");
			usageHistReqTpt = su.readResource("/resources/GetElectricityConsumptionHistory.request");
			usagePtRespTpt = su.readResource("/resources/GetElectricityUsagePoints.response");
			usageHistRespTpt = su.readResource("/resources/GetElectricityConsumptionHistory.response");
			try {
				usagePtTimeout = Long.parseLong(su.getAllProps().get("estfeed.msTimeout.GetElectricityUsagePoints"));
			}
			catch(Exception x) {
				usagePtTimeout = 30000;
			}
			try {
				usageHistTimeout = Long.parseLong(su.getAllProps().get("estfeed.msTimeout.GetElectricityConsumptionHistory"));
			}
			catch(Exception x) {
				usageHistTimeout = 30000;
			}
		}
		catch(Exception x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * @param identityCode id code or company registration code
	 * @param identityType person/company
	 */
	public Collection<UsagePointDetails> getUsagePointsList(String identityCode, String identityType) throws Exception {
		ArrayList<UsagePointDetails> ret = new ArrayList<>();
		Object[] params = {++transactionCounter, identityCode, identityType};
		String response;
		try {
			response = runPublishSubscribeRequest(usagePtReqTpt, usagePtRespTpt, params, usagePtTimeout);
			if (response == null)
				return ret;
		}
		catch(CommunicationException ce) {
			log.error("", ce);
			return ret;
		}


		Collection<String> responseItems = SoapAccessor.getImmediateSubitems(response, "LegalPerson");
		if (responseItems != null) for(String subitem : responseItems) {
			if (SoapAccessor.getResponseField(subitem, "UsagePointLocation") == null)
				continue;
			UsagePointDetails upd = new UsagePointDetails();
			upd.eic = SoapAccessor.getResponseField(subitem, "EIC");
			upd.address = SoapAccessor.getResponseField(subitem, "StreetDetail");
			ret.add(upd);
		}
		log.debug("Received "+ret.size()+" EIC-s for "+identityCode);
		return ret;
	}

	public String getUsageHistory(String eic) throws Exception {
		Object[] params = {++transactionCounter, eic};
		try {
			String response = runPublishSubscribeRequest(usageHistReqTpt, usageHistRespTpt, params, usageHistTimeout);
			if (response != null)
				logResponse("HISTORY ASYNC RESPONSE", response);
			return response;
		}
		catch(CommunicationException ce) {
			log.error("", ce);
			return null;
		}
	}

	/**
	 * @param reqTemplate request template
	 * @param params template parameters; parameter[0] must be transaction Id
	 * @param timeout timeout in ms
	 * @return response; or null if timeout occured
	 * @throws Exception
	 */
	private String runPublishSubscribeRequest(String reqTemplate, String respTemplate, Object[] params, long timeout) throws Exception {
		long transactionId = (Long) params[0];

		// Compose request description for debugging
		String serviceName = SoapAccessor.getResponseField(reqTemplate, "code");
		StringBuilder sbr = new StringBuilder(serviceName+"; params=[");
		for(Object param : params)
			sbr.append(param+" ");
		sbr.append("]");
		String requestDescr = sbr.toString();
		log.debug("Executing estfeed request "+requestDescr+", timeout="+timeout+"ms");

		// Format and execute the request
		String xml = MessageFormat.format(reqTemplate, params);
		HttpClient h = new DefaultHttpClient();
		HttpPost p = new HttpPost(serviceUrl);
		p.setHeader("Content-Type", "multipart/related; boundary=haVcUmJkSGCWyYLYoukNggGGKftWbnXc");
		p.setEntity(new StringEntity(xml));
		HttpResponse r = h.execute(p);
		log.debug("Got synchronous response to "+requestDescr);

		// Assemble synchronous response
		StringBuffer sb = new StringBuffer();
		BufferedReader bfr = new BufferedReader(new InputStreamReader(r.getEntity().getContent(), "utf8"));
		String line;
		while((line = bfr.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		bfr.close();
		String response = sb.toString();

		if (response.indexOf("<estfeed:acknowledgement") < 0)
			throw new CommunicationException(CommunicationException.ERR_SERVER, cut128(SoapAccessor.getResponseField(response, "detail")));

		try {
			TransactionDetails td = new TransactionDetails();
			td.responseTemplate = respTemplate;
			transactionMap.put(transactionId, td);
			synchronized (td.mutex) {
				td.mutex.wait(timeout);
			}
			String ret = transactionMap.get(transactionId).response;
			if (ret == null)
				log.warn("Request "+requestDescr+" timed out in "+timeout+"ms");
			else log.debug("Got asynchronous response to "+requestDescr);
			return ret;
		}
		finally {
			transactionMap.remove(transactionId);
		}
	}

	private String cut128(String text) {
		int len = text.length();
		if (len > 128)
			text = text.substring(0, 64) + "..." + text.substring(len - 64);
		if (text.indexOf('\n') >= 0)
			text = text.substring(0, text.indexOf('\n'));
		return text;
	}

	private void logResponse(String boundary, String response) {
		int rlen = response.length();
		if (rlen > 1000)
			response = response.substring(0, 500) + "..." + response.substring(rlen - 500);
		response = "\n==================== " + boundary + "====================\n" +
			response + "\n==================== END " + boundary + "====================";
		log.debug(response);
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
				td.responseTemplate,
				tranId);
		return xml;
	}
}
