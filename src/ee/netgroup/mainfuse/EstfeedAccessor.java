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
	private static long usagePtTimeout;
	private static String usagePtRespTpt;
	private static long transactionCounter = 0;
	private static Hashtable<Long, TransactionDetails> transactionMap = new Hashtable<>();

	private class TransactionDetails {
		Object mutex = new Object();
		String response;
	}

	public EstfeedAccessor(ServletUtil su) {
		if (serviceUrl == null) try {
			serviceUrl = su.getProperty("estfeed.serviceUrl");
			usagePtReqTpt = su.readResource("/resources/GetElectricityUsagePoints.request");
			usagePtRespTpt = su.readResource("/resources/GetElectricityUsagePoints.response");
			try {
				usagePtTimeout = Long.parseLong(su.getAllProps().get("estfeed.msTimeout.GetElectricityUsagePoints"));
			}
			catch(Exception x) {
				usagePtTimeout = 30000;
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
		long tranId = ++transactionCounter;
		String xml = MessageFormat.format(
				usagePtReqTpt,
				tranId,
				identityCode,
				identityType);
		ArrayList<UsagePointDetails> ret = new ArrayList<>();
		String response = runPublishSubscribeRequest("GetElectricityUsagePoints", xml, tranId, usagePtTimeout);
		if (response == null)
			return ret;
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

	private String runPublishSubscribeRequest(String serviceName, String request, long transactionId, long timeout) throws Exception {
		HttpClient h = new DefaultHttpClient();
		HttpPost p = new HttpPost(serviceUrl);
		p.setHeader("Content-Type", "multipart/related; boundary=haVcUmJkSGCWyYLYoukNggGGKftWbnXc");
		p.setEntity(new StringEntity(request));

		log.debug("Running estfeed request "+serviceName);
		HttpResponse r = h.execute(p);
		log.debug("Got synchronous response to "+serviceName);
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
			throw new CommunicationException(CommunicationException.ERR_SERVER, SoapAccessor.getResponseField(response, "detail"));

		try {
			TransactionDetails td = new TransactionDetails();
			transactionMap.put(transactionId, td);
			synchronized (td.mutex) {
				td.mutex.wait(timeout);
			}
			String ret = transactionMap.get(transactionId).response;
			if (ret == null)
				log.warn("Request to "+serviceName+" timed out in "+timeout+"ms");
			return ret;
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
