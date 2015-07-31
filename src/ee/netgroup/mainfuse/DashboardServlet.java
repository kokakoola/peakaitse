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

	private EstfeedAccessor efa;

	@Override
	public void init() throws ServletException {
		super.init();
		efa = new EstfeedAccessor(su);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.debug("Incoming data-request");
		HashMap<String, Object> rspObj = new HashMap<>();
		try {
			if (su.hasValidSession(req)) {
				String idCode = su.getIdCode(req);
				String name = su.getName(req);
				String surname = su.getSurname(req);

				// Compose customer data and address list
				Collection<HashMap<String, String>> adrlist = assembleAdrList(idCode, name, surname);
				rspObj.put("address", "Tulika põik 7-11");
				rspObj.put("addressList", adrlist);
				rspObj.put("idCode", idCode);
				rspObj.put("name", name);
				rspObj.put("surname", surname);

				// Compose graph data
				if (adrlist.size() > 0) {
					String eic = req.getParameter("eic");
					if (eic == null)
						eic = adrlist.iterator().next().get("eic");
					ArrayList<HashMap<String, Object>> cons = assembleGraphData(eic);
					rspObj.put("eic", eic);
					rspObj.put("graphReadings", cons);
					rspObj.put("recommendedFuseSize", 16);
					rspObj.put("calculatedMinFuseSize", 20);
				}
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
	}

	private Collection<HashMap<String, String>> assembleAdrList(String idCode, String name, String surname) throws Exception {
		TreeMap<String, HashMap<String, String>> ret = new TreeMap<>();//sorted map, sort key is address string

		// Add personal EIC/address pairs
		CustomerDetails cdet = efa.getCustomerData(idCode, "person");
		for(UsagePointDetails entry : cdet.usagePoints)
			addAddressEntry(ret, idCode, name + " " + surname, entry.address, cdet.customerEic+EstfeedAccessor.EIC_SEP+entry.eic, "ERA", "eraisik");

		// EIC items associated with company represenations
		for(OrganizationData repr : new BusinessRegistry().getRepresentations(idCode, su.getAllProps())) {
			cdet = efa.getCustomerData(repr.code, "company");
			for(UsagePointDetails entry : cdet.usagePoints)
				addAddressEntry(ret, repr.code, repr.name, entry.address, cdet.customerEic+EstfeedAccessor.EIC_SEP+entry.eic, repr.orgTypeCode, repr.orgType);
		}

		return ret.values();
	}

	private void addAddressEntry(TreeMap<String, HashMap<String, String>> ret, String code, String name, String address, String eic, String personTypeCode, String personType) {
		HashMap<String, String> entry = new HashMap<>();
		entry.put("code", code);
		entry.put("name", name);
		entry.put("address", address);
		entry.put("eic", eic);
		entry.put("personTypeCode", personTypeCode);
		entry.put("personType", personType);
		
		ret.put(address, entry);
	}

	private ArrayList<HashMap<String, Object>> assembleGraphData(String eic) {
		try {
			ArrayList<HashMap<String, Object>> ret = new ArrayList<>();
			String[] eicPair = eic.split(EstfeedAccessor.EIC_SEP);
			for(ReadingDetails rdet : efa.getUsageHistory(eicPair[0], eicPair[1])) {
				HashMap<String, Object> r = new HashMap<>();
				r.put("time", rdet.period.split("/")[0]);
				r.put("kWh", rdet.value);
				r.put("A1", rdet.value*1000/230);
				r.put("A3", rdet.value*3000/230);
				ret.add(r);
			}
			return ret;
		}
		catch(Exception ce) {
			log.error("Unable to use estfeed data. Will return random data", ce);
			return generateGraphData();
		}
	}

	private ArrayList<HashMap<String, Object>> generateGraphData() {
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
