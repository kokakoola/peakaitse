package ee.netgroup.mainfuse;

import java.io.IOException;
import java.time.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Serves graph data request and outputs Json
 * @author selgemar
 *
 */
@WebServlet(urlPatterns="/data")
public class DashboardServlet extends BaseServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("Incoming data-request");
		HashMap<String, Object> rspObj = new HashMap<>();
		try {
			if (su.hasValidSession(req)) {
				ArrayList<HashMap<String, Object>> cons = assembleGraphData();
				ArrayList<HashMap<String, String>> adrlist = assembleAdrList(su.getIdCode(req), su.getName(req), su.getSurname(req));
		
				rspObj.put("address", "Tulika põik 7-11");
				rspObj.put("eic", "7368956");
				rspObj.put("addressList", adrlist);
				rspObj.put("graphReadings", cons);
				rspObj.put("recommendedFuseSize", 16);
				rspObj.put("calculatedMinFuseSize", 20);
				rspObj.put("idCode", su.getIdCode(req));
				rspObj.put("name", su.getName(req));
				rspObj.put("surname", su.getSurname(req));
			}
			else rspObj.put("errorCode", "NoSession");
		}
		catch(Exception x) {
			log.error("", x);
			rspObj.put("errorCode", CommunicationException.ERR_SERVER);
		}

		String rspStr = new JSonSerializer().toJson(rspObj);
		resp.getWriter().write(rspStr);
		log.debug("Finished serving data-request");
//		if (log.isDebugEnabled())
//			log.debug("Response to data-request: " + (rspStr.length() > 32 ? rspStr.substring(0, 32)+"..." : rspStr));
	}

	private ArrayList<HashMap<String, String>> assembleAdrList(String idCode, String name, String surname) throws Exception {
		ArrayList<HashMap<String, String>> ret = new ArrayList<>();

		// Add personal EIC items
		EstfeedAccessor efa = new EstfeedAccessor(su);
		for(UsagePointDetails entry : efa.getUsagePointsList(idCode, "person")) {
			addAddressEntry(ret, idCode, name + " " + surname, entry.address, entry.eic, "ERA", "eraisik");
			efa.getUsageHistory(entry.eic);
		}

		// TODO: remove this fake Personal EIC, which is used for prototyping only
		addAddressEntry(ret, idCode, name + " " + surname, "Suvaline tn 3-2", null, "ERA", "eraisik");

		// EIC items associated with company represenations
		for(OrganizationData repr : new BusinessRegistry().getRepresentations(idCode, su.getAllProps()))
			for(UsagePointDetails entry : efa.getUsagePointsList(repr.code, "company"))
				addAddressEntry(ret, repr.code, repr.name, entry.address, entry.eic, repr.orgTypeCode, repr.orgType);

		return ret;
	}

	private void addAddressEntry(ArrayList<HashMap<String, String>> ret, String code, String name, String address, String eic, String personTypeCode, String personType) {
		HashMap<String, String> entry = new HashMap<>();
		entry.put("code", code);
		entry.put("name", name);
		entry.put("address", address);
		entry.put("eic", new Random().nextInt(1000000000) + "");
		entry.put("personTypeCode", personTypeCode);
		entry.put("personType", personType);
		
		ret.add(entry);
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
			r.put("A1", val*1000/230);
			r.put("A3", val*3000/230);
			cons.add(r);
			start = start.plusHours(1);
		}
		return cons;
	}
}
