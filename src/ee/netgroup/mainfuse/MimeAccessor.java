package ee.netgroup.mainfuse;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class MimeAccessor {

	private static final Logger log = Logger.getLogger(MimeAccessor.class);
	private static final Object waitMutex = new Object();
	private static String asyncRsp;

	public String runMimeRequest(String request) throws Exception {
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
		asyncRsp = null;
		synchronized (waitMutex) {
			waitMutex.wait(50000);
		}
		return syncRsp + "\n\n\n\n<br><br><br>\n\n" + asyncRsp;
	}

	public void setMimeResponse(String response) {
		log.debug("Got async MIME response");
		asyncRsp = response;
		synchronized (waitMutex) {
			waitMutex.notify();
		}
	}
}
