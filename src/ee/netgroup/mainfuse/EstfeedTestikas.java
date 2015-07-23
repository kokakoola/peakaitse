package ee.netgroup.mainfuse;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class EstfeedTestikas {

	public static void main(String[] args) throws Exception {
		String xmlMime = new ServletUtil().readResource("/resources/sampleEstfeed.request");
		HttpClient h = new DefaultHttpClient();
		HttpPost p = new HttpPost("http://radon.netgroupdigital.com:8080/EstfeedServlet");
		p.setHeader("Content-Type", "text/xml;charset=UTF-8");
		p.setEntity(new StringEntity(xmlMime));
		HttpResponse r = h.execute(p);
		StringBuffer sb = new StringBuffer();
		BufferedReader bfr = new BufferedReader(new InputStreamReader(r.getEntity().getContent(), "utf8"));
		String line;
		while((line = bfr.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		bfr.close();
		String rStr = sb.toString();
		rStr = null;
	}
}
