package com.ibm.cbt.rest.api.test.address;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.CBTURI;
import com.ibm.cbt.rest.api.test.utilities.CBTUtility;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShipmentTest extends CBTUtility{

	final static String testDataFileName = "resources/shipment/shipmentTest.xml";
	final static String preLoadDataFileName = "resources/shipment/_preLoadData.config";
	private static String access_token;
	static RestTest rt = new RestTest();
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);
		rt.setDataFile(testDataFileName);
		access_token = CBTUtility.oauth();
	}
	
	@Test
	public void test_000_getShipProviderTest() throws Exception {
		
		rt.setDataLocation("getShipProviderTest", "shipmentBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SHIP_PROVIDERS);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.ship[0]")
		.body("id", notNullValue())
		.body("name", equalTo(rt.getOutputParameter("name")))
		.body("shopId", equalTo(rt.getPreLoadParameter("$orgId")))
		.body("sequence", equalTo(rt.getOutputParameter("sequence")))
		.body("isDefault", equalTo(rt.getOutputParameter("isDefault")));
	}
}
