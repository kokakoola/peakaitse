package ee.netgroup.mainfuse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class EstfeedAccessor {

	public static final String EIC_SEP = "/";
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
	public CustomerDetails getCustomerData(String identityCode, String identityType) throws Exception {
		CustomerDetails ret = new CustomerDetails();
		Object[] params = {++transactionCounter, identityCode, identityType};
		String response;
		try {
			response = runPublishSubscribeRequest(usagePtReqTpt, usagePtRespTpt, params, usagePtTimeout);
			if (response == null)
				return ret;
			else logResponse("POINTS ASYNC RESPONSE", response);
		}
		catch(CommunicationException ce) {
			log.error("", ce);
			return ret;
		}


		ret.customerEic = SoapAccessor.getResponseField(response, "EIC");
		Collection<String> responseItems = SoapAccessor.getImmediateSubitems(response, "LegalPerson");
		if (responseItems != null) for(String subitem : responseItems) {
			if (SoapAccessor.getResponseField(subitem, "UsagePointLocation") == null)
				continue;
			UsagePointDetails upd = new UsagePointDetails();
			upd.eic = SoapAccessor.getResponseField(subitem, "EIC");
			upd.address = SoapAccessor.getResponseField(subitem, "StreetDetail");
			ret.usagePoints.add(upd);
		}
		log.debug("Received "+ret.usagePoints.size()+" EIC-s for "+identityCode);
		return ret;
	}

	public Collection<ReadingDetails> getUsageHistory(String customerEic, String usagePointEic) throws Exception {
		Object[] params = {++transactionCounter, customerEic, usagePointEic};
		try {
			String response = runPublishSubscribeRequest(usageHistReqTpt, usageHistRespTpt, params, usageHistTimeout);
			if (response != null)
				logResponse("HISTORY ASYNC RESPONSE", response);
			Collection<String> readings = SoapAccessor.getImmediateSubitems(response, "MeterReading");
			ArrayList<ReadingDetails> ret = new ArrayList<ReadingDetails>();
			for(String reading : readings) try {
				ReadingDetails rd = new ReadingDetails();
				rd.period = SoapAccessor.getResponseField(reading, "TimePeriod");
				if (rd.period == null) continue;
				rd.value = Float.parseFloat(SoapAccessor.getResponseField(reading, "Value"));
				ret.add(rd);
			}
			catch(Exception x) {
				log.warn("Ignoring reading "+cut(reading, 128));
			}
			return ret;
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
			throw new CommunicationException(CommunicationException.ERR_SERVER, cut(SoapAccessor.getResponseField(response, "detail"), 128));

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

	private String cut(String text, int maxsz) {
		int len = text.length();
		if (len > maxsz)
			text = text.substring(0, maxsz/2) + "..." + text.substring(len - maxsz/2);
		text = text.replaceAll("\n", "/").replaceAll("\r", "/");
		return text;
	}

	private void logResponse(String boundary, String response) {
		response = "\n==================== " + boundary + "====================\n" +
			cut(response, 1000) + "\n==================== END " + boundary + "====================";
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
