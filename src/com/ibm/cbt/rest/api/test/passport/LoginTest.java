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
public class LoginTest extends CBTUtility{
	
	final static String testDataFileName = "resources/passport/login/loginTest.xml";
	final static String preLoadDataFileName = "resources/passport/login/_preLoadData.config";
	static RestTest rt = new RestTest();
	RegisterTest register = new RegisterTest();
	private static String access_token;
	private static String token;

	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);
		rt.setDataFile(testDataFileName);
		access_token = CBTUtility.oauth();
	}
	
	@Test
	public void test_000_loginTest() throws Exception {
		
		rt.setDataLocation("loginTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		loginBody = loginBody.replaceAll("@password", rt.getPreLoadParameter("$oldPassword"));
		
		if(RegisterTest.USERNAME!=null && RegisterTest.USERNAME.length()!=0){
			loginBody = loginBody.replaceAll("@username", RegisterTest.USERNAME);	
			
			Response response = given().log().ifValidationFails().contentType("application/json")
					.header("Authorization", access_token)
					.body(loginBody)
					.when()
					.post(CBTURI.LOGIN);
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true))
			.body("results.accessToken", notNullValue())
			.body("results.username", equalTo(RegisterTest.USERNAME));
			
			token = response.jsonPath().get("results.accessToken");
		}else
		{
			loginBody = loginBody.replace("@username", rt.getPreLoadParameter("$username"));
			Response response = given().log().ifValidationFails().contentType("application/json")
					.header("Authorization", access_token)
					.body(loginBody)
					.when()
					.post(CBTURI.LOGIN);
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true))
			.body("results.accessToken", notNullValue())
			.body("results.username", equalTo(rt.getPreLoadParameter("$username")));
			
			token = response.jsonPath().get("results.accessToken");
		}
		
	}
	
	
	@Test
	public void test_001_wrongUsernameTest() throws Exception {
	
		rt.setDataLocation("wrongUsernameTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_WRONG_USERNAME));	
	}
	
	@Test
	public void test_002_wrongPasswordTest() throws Exception {
		
		rt.setDataLocation("wrongPasswordTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_WRONG_PASSWORD, equalTo(CBTErrors.USER_WRONG_PASSWORD));	
	}
	
	@Test
	public void test_003_requiredField_usernameTest() throws Exception {
		
		rt.setDataLocation("requiredFieldNameTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_REQUIRED_USERNAME));	
	}
	
	@Test
	public void test_004_requiredField_passwordTest() throws Exception {
		
		rt.setDataLocation("requiredFieldPasswordTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_REQUIRED_PASSWORD));	
	}
	
	@Test
	public void test_005_null_usernameTest() throws Exception {
		
		rt.setDataLocation("nullUsernameTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username", hasItem(CBTErrors.USER_REQUIRED_USERNAME));	
	}
	
	@Test
	public void test_006_null_passwordTest() throws Exception {
		
		rt.setDataLocation("nullPasswordTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_REQUIRED_PASSWORD));	
	}
	
	@Test
	public void test_007_oauth_null_LoginTest() throws Exception {

		rt.setDataLocation("loginTest", "loginBody");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		loginBody = loginBody.replace("@username", rt.getPreLoadParameter("$username"));
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_AUTHORIZATION , equalTo(CBTErrors.TOKEN_AUTHORIZATION));	
	}
	
	@Test
	public void test_008_oauth_invalid_LoginTest() throws Exception {

		rt.setDataLocation("invalidOauthTest", "loginBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String token = "Bearer " + response.jsonPath().get("results.access_token");
		
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		loginBody = loginBody.replace("@username", rt.getPreLoadParameter("$username"));
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".username" , hasItem(CBTErrors.USER_WRONG_USERNAME));	
	}
	
	@Test
	public void test_009_reset_passwordTest() throws Exception {

		rt.setDataLocation("resetPasswordTest", "resetPasswordBody");
		
		String resetPasswordBodyPath = rt.getInputParameter("resetPasswordBody");
		String resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		resetPasswordBody = resetPasswordBody.replaceAll("@oldPassword", rt.getPreLoadParameter("$oldPassword"));
		resetPasswordBody = resetPasswordBody.replaceAll("@newPassword", rt.getPreLoadParameter("$newPassword"));
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.message", equalTo(CBTErrors.USER_RESET_PASSWORD));	
		
		//login as new password
		String loginBodyPath = rt.getInputParameter("loginBody");
		String loginBody = rt.getMsgBodyfromJson(loginBodyPath);
		loginBody = loginBody.replace("@password", rt.getPreLoadParameter("$newPassword"));
		
		if(RegisterTest.USERNAME!=null && RegisterTest.USERNAME.length()!=0){
			loginBody = loginBody.replaceAll("@username", RegisterTest.USERNAME);	
			
			response = given().log().ifValidationFails().contentType("application/json")
					.header("Authorization", access_token)
					.body(loginBody)
					.when()
					.post(CBTURI.LOGIN);
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true))
			.body("results.accessToken", notNullValue())
			.body("results.username", equalTo(RegisterTest.USERNAME));
			
		}else
		{
			loginBody = loginBody.replace("@username", rt.getPreLoadParameter("$username"));
			response = given().log().ifValidationFails().contentType("application/json")
					.header("Authorization", access_token)
					.body(loginBody)
					.when()
					.post(CBTURI.LOGIN);
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true))
			.body("results.accessToken", notNullValue())
			.body("results.username", equalTo(rt.getPreLoadParameter("$username")));
			
		}
		
		//reset password back to the old password
		resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		resetPasswordBody = resetPasswordBody.replaceAll("@oldPassword", rt.getPreLoadParameter("$newPassword"));
		resetPasswordBody = resetPasswordBody.replaceAll("@newPassword", rt.getPreLoadParameter("$oldPassword"));
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.message", equalTo(CBTErrors.USER_RESET_PASSWORD));	
	}
	
	@Test
	public void test_010_reset_password_wrongPasswordTest() throws Exception {

		String token = login(access_token);
		rt.setDataLocation("resetPasswordWrongPasswordTest", "resetPasswordBody");
		
		String resetPasswordBodyPath = rt.getInputParameter("resetPasswordBody");
		String resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_RESET_PASSWORD_WRONG_PASSWORD, equalTo(CBTErrors.USER_RESET_PASSWORD_WRONG_PASSWORD));	
	}
	
	@Test
	public void test_011_reset_password_confirmPasswordTest() throws Exception {

		String token = login(access_token);
		rt.setDataLocation("resetPasswordConfirmPasswordTest", "resetPasswordBody");
		
		String resetPasswordBodyPath = rt.getInputParameter("resetPasswordBody");
		String resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_UNMATCH_PASSWORD));
	}
	
	@Test
	public void test_012_reset_password_required_passwordTest() throws Exception {

		String token = login(access_token);
		rt.setDataLocation("resetPasswordRequiredPasswordTest", "resetPasswordBody");
		
		String resetPasswordBodyPath = rt.getInputParameter("resetPasswordBody");
		String resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".oldPassword", hasItem(CBTErrors.USER_RESET_PASSWORD_REQUIRED_PASSWORD));
	}
	
	@Test
	public void test_013_reset_password_required_newPasswordTest() throws Exception {

		String token = login(access_token);
		rt.setDataLocation("resetPasswordRequiredNewPasswordTest", "resetPasswordBody");
		
		String resetPasswordBodyPath = rt.getInputParameter("resetPasswordBody");
		String resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".password", hasItem(CBTErrors.USER_RESET_PASSWORD_REQUIRED_NEWPASSWORD))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_UNMATCH_PASSWORD));
	}
	
	@Test
	public void test_014_reset_password_required_confirmPasswordTest() throws Exception {

		String token = login(access_token);
		rt.setDataLocation("resetPasswordRequiredConfirmPasswordTest", "resetPasswordBody");
		
		String resetPasswordBodyPath = rt.getInputParameter("resetPasswordBody");
		String resetPasswordBody = rt.getMsgBodyfromJson(resetPasswordBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(resetPasswordBody)
				.when()
				.post(CBTURI.PASSWORD + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".confirmPassword", hasItem(CBTErrors.USER_RESET_PASSWORD_REQUIRED_CONFIRMPASSWORD));
	}
	
	@Test
	public void test_015_logoutTest() throws Exception {

		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.post(CBTURI.LOGOUT + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", equalTo(true));
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.post(CBTURI.LOGOUT + "?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors", hasItem(CBTErrors.NEED_LOGIN))
		.body("results", hasSize(0));
	}
}
