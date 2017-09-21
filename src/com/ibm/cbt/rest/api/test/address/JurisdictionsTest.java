package com.ibm.cbt.rest.api.test.address;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.CBTErrors;
import com.ibm.cbt.rest.api.test.utilities.CBTURI;
import com.ibm.cbt.rest.api.test.utilities.CBTUtility;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JurisdictionsTest extends CBTUtility{

	final static String testDataFileName = "resources/jurisdictions/jurisdictionsTest.xml";
	private static String access_token;
	static RestTest rt = new RestTest();
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize();
		rt.setDataFile(testDataFileName);
		access_token = CBTUtility.oauth();
	}
	
	@Test
	public void test_000_getAllProvinceTest() throws Exception {
		
		rt.setDataLocation("getAllProvinceTest", "jurstBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.JURST_ALL_PROVINCE);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("name", equalTo(rt.getOutputParameter("name")))
		.body("countryCode", equalTo(rt.getOutputParameter("countryCode")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.sub")
		.body("name[0]", equalTo(rt.getOutputParameter("name1")))
		.body("name[1]", equalTo(rt.getOutputParameter("name2")))
		.body("name[2]", equalTo(rt.getOutputParameter("name3")))
		.body("name[3]", equalTo(rt.getOutputParameter("name4")))
		.body("name[4]", equalTo(rt.getOutputParameter("name5")))
		.body("name[5]", equalTo(rt.getOutputParameter("name6")))
		.body("name[6]", equalTo(rt.getOutputParameter("name7")))
		.body("name[7]", equalTo(rt.getOutputParameter("name8")))
		.body("name[8]", equalTo(rt.getOutputParameter("name9")))
		.body("name[9]", equalTo(rt.getOutputParameter("name10")))
		.body("name[10]", equalTo(rt.getOutputParameter("name11")))
		.body("name[11]", equalTo(rt.getOutputParameter("name12")))
		.body("name[12]", equalTo(rt.getOutputParameter("name13")))
		.body("name[13]", equalTo(rt.getOutputParameter("name14")))
		.body("name[14]", equalTo(rt.getOutputParameter("name15")))
		.body("name[15]", equalTo(rt.getOutputParameter("name16")))
		.body("name[16]", equalTo(rt.getOutputParameter("name17")))
		.body("name[17]", equalTo(rt.getOutputParameter("name18")))
		.body("name[18]", equalTo(rt.getOutputParameter("name19")))
		.body("name[19]", equalTo(rt.getOutputParameter("name20")))
		.body("name[20]", equalTo(rt.getOutputParameter("name21")))
		.body("name[21]", equalTo(rt.getOutputParameter("name22")))
		.body("name[22]", equalTo(rt.getOutputParameter("name23")))
		.body("name[23]", equalTo(rt.getOutputParameter("name24")))
		.body("name[24]", equalTo(rt.getOutputParameter("name25")))
		.body("name[25]", equalTo(rt.getOutputParameter("name26")))
		.body("name[26]", equalTo(rt.getOutputParameter("name27")))
		.body("name[27]", equalTo(rt.getOutputParameter("name28")))
		.body("name[28]", equalTo(rt.getOutputParameter("name29")))
		.body("name[29]", equalTo(rt.getOutputParameter("name30")))
		.body("name[30]", equalTo(rt.getOutputParameter("name31")));
		
	}
	
	@Test
	public void test_001_getSubProvince_CityTest() throws Exception {
		
		rt.setDataLocation("getSubProvinceCityTest", "jurstBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("code", rt.getInputParameter("code"))
				.when()
				.get(CBTURI.JURST_SUB_PROVINCE + "{code}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("provinceCode", equalTo(rt.getInputParameter("code")))
		.body("name", equalTo(rt.getOutputParameter("name")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.sub[0]")
		.body("name", equalTo(rt.getOutputParameter("name")))
		.body("code", equalTo(rt.getOutputParameter("code")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.sub[0].sub")
		.body("name[0]", equalTo(rt.getOutputParameter("name1")))
		.body("name[1]", equalTo(rt.getOutputParameter("name2")))
		.body("name[2]", equalTo(rt.getOutputParameter("name3")))
		.body("name[3]", equalTo(rt.getOutputParameter("name4")))
		.body("name[4]", equalTo(rt.getOutputParameter("name5")))
		.body("name[5]", equalTo(rt.getOutputParameter("name6")))
		.body("name[6]", equalTo(rt.getOutputParameter("name7")))
		.body("name[7]", equalTo(rt.getOutputParameter("name8")))
		.body("name[8]", equalTo(rt.getOutputParameter("name9")))
		.body("name[9]", equalTo(rt.getOutputParameter("name10")))
		.body("name[10]", equalTo(rt.getOutputParameter("name11")))
		.body("name[11]", equalTo(rt.getOutputParameter("name12")))
		.body("name[12]", equalTo(rt.getOutputParameter("name13")))
		.body("name[13]", equalTo(rt.getOutputParameter("name14")))
		.body("name[14]", equalTo(rt.getOutputParameter("name15")))
		.body("name[15]", equalTo(rt.getOutputParameter("name16")));
		
	}
	
	@Test
	public void test_002_getSubProvinceTest() throws Exception {
		
		rt.setDataLocation("getSubProvinceTest", "jurstBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("code", rt.getInputParameter("code"))
				.when()
				.get(CBTURI.JURST_SUB_PROVINCE + "{code}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("provinceCode", equalTo(rt.getInputParameter("code")))
		.body("name", equalTo(rt.getOutputParameter("name")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.sub")
		.body("name[0]", equalTo(rt.getOutputParameter("name1")))
		.body("name[1]", equalTo(rt.getOutputParameter("name2")))
		.body("name[2]", equalTo(rt.getOutputParameter("name3")))
		.body("name[3]", equalTo(rt.getOutputParameter("name4")))
		.body("name[4]", equalTo(rt.getOutputParameter("name5")))
		.body("name[5]", equalTo(rt.getOutputParameter("name6")))
		.body("name[6]", equalTo(rt.getOutputParameter("name7")))
		.body("name[7]", equalTo(rt.getOutputParameter("name8")))
		.body("name[8]", equalTo(rt.getOutputParameter("name9")))
		.body("name[9]", equalTo(rt.getOutputParameter("name10")))
		.body("name[10]", equalTo(rt.getOutputParameter("name11")))
		.body("name[11]", equalTo(rt.getOutputParameter("name12")))
		.body("name[12]", equalTo(rt.getOutputParameter("name13")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.sub[0].sub")
		.body("name[0]", equalTo(rt.getOutputParameter("cityName1")))
		.body("name[1]", equalTo(rt.getOutputParameter("cityName2")))
		.body("name[2]", equalTo(rt.getOutputParameter("cityName3")))
		.body("name[3]", equalTo(rt.getOutputParameter("cityName4")))
		.body("name[4]", equalTo(rt.getOutputParameter("cityName5")))
		.body("name[5]", equalTo(rt.getOutputParameter("cityName6")))
		.body("name[6]", equalTo(rt.getOutputParameter("cityName7")))
		.body("name[7]", equalTo(rt.getOutputParameter("cityName8")))
		.body("name[8]", equalTo(rt.getOutputParameter("cityName9")))
		.body("name[9]", equalTo(rt.getOutputParameter("cityName10")))
		.body("name[10]", equalTo(rt.getOutputParameter("cityName11")))
		.body("name[11]", equalTo(rt.getOutputParameter("cityName12")));
		
	}
	
	@Test
	public void test_003_getSubProvince_invalidCodeTest() throws Exception {
		
		rt.setDataLocation("getSubProvinceTest", "jurstBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.JURST_SUB_PROVINCE + "1");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_SHIP_INVALID_JURISDICTION_CODE, equalTo(CBTErrors.SHIP_INVALID_JURISDICTION_CODE))
		.body("results", hasSize(0));
	}
}
