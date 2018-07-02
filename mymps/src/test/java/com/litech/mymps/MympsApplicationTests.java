package com.litech.mymps;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snmp4j.smi.OID;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MympsApplicationTests {

	@Test
	public void contextLoads() {
	}
	
	@Test
	public void test1() {
		SimpleSnmpClient client = new SimpleSnmpClient("udp:127.0.0.1/161");
		String sysDescr;
		OID oidSysDescr = new OID(".1.3.6.1.2.1.1.1");
		oidSysDescr.append(0);
		
		try {
			sysDescr = client.getAsString(oidSysDescr);
			System.out.println(sysDescr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			client.getAsync(Stream.of (oidSysDescr).collect(Collectors.toList()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
