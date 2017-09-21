package com.ibm.cbt.rest.api.test.passport;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.*;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OauthTest extends CBTUtility{

	final static String testDataFileName = "resources/passport/oauth/oauthTest.xml";
	static RestTest rt = new RestTest();

	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize();
		rt.setDataFile(testDataFileName);
	}
	
	@Test
	public void test_000_oauthTest() throws Exception {

		rt.setDataLocation("oauthTest", "oauthBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.access_token", notNullValue())
		.body("results.expires_in", notNullValue())
		.body("results.token_type", equalTo(rt.getOutputParameter("token_type")))
		.body("results.scope", equalTo(rt.getOutputParameter("scope")));
		
	}
	
	@Test
	public void test_001_invalid_oauthTest() throws Exception {

		rt.setDataLocation("invalidOauthTest", "oauthBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors", hasItem(CBTErrors.INVALID_CREDENTIALS));
		
	}
	
	@Test
	public void test_002_invalid_grantTypeTest() throws Exception {

		rt.setDataLocation("invalidGrantTypeTest", "oauthBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors", hasItem(CBTErrors.INVALID_GRANTTYPE));
		
	}
	
	@Test
	public void test_003_null_grantTypeTest() throws Exception {

		rt.setDataLocation("nullGrantTypeTest", "oauthBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors", hasItem(CBTErrors.NULL_GRANTTYPE));
		
	}
	
	@Test
	public void test_004_null_clientIdTest() throws Exception {

		rt.setDataLocation("nullClientIdTest", "oauthBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors", hasItem(CBTErrors.NULL_CLIENTID));
		
	}
	
	@Test
	public void test_005_null_clientSecretTest() throws Exception {

		rt.setDataLocation("nullclientSecretTest", "oauthBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors", hasItem(CBTErrors.NULL_CLIENTSECRET));
		
	}
}
