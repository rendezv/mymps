package com.litech.mymps;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

//https://blog.jayway.com/2010/05/21/introduction-to-snmp4j/
// https://github.com/soulwing/tnm4j
// http://www.snmp4j.org/doc/org/snmp4j/Snmp.html
public class SimpleSnmpClient {
	
	private String address;
	private Snmp snmp;	
	
	static int defaultPort = 161;
	static String defaultCommunitystring = "public";
    //static String defaultIP = "127.0.0.1";
    //static String defaultOID = "1.3.6.1.4.1.14586.100.77.1";
    //static String defaultOID = "1.3.6.1.2";
	static int defaultRetries = 2;
	static int defaultTimeout = 1500;
	static int defaultVersion = SnmpConstants.version1;

    public SimpleSnmpClient(String address) {
		super();
		this.address = address;
		try {
			start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
	private void start() throws IOException {
		TransportMapping<?> transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		// Do not forget this line!
		transport.listen();
	}
	// Since snmp4j relies on asynch req/resp we need a listener
		// for responses which should be closed
		public void stop() throws IOException {
			snmp.close();
		}

	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(defaultCommunitystring));
		target.setAddress(targetAddress);
		target.setRetries(defaultRetries);
		target.setTimeout(defaultTimeout);
		target.setVersion(defaultVersion);
		return target;
	}
	
	public String getAsString(OID oid) throws IOException {
		ResponseEvent response = get(new OID[]{oid});
		// extract the response PDU (could be null if timed out)
		PDU responsePDU = response.getResponse();		
		//Address peerAddress = response.getPeerAddress();

		if (responsePDU == null) {
			System.out.println("responsePdu null");
			return null;
		}
		else
			return responsePDU.get(0).getVariable().toString();
		//return event.getResponse().get(0).getVariable().toString();
	}
	/*
	private PDU getPDU(OID oids[]) {
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
	 	   
		pdu.setType(PDU.GET);
		return pdu;
	}*/


	public ResponseEvent get(OID oids[]) throws IOException {
		PDU pdu = new PDU();
	 	for (OID oid : oids) {
	 	     pdu.add(new VariableBinding(oid));
	 	}
	 	pdu.setType(PDU.GET);
	 	ResponseEvent event = snmp.send(pdu, getTarget(), null);
		if(event != null) {
			return event;
		}
		throw new RuntimeException("GET timed out");
	}
	
	public void getAsync(Collection<OID> oids) throws IOException {
		PDU pdu = new PDU();
	 	for (OID oid : oids) {
	 	     pdu.add(new VariableBinding(oid));
	 	}
	 	pdu.setType(PDU.GET);
	 	
	 	// sending request
	 	ResponseListener listener = new ResponseListener() {
	 	     public void onResponse(ResponseEvent event) {
	 	       // Always cancel async request when response has been received
	 	       // otherwise a memory leak is created! Not canceling a request
	 	       // immediately can be useful when sending a request to a broadcast
	 	       // address.
	 	       ((Snmp)event.getSource()).cancel(event.getRequest(), this);
	 	       
	 	       System.out.println("Received response PDU is: "+event.getResponse());
	 	     }
	 	   };
	 	   
	 	snmp.send(pdu, getTarget(), null, listener);

	 	System.out.println("sent");
		

	}
	
	/**
	 * Normally this would return domain objects or something else than this...
	 */
	public List<List<String>> getTableAsStrings(OID[] oids) {
		TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());
		
		
		List<TableEvent> events = tUtils.getTable(getTarget(), oids, null, null);
		
		List<List<String>> list = new ArrayList<List<String>>();
		for (TableEvent event : events) {
			if(event.isError()) {
				throw new RuntimeException(event.getErrorMessage());
			}
			List<String> strList = new ArrayList<String>();
			list.add(strList);
			for(VariableBinding vb: event.getColumns()) {
				strList.add(vb.getVariable().toString());
			}
		}
		return list;
	}
	
	public static String extractSingleString(ResponseEvent event) {
		return event.getResponse().get(0).getVariable().toString();
	}    
    
	/*
    public static void main(String[] args) {
    }
    */

}
