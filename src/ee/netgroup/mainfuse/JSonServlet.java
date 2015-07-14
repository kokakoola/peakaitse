package ee.netgroup.mainfuse;

import java.io.IOException;
import java.time.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns="/data")
public class JSonServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ArrayList<HashMap<String, Object>> cons = assembleGraphData();
		ArrayList<HashMap<String, String>> adrlist = assembleAdrList();

		HashMap<String, Object> rspObj = new HashMap<>();
		rspObj.put("address", "Tulika põik 7-11");
		rspObj.put("addressList", adrlist);
		rspObj.put("graphReadings", cons);
		rspObj.put("recommendedFuseSize", 16);
		rspObj.put("calculatedMinFuseSize", 20);
		resp.getWriter().write(new JSonSerializer().toJson(rspObj));
	}

	private ArrayList<HashMap<String, String>> assembleAdrList() {
		final String[] ADR = {"Tulika põik 7-11", "7899023", "Läänemere tee 76-121", "9883992"};
		ArrayList<HashMap<String, String>> ret = new ArrayList<>();
		for(int i = 0; i < ADR.length; i += 2) {
			HashMap<String, String> a = new HashMap<>();
			a.put("address", ADR[i]);
			a.put("eic", ADR[i + 1]);
			ret.add(a);
		}
		return ret;
	}

	private ArrayList<HashMap<String, Object>> assembleGraphData() {
		LocalDateTime end = LocalDateTime.now(ZoneId.of("Europe/Tallinn"));
		end = end.minusHours(end.getHour());
		end = end.minusMinutes(end.getMinute());
		end = end.minusSeconds(end.getSecond());
		end = end.minusNanos(end.getNano());
		LocalDateTime start = LocalDateTime.from(end);
		start = start.minusYears(1);
		ArrayList<HashMap<String, Object>> cons = new ArrayList<>();
		Random rnd = new Random();
		while(start.isBefore(end)) {
			HashMap<String, Object> r = new HashMap<>();
			r.put("time", start.toString());
			double val = rnd.nextInt(1000) / 1000f;
			r.put("kWh", val);
			r.put("A", val*1000/230);
			cons.add(r);
			start = start.plusHours(1);
		}
		return cons;
	}
}
