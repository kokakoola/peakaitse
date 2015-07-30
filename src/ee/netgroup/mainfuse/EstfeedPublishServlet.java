package ee.netgroup.mainfuse;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/publish")
public class EstfeedPublishServlet extends BaseServlet {

	private EstfeedAccessor efa;

	@Override
	public void init() throws ServletException {
		super.init();
		efa = new EstfeedAccessor(su);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		StringBuilder sb = new StringBuilder();
		char[] cbuf = new char[8192];
		InputStreamReader reader = new InputStreamReader(req.getInputStream(), "utf8");
		int sz;
		while((sz = reader.read(cbuf)) > 0)
			sb.append(cbuf, 0, sz);
		String mimeResponse = sb.toString();
		log.debug("Received asynchronous response from Estfeed, size="+mimeResponse.length()+" bytes");
//		if (log.isDebugEnabled())
//			log.debug(mimeResponse.length() > 512 ? mimeResponse.substring(0, 512)+"...("+mimeResponse.length()+" bytes)" : mimeResponse);
		resp.setContentType("multipart/related; boundary=haVcUmJkSGCWyYLYoukNggGGKftWbnXc");
		String response = efa.handleMimeResponse(mimeResponse);
		resp.getWriter().print(response);
	}

}
