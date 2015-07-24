package ee.netgroup.mainfuse;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/publish")
public class MimePublishServlet extends BaseServlet {

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
		BufferedReader reader = req.getReader();
		int sz;
		while((sz = reader.read(cbuf)) > 0)
			sb.append(cbuf, 0, sz);
		String mimeResponse = sb.toString();
		if (log.isDebugEnabled())
			log.debug(mimeResponse.length() > 512 ? mimeResponse.substring(0, 512)+"...("+mimeResponse.length()+" bytes)" : mimeResponse);
		String response = efa.handleMimeResponse(mimeResponse);
		resp.getWriter().print(response);
	}

}
