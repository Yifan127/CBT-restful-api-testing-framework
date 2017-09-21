package com.ibm.cbt.rest.api.test.address;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.*;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AddressTest extends CBTUtility{
	
	final static String testDataFileName = "resources/address/addressTest.xml";
	private static String token;
	private static String access_token;
	static RestTest rt = new RestTest();
	private static String addressId1 = null;
	private static String addressId2 = null;
	

	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize();
		rt.setDataFile(testDataFileName);
		access_token = CBTUtility.oauth();
		token = login(access_token);
		
		//get current address list size
		Response response_list = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response_list.jsonPath().get("results.addressId");
		//delete address by id	
		if(addressList.size()!= 0)
		{
			for(int i=0; i<addressList.size();i++)
			{
				Response response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.pathParam("addressId", addressList.get(i))
						.when()
						.delete(CBTURI.ADDRESS + "/{addressId}?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true));	
			}			
		}
	}
	
	@Test
	public void test_000_addAddressTest() throws Exception {
		
		rt.setDataLocation("addAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", notNullValue())
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", equalTo((rt.getOutputParameter("stateName"))))
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", equalTo((rt.getOutputParameter("cityName"))))
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", equalTo((rt.getOutputParameter("districtName"))))
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", equalTo((rt.getOutputParameter("receiverPhone"))));
	
		addressId1 = response.jsonPath().getString("results.addressId");
		
	}
	
	@Test
	public void test_001_getAddressTest() throws Exception {
		
		rt.setDataLocation("addAddressTest", "addressBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId1)
				.when()
				.get(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", equalTo(addressId1))
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", equalTo((rt.getOutputParameter("stateName"))))
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", equalTo((rt.getOutputParameter("cityName"))))
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", equalTo((rt.getOutputParameter("districtName"))))
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", equalTo((rt.getOutputParameter("receiverPhone"))));
	}
	
	@Test
	public void test_002_getAddressListTest() throws Exception {
		
		rt.setDataLocation("getAddressListTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		//add another address,set default = true
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		addressId2 = response.jsonPath().getString("results.addressId");
		
		//verify isDefault of addressId2 is true
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", notNullValue())
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))));
		
		//verify isDefault of addressId1 is false
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId1)
				.when()
				.get(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", equalTo(addressId1))
		.body("isDefault", equalTo(false));
		
		//get address list
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
			
		ArrayList<String> addressList = response.jsonPath().get("results");
		Assert.assertTrue(addressList.size()==2);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", hasItem(addressId1))
		.body("addressId", hasItem(addressId2));	
	}
	
	@Test
	public void test_003_setDefaultAddressTest() throws Exception {
		
		rt.setDataLocation("setDefaultAddressTest", "addressBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId1)
				.when()
				.put(CBTURI.ADDRESS_DEFAULT + "/{addressId}?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", equalTo(addressId1))
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", equalTo((rt.getOutputParameter("stateName"))))
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", equalTo((rt.getOutputParameter("cityName"))))
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", equalTo((rt.getOutputParameter("districtName"))))
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", equalTo((rt.getOutputParameter("receiverPhone"))));
	
	}
	
	@Test
	public void test_004_getDefaultAddressTest() throws Exception {
		
		rt.setDataLocation("getDefaultAddressTest", "addressBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS_DEFAULT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", equalTo(addressId1))
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", equalTo((rt.getOutputParameter("stateName"))))
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", equalTo((rt.getOutputParameter("cityName"))))
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", equalTo((rt.getOutputParameter("districtName"))))
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", equalTo((rt.getOutputParameter("receiverPhone"))));
	}
	
	@Test
	public void test_005_updateAddressTest() throws Exception {
		
		rt.setDataLocation("updateAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		//update addressId1, set default = false
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId1)
				.body(addressBody)
				.when()
				.put(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", equalTo(addressId1))
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", equalTo((rt.getOutputParameter("stateName"))))
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", equalTo((rt.getOutputParameter("cityName"))))
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", equalTo((rt.getOutputParameter("districtName"))))
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", equalTo((rt.getOutputParameter("receiverPhone"))));
	
		//verify addressId2 is default address
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId2)
				.when()
				.get(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", equalTo(addressId2))
		.body("isDefault", equalTo(false));
	}
	
	@Test
	public void test_006_deleteAddressTest() throws Exception {
		
		//get original address list size
		Response response_list = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response_list.jsonPath().get("results");
		int size_before = addressList.size();
		//delete address by id		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId1)
				.when()
				.delete(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");	
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", equalTo(addressId1));	
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("addressId", addressId2)
				.when()
				.delete(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", equalTo(addressId2));	
			
		//verify current address list size
		response_list = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		addressList = response_list.jsonPath().get("results");
		int size_after = addressList.size();
		Assert.assertEquals(size_after+2, size_before);
	}
	
	@Test
	public void test_007_nonRequiredField_addAddressTest() throws Exception {
		
		rt.setDataLocation("nonRequiredFieldAddAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", notNullValue())
		.body("isDefault", equalTo(false))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", nullValue())
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", nullValue())
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", nullValue())
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", nullValue());
		
	}
	
	@Test
	public void test_008_requiredField_stateCodeTest() throws Exception {
		
		rt.setDataLocation("requiredFieldStateCodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".stateCode", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_009_requiredField_cityCodeTest() throws Exception {
		
		rt.setDataLocation("requiredFieldCityCodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".cityCode", hasItem(CBTErrors.REQUIRED_FIELD));			
	}
	
	@Test
	public void test_010_requiredField_districtCodeTest() throws Exception {
		
		rt.setDataLocation("requiredFieldDistrictCodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".districtCode", hasItem(CBTErrors.REQUIRED_FIELD));	
	}
	
	@Test
	public void test_011_requiredField_addressTest() throws Exception {
		
		rt.setDataLocation("requiredFieldAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".address", hasItem(CBTErrors.REQUIRED_FIELD));	
	}
	
	@Test
	public void test_012_requiredField_receiverNameTest() throws Exception {

		rt.setDataLocation("requiredFieldReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverName", hasItem(CBTErrors.REQUIRED_FIELD));	
	}

	@Test
	public void test_013_requiredField_receiverMobileTest() throws Exception {
		
		rt.setDataLocation("requiredFieldReceiverMobileTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverMobile", hasItem(CBTErrors.REQUIRED_FIELD));	
	}
	
	@Test
	public void test_014_requiredField_postcodeTest() throws Exception {

		rt.setDataLocation("requiredFieldPostcodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".postcode", hasItem(CBTErrors.REQUIRED_FIELD));		
	}
	
	@Test
	public void test_015_null_stateCodeTest() throws Exception {
		
		rt.setDataLocation("nullStateCodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".stateCode", hasItem(CBTErrors.REQUIRED_FIELD));		
	}
	
	@Test
	public void test_016_null_cityCodeTest() throws Exception {
		
		rt.setDataLocation("nullCityCodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".cityCode", hasItem(CBTErrors.REQUIRED_FIELD));		
	}
	
	@Test
	public void test_017_null_districtCodeTest() throws Exception {
		
		rt.setDataLocation("nullDistrictCodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".districtCode", hasItem(CBTErrors.REQUIRED_FIELD));	
	}
	
	@Test
	public void test_018_null_addressTest() throws Exception {

		rt.setDataLocation("nullAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".address", hasItem(CBTErrors.REQUIRED_FIELD));		
	}
	
	@Test
	public void test_019_null_receiverNameTest() throws Exception {
		
		rt.setDataLocation("nullReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverName", hasItem(CBTErrors.REQUIRED_FIELD));	
	}

	@Test
	public void test_020_null_receiverMobileTest() throws Exception {
		
		rt.setDataLocation("nullReceiverMobileTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverMobile", hasItem(CBTErrors.REQUIRED_FIELD));			
	}
	
	@Test
	public void test_021_null_postcodeTest() throws Exception {
		
		rt.setDataLocation("nullPostcodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".postcode", hasItem(CBTErrors.REQUIRED_FIELD));		
	}
	
	@Test
	public void test_022_wrongType_postcodeTest() throws Exception {
		
		rt.setDataLocation("wrongTypePostcodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".postcode", hasItem(CBTErrors.ADDRESS_INVALID_POSTCODE));		
	}
	
	@Test
	public void test_023_wrongType_AddressTest() throws Exception {
		
		rt.setDataLocation("wrongTypeAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".address", hasItem(CBTErrors.ADDRESS_ADDRESS));	
	}
	
	@Test
	public void test_024_wrongType_receiverMobileTest() throws Exception {
		
		rt.setDataLocation("wrongTypeReceiverMobileTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverMobile", hasItem(CBTErrors.ADDRESS_INVALID_RECEIVERMOBILE));	
	}
	
	
	@Test
	public void test_025_wrongType_receiverNameTest() throws Exception {
		
		rt.setDataLocation("wrongTypeReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverName", hasItem(CBTErrors.ADDRESS_RECEIVERNAME));			
	}
	//10-200
	@Test
	public void test_026_min_boundary_addressTest() throws Exception {
		
		rt.setDataLocation("minBoundaryAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".address", hasItem(CBTErrors.ADDRESS_ADDRESS));			
	}
	
	@Test
	public void test_027_max_boundary_addressTest() throws Exception {
		
		rt.setDataLocation("maxBoundaryAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".address", hasItem(CBTErrors.ADDRESS_ADDRESS));			
	}
	
	//2-10
	@Test
	public void test_028_min_boundary_receiverNameTest() throws Exception {
		
		rt.setDataLocation("minBoundaryReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverName", hasItem(CBTErrors.ADDRESS_RECEIVERNAME));	

	}
	
	@Test 
	public void test_029_max_boundary_receiverNameTest() throws Exception {
		
		rt.setDataLocation("maxBoundaryReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverName", hasItem(CBTErrors.ADDRESS_RECEIVERNAME));	
	
	}
	
	@Test
	public void test_030_validation_postcodeTest() throws Exception {
		
		rt.setDataLocation("validationPostcodeTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".postcode", hasItem(CBTErrors.ADDRESS_VALIDATION_POSTCODE));			
	}
	
	@Test
	public void test_031_validation_receiverMobileTest() throws Exception {
		
		rt.setDataLocation("validationReceiverMobileTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverMobile", hasItem(CBTErrors.ADDRESS_VALIDATION_RECEIVERMOBILE));	
	}
	
	@Test
	public void test_032_validation_receiverPhoneTest() throws Exception {
		
		rt.setDataLocation("validationReceiverPhoneTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverPhone", hasItem(CBTErrors.ADDRESS_VALIDATION_RECEIVERPHONE));	
	}
	
	@Test
	public void test_033_exceedMaxAddressTest() throws Exception {
		
		//get current address list size
		Response response_list = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response_list.jsonPath().get("results.address");
		int size = addressList.size();
		//add address to 10	
        rt.setDataLocation("exceedMaxAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		for(int i=0; i<10-size; i++)
		{
			Response response = given().log().ifValidationFails().contentType("application/json")
					.header("Authorization", access_token)
					.pathParam("token", token)
					.body(addressBody)
					.when()
					.post(CBTURI.ADDRESS + "?=token={token}");
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true));
		}
		
		//add the 11 address
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_EXCEED_MAX_ADDRESS, containsString(CBTErrors.ADDRESS_EXCEED_MAX));
	
		//remove all addresses
		response_list = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		addressList = response_list.jsonPath().get("results.addressId");
		//delete
		if(addressList.size()!= 0)
		{
			for(int i=0; i<addressList.size();i++)
			{
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.pathParam("addressId", addressList.get(i))
						.when()
						.delete(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true));	
			}			
		}
		
	}
	
	@Test
	public void test_034_invalidId_getAddressTest() throws Exception {
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "/1?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_ADDRESS_NOT_EXIST, containsString(CBTErrors.ADDRESS_NOT_EXIST))
		.body("results", hasSize(0));
		
	}
	
	@Test
	public void test_035_invalidId_setDefaultAddressTest() throws Exception {
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.put(CBTURI.ADDRESS_DEFAULT + "/1?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_ADDRESS_NOT_EXIST, containsString(CBTErrors.ADDRESS_NOT_EXIST))
		.body("results", hasSize(0));
	}
	
	@Test
	public void test_036_authorizationTest() throws Exception {
		
        rt.setDataLocation("addAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors", hasItem(CBTErrors.NEED_LOGIN));
	}
	
	@Test
	public void test_037_oauth_null_AddressTest() throws Exception {
		
        rt.setDataLocation("addAddressTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_AUTHORIZATION , equalTo(CBTErrors.TOKEN_AUTHORIZATION));	
	}
	
	@Test
	public void test_038_oauth_invalid_AddressTest() throws Exception {
		
        rt.setDataLocation("invalidOauthTest", "addressBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_TOKEN_NOT_MATCH , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	@Test
	public void test_039_multiTenant_Test() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "addressBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		String userToken = login("autoTest4", "Test123_", accessToken);
		
		//add address to tenant user A
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		addressId1 = response.jsonPath().getString("results.addressId");
		
		//get address - Tenant User B get Tenant-A address
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.pathParam("addressId", addressId1)
				.when()
				.get(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_ADDRESS_NOT_EXIST, containsString(CBTErrors.ADDRESS_NOT_EXIST))
		.body("results", hasSize(0));
	}
	
	@Test
	public void test_040_invalid_specialCharacters_receiverNameTest() throws Exception {
		
		rt.setDataLocation("invalidSpecialReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".receiverName", hasItem(CBTErrors.ADDRESS_RECEIVERNAME));	

	}
	
	@Test
	public void test_041_valid_specialCharacters_receiverNameTest() throws Exception {
		
		rt.setDataLocation("validSpecialReceiverNameTest", "addressBody");
		
		String addressBodyPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("addressId", notNullValue())
		.body("isDefault", equalTo(Boolean.parseBoolean(rt.getOutputParameter("isDefault"))))
		.body("stateCode", equalTo((rt.getOutputParameter("stateCode"))))
		.body("stateName", equalTo((rt.getOutputParameter("stateName"))))
		.body("cityCode", equalTo((rt.getOutputParameter("cityCode"))))
		.body("cityName", equalTo((rt.getOutputParameter("cityName"))))
		.body("districtCode", equalTo((rt.getOutputParameter("districtCode"))))
		.body("districtName", equalTo((rt.getOutputParameter("districtName"))))
		.body("postcode", equalTo((rt.getOutputParameter("postcode"))))
		.body("address", equalTo((rt.getOutputParameter("address"))))
		.body("receiverName", equalTo((rt.getOutputParameter("receiverName"))))
		.body("receiverMobile", equalTo((rt.getOutputParameter("receiverMobile"))))
		.body("receiverPhone", equalTo((rt.getOutputParameter("receiverPhone"))));

	}
	
	@AfterClass
	public static void cleanData() throws Exception {
		
//		String token = login(access_token);
		//get current address list size
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response.jsonPath().get("results.addressId");
		//clean address
		if(addressList.size()!= 0)
		{
			for(int i=0; i<addressList.size();i++)
			{
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.pathParam("addressId", addressList.get(i))
						.when()
						.delete(CBTURI.ADDRESS + "/{addressId}?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true));	
			}			
		}
	}
}
