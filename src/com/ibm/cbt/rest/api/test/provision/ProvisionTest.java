package com.ibm.cbt.rest.api.test.provision;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.CBTURI;
import com.ibm.cbt.rest.api.test.utilities.CBTUtility;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@SuppressWarnings("deprecation")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvisionTest extends CBTUtility{

	final static String preLoadDataFileName = "resources/provision/_preLoadData.config";
	final static String testDataFileName = "resources/provision/provisionTest.xml";

	static RestTest rt = new RestTest();
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);	
		rt.setDataFile(testDataFileName);	
	}
	
	@Test
	public void test_000_provisionTest() throws Exception {

		rt.setDataLocation("provisionTest", "provisionBody");
	
		String provisionPath = rt.getInputParameter("provisionBody");
		String provisionBody = rt.getMsgBodyfromJson(provisionPath);
		String timestamp = String.valueOf(System.currentTimeMillis());		
		provisionBody = provisionBody.replace("@timestamp", timestamp);	
		
		Response response = given().log().all().contentType("application/json")
				.body(provisionBody)
				.when()
				.post(CBTURI.PROVISION);

		response.then().log().all().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
	    
	}
}
