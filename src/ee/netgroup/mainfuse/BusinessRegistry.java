package ee.netgroup.mainfuse;

import java.text.MessageFormat;
import java.util.ArrayList;

public class BusinessRegistry extends SoapAccessor {

	private static String reprReqTpt;//paringesindus_v3-request template

	public BusinessRegistry() {
		if (reprReqTpt == null) try {
			reprReqTpt = new ServletUtil().readResource("/resources/BusinessRegRepresentation.request");
		}
		catch(Exception x) {
			throw new RuntimeException(x);
		}
	}

	public ArrayList<OrganizationData> getRepresentations(String idCode, Props props) throws Exception {
		String xml = MessageFormat.format(
				reprReqTpt,
				props.get("businessreg.username"),
				props.get("businessreg.password"),
				idCode);
		String serviceUrl = props.get("businessreg.serviceUrl");
		String rs = postSoapRequest(serviceUrl, xml);

		ArrayList<OrganizationData> ret = new ArrayList<>();
		for(String orgData : getImmediateSubitems(rs, "ettevotjad")) {
			OrganizationData od = new OrganizationData();
			od.name = getResponseField(orgData, "arinimi");
			od.code = getResponseField(orgData, "ariregistri_kood");
			od.status = getResponseField(orgData, "staatus");
			od.orgTypeCode = getResponseField(orgData, "oiguslik_vorm");
			od.orgType = getResponseField(orgData, "oiguslik_vorm_tekstina");
			ret.add(od);
		}
		return ret;
	}
}
