package com.ibm.cbt.rest.api.test.passport;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import com.jayway.restassured.response.Response;
import com.ibm.cbt.rest.api.test.utilities.CBTErrors;
import com.ibm.cbt.rest.api.test.utilities.CBTURI;
import com.ibm.cbt.rest.api.test.utilities.CBTUtility;
import com.ibm.cbt.rest.test.restassured.RestTest;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.*;
import org.junit.Test;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegisterTest {
	
	final static String testDataFileName = "resources/passport/register/registerTest.xml";
	final static String preLoadDataFileName = "resources/passport/register/_preLoadData.config";
	static RestTest rt = new RestTest();
	public static String USERNAME;
	private static String access_token;

	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);
		rt.setDataFile(testDataFileName);
		access_token = CBTUtility.oauth();
	}
	
	@Test
	public void test_000_registerTest() throws Exception {
		
		rt.setDataLocation("registerTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		String timestamp = String.valueOf(System.currentTimeMillis());		
		registerBody = registerBody.replace("@timestamp", timestamp);	
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.accessToken", notNullValue())
		.body("results.username", equalTo("test" + timestamp));
		
		USERNAME = response.jsonPath().get("results.username");
	}
	
	@Test
	public void test_001_duplicatedRegisterTest() throws Exception {
		
		rt.setDataLocation("duplicatedRegisterTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		registerBody = registerBody.replaceAll("@username", rt.getPreLoadParameter("$username"));
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_NAME_ALREADY_USED));	
	}
	
	@Test
	public void test_002_unmatchPasswordTest() throws Exception {
		
		rt.setDataLocation("unMatchPasswordTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		registerBody = registerBody.replace("@timestamp", String.valueOf(System.currentTimeMillis()));	
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_UNMATCH_PASSWORD));	
	}
	
	@Test
	public void test_003_invalidUsername_mobileTest() throws Exception {
		
		rt.setDataLocation("invalidUsernameMobileTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_INVALID_USERNAME, containsString(CBTErrors.USER_INVALID_USERNAME));	
	}
	
	@Test
	public void test_004_invalidUsername_emailTest() throws Exception {
		
		rt.setDataLocation("invalidUsernameEmailTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
			
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors."  + CBTErrors.CODE_INVALID_USERNAME, containsString(CBTErrors.USER_INVALID_USERNAME));
	}
	
	@Test
	public void test_005_requiredField_usernameTest() throws Exception {
		
		rt.setDataLocation("requiredFieldUsernameTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_REQUIRED_USERNAME));	
	}
	
	@Test
	public void test_006_requiredField_passwordTest() throws Exception {
		
		rt.setDataLocation("requiredFieldPasswordTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		registerBody = registerBody.replace("@timestamp", String.valueOf(System.currentTimeMillis()));	
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_REQUIRED_PASSWORD))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_UNMATCH_PASSWORD));	
	}
	
	@Test
	public void test_007_requiredField_confirmPasswordTest() throws Exception {
		
		rt.setDataLocation("requiredFieldConfirmPasswordTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		registerBody = registerBody.replace("@timestamp", String.valueOf(System.currentTimeMillis()));	
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_REQUIRED_CONFIRMPASSWORD));	
	}
	
	@Test
	public void test_008_null_usernameTest() throws Exception {
		
		rt.setDataLocation("nullUsernameTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_REQUIRED_USERNAME));	
	}
	
	@Test
	public void test_009_null_passwordTest() throws Exception {
		
		rt.setDataLocation("nullPasswordTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		registerBody = registerBody.replace("@timestamp", String.valueOf(System.currentTimeMillis()));	
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_REQUIRED_PASSWORD))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_UNMATCH_PASSWORD));	
	}
	
	@Test
	public void test_010_null_confirmPasswordTest() throws Exception {
		
		rt.setDataLocation("nullConfirmPasswordTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		registerBody = registerBody.replace("@timestamp", String.valueOf(System.currentTimeMillis()));	
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_REQUIRED_CONFIRMPASSWORD));	
	}
	
	//username boundary max - 20
	@Test
	public void test_011_boundary_max_usernameTest() throws Exception {
		
		rt.setDataLocation("registerUsernameMaxBoundaryTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_NAME_MAX));	
	}
	
	//username boundary min - 6
	@Test
	public void test_012_boundary_min_usernameTest() throws Exception {
		
		rt.setDataLocation("registerUsernameMinBoundaryTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_NAME_MIN));	
	}
	
	//password boundary max - 20
	@Test
	public void test_013_boundary_max_passwordTest() throws Exception {
		
		rt.setDataLocation("registerPasswordMaxBoundaryTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_PASSWORD_MAX));	
	}
	
	//password boundary min - 6
	@Test
	public void test_014_boundary_min_passwordTest() throws Exception {
		
		rt.setDataLocation("registerPasswordMinBoundaryTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_PASSWORD_MIN));	
	}
	
	@Test
	public void test_015_oauth_registerTest() throws Exception {
		
		rt.setDataLocation("registerTest", "registerBody");
		
		String registerBodyPath = rt.getInputParameter("registerBody");
		String registerBody = rt.getMsgBodyfromJson(registerBodyPath);
		
		String timestamp = String.valueOf(System.currentTimeMillis());		
		registerBody = registerBody.replace("@timestamp", timestamp);	
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(registerBody)
				.when()
				.post(CBTURI.REGISTER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_AUTHORIZATION , equalTo(CBTErrors.TOKEN_AUTHORIZATION));	
	}
}
