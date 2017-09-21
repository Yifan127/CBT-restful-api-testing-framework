package com.ibm.cbt.rest.api.test.order;

import java.util.ArrayList;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.*;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import com.jayway.restassured.response.Response;
import com.ibm.cbt.rest.api.test.utilities.*;
import com.ibm.cbt.rest.test.restassured.RestTest;


/**
 * @author yifzhang
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrderTest extends CBTUtility{
	
	final static String preLoadDataFileName = "resources/order/_preLoadData.config";
	final static String testDataFileName = "resources/order/orderTest.xml";
	private static String token;
	private static String access_token;
	static RestTest rt = new RestTest();
	
	private static String cartlineId = "";
	private static String addressId = "";
	private static String orderId = "";
	private static String itemId1 = "";
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);	
		rt.setDataFile(testDataFileName);	
		access_token = CBTUtility.oauth();
		token = login(access_token);
		//get item id
		itemId1 = CBTUtility.getItemId1(access_token);
		
		//get current address list size
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response.jsonPath().get("results.addressId");
		//clear address
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
		//add address
		rt.setDataLocation("setup", "orderBody");
		String addressPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressPath);
		addressId = CBTUtility.addAddress(access_token,token,addressBody);
				
		//clear cartline
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART + "?=token={token}");
		
		ArrayList<String> l = response.jsonPath().get("results");	
		if(l!=null && l.size()!=0)
		{
			ArrayList<String> cartLineIdList = response.jsonPath().get("results.cartlineId");	
			
			for(int i=0; i<cartLineIdList.size();i++ )
			{
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.when()
						.delete(CBTURI.CART + "/" + cartLineIdList.get(i) + "?=token={token}");
			}
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true));
		}	

	}
	
	@Test
	public void test_000_submitOrderTest() throws Exception {
		
		rt.setDataLocation("sumbitOrderTest", "orderBody");

		//get inventory before submit order
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", itemId1)
				.when()
				.get(CBTURI.PRODUCT_REALTIME + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		Integer inventory = response.jsonPath().getInt("results.inventory");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		//verify inventory after submit order
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", itemId1)
				.when()
				.get(CBTURI.PRODUCT_REALTIME + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.inventory", equalTo(inventory-1));

	}
	
	@Test(timeout=10000) 
	public void test_001_getOrder_byOrderIdTest() throws Exception {
		
		rt.setDataLocation("getOrderByOrderIdTest", "orderBody");
		//get order by id
		while(true){	
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
		if(status)
		{
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results")
			.body("orderId", equalTo(orderId))
			.body("buyerId", notNullValue())
			.body("orderComment", equalTo(rt.getOutputParameter("orderComment")))
			.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
			.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
			.body("createTimestamp", notNullValue())
			.body("updateTimestamp", notNullValue());
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.shippingInfo")
			.body("shippingAddress", equalTo(rt.getOutputParameter("shippingAddress")))
			.body("shippingReceiverName", equalTo(rt.getOutputParameter("shippingReceiverName")))
			.body("shippingReceiverMobile", equalTo(rt.getOutputParameter("shippingReceiverMobile")))
			.body("shippingReceiverPhone", equalTo(rt.getOutputParameter("shippingReceiverPhone")))
			.body("shippingReceiverEmail", equalTo(rt.getOutputParameter("shippingReceiverEmail")))
			.body("shippingCountryCode", equalTo(rt.getOutputParameter("shippingCountryCode")))
			.body("shippingCountryName", equalTo(rt.getOutputParameter("shippingCountryName")))
			.body("shippingStateCode", equalTo(rt.getOutputParameter("shippingStateCode")))
			.body("shippingStateName", equalTo(rt.getOutputParameter("shippingStateName")))
			.body("shippingCityCode", equalTo(rt.getOutputParameter("shippingCityCode")))
			.body("shippingCityName", equalTo(rt.getOutputParameter("shippingCityName")))
			.body("shippingDistrictCode", equalTo(rt.getOutputParameter("shippingDistrictCode")))
			.body("shippingDistrictName", equalTo(rt.getOutputParameter("shippingDistrictName")))
			.body("shippingPostcode", equalTo(rt.getOutputParameter("shippingPostcode")));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.orderPriceInfo")
			.body("shippingOriginalPrice", equalTo(rt.getOutputParameter("shippingOriginalPrice")))
			.body("shippingPrice", equalTo(rt.getOutputParameter("shippingPrice")))
			.body("taxPrice", equalTo(rt.getOutputParameter("taxPrice")))
			.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
			.body("itemsPrice", equalTo(rt.getOutputParameter("itemsPrice")))
			.body("adjustmentPrice", equalTo(rt.getOutputParameter("adjustmentPrice")));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("results.paymentInfo[0]", Matchers.hasSize(0));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.orderLineList")
			.body("orderLineId[0]", notNullValue())
			.body("orderId[0]", equalTo(orderId))
			.body("itemId[0]", equalTo(itemId1))
			.body("quantity[0]", equalTo(Integer.parseInt(rt.getOutputParameter("quantity"))))
			.body("itemDisplayText[0]", equalTo(rt.getOutputParameter("itemDisplayText")))
			.body("itemImageLink[0]", equalTo(rt.getOutputParameter("itemImageLink")))
			.body("unitPrice[0]", equalTo(rt.getOutputParameter("unitPrice")))
			.body("tax[0]", equalTo(rt.getOutputParameter("tax")))
			.body("adjustment[0]", equalTo(rt.getOutputParameter("adjustment")));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.orderHistory")
			.body("createTimestamp[0]", notNullValue())
			.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId")))
			.body("description[0]", equalTo(rt.getOutputParameter("description")));
			break;
		}
	 }	
	}
	
	@Test
	public void test_002_confirmReceivedOrderTest() throws Exception {

		rt.setDataLocation("confirmReceivedOrderTest", "orderBody");
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);		
		orderBody = orderBody.replace("@orderId", orderId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody)
				.when()
				.post(CBTURI.ORDER_CONFIRM_RECEIVE + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", equalTo(orderId))
		.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
	}
	
	@Test(timeout=10000)
	public void test_003_submitOrder_carrierIdTest() throws Exception {
		
		rt.setDataLocation("sumbitOrderCarrierIdTest", "orderBody");	
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//get carrierId
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SHIP_PROVIDERS);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		String carrierId = response.jsonPath().get("results.ship[0].id");	
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderBody = orderBody.replaceAll("@carrierId", carrierId);
		
		String orderId = CBTUtility.submitOrder(access_token,token,orderBody);
		
		//get order by id
		while(true){	
			response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
		if(status)
		{
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results")
			.body("orderId", equalTo(orderId))
			.body("buyerId", notNullValue())
			.body("orderComment", equalTo(rt.getOutputParameter("orderComment")))
			.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
			.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
			.body("createTimestamp", notNullValue())
			.body("updateTimestamp", notNullValue());
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.shippingInfo")
			.body("shippingAddress", equalTo(rt.getOutputParameter("shippingAddress")))
			.body("shippingReceiverName", equalTo(rt.getOutputParameter("shippingReceiverName")))
			.body("shippingReceiverMobile", equalTo(rt.getOutputParameter("shippingReceiverMobile")))
			.body("shippingReceiverPhone", equalTo(rt.getOutputParameter("shippingReceiverPhone")))
			.body("shippingReceiverEmail", equalTo(rt.getOutputParameter("shippingReceiverEmail")))
			.body("shippingCountryCode", equalTo(rt.getOutputParameter("shippingCountryCode")))
			.body("shippingCountryName", equalTo(rt.getOutputParameter("shippingCountryName")))
			.body("shippingStateCode", equalTo(rt.getOutputParameter("shippingStateCode")))
			.body("shippingStateName", equalTo(rt.getOutputParameter("shippingStateName")))
			.body("shippingCityCode", equalTo(rt.getOutputParameter("shippingCityCode")))
			.body("shippingCityName", equalTo(rt.getOutputParameter("shippingCityName")))
			.body("shippingDistrictCode", equalTo(rt.getOutputParameter("shippingDistrictCode")))
			.body("shippingDistrictName", equalTo(rt.getOutputParameter("shippingDistrictName")))
			.body("shippingPostcode", equalTo(rt.getOutputParameter("shippingPostcode")));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.orderPriceInfo")
			.body("shippingOriginalPrice", equalTo(rt.getOutputParameter("shippingOriginalPrice")))
			.body("shippingPrice", equalTo(rt.getOutputParameter("shippingPrice")))
			.body("taxPrice", equalTo(rt.getOutputParameter("taxPrice")))
			.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
			.body("itemsPrice", equalTo(rt.getOutputParameter("itemsPrice")))
			.body("adjustmentPrice", equalTo(rt.getOutputParameter("adjustmentPrice")));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("results.paymentInfo[0]", Matchers.hasSize(0));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.orderLineList")
			.body("orderLineId[0]", notNullValue())
			.body("orderId[0]", equalTo(orderId))
			.body("itemId[0]", equalTo(itemId1))
			.body("quantity[0]", equalTo(Integer.parseInt(rt.getOutputParameter("quantity"))))
			.body("itemDisplayText[0]", equalTo(rt.getOutputParameter("itemDisplayText")))
			.body("itemImageLink[0]", equalTo(rt.getOutputParameter("itemImageLink")))
			.body("unitPrice[0]", equalTo(rt.getOutputParameter("unitPrice")))
			.body("tax[0]", equalTo(rt.getOutputParameter("tax")))
			.body("adjustment[0]", equalTo(rt.getOutputParameter("adjustment")));
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results.orderHistory")
			.body("createTimestamp[0]", notNullValue())
			.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId")))
			.body("description[0]", equalTo(rt.getOutputParameter("description")));
			break;
		}
	 }	
	}
	
	@Test
	public void test_004_requiredField_cartlineIdTest() throws Exception {

		rt.setDataLocation("requiredFieldCartlineIdTest", "orderBody");		
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".cartlineIds", hasItem(CBTErrors.REQUIRED_FIELD));	
	}
	
	@Test
	public void test_005_requiredField_addressIdTest() throws Exception {

		rt.setDataLocation("requiredFieldAddressIdTest", "orderBody");		
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".addressId", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_006_requiredField_orderIdTest() throws Exception {

		rt.setDataLocation("requiredFieldOrderIdTest", "orderBody");	
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body("{}")
				.when()
				.post(CBTURI.ORDER_CONFIRM_RECEIVE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_ORDER_INVALID , equalTo(CBTErrors.ORDER_ORDERID_INVALID));
	}
	
	@Test
	public void test_007_null_cartlineIdTest() throws Exception {

		rt.setDataLocation("nullCartlineIdTest", "orderBody");		
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".cartlineIds", hasItem(CBTErrors.REQUIRED_FIELD));	
	}
	
	@Test
	public void test_008_null_addressIdTest() throws Exception {

		rt.setDataLocation("nullAddressIdTest", "orderBody");		
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".addressId", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_009_null_orderIdTest() throws Exception {

		rt.setDataLocation("nullOrderIdTest", "orderBody");		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body("{\"orderId\" : \"\"}")
				.when()
				.post(CBTURI.ORDER_CONFIRM_RECEIVE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_ORDER_INVALID, equalTo(CBTErrors.ORDER_ORDERID_INVALID));
	}
	

	@Test
	public void test_010_invalidCartlineIdTest() throws Exception {

		rt.setDataLocation("invalidCartlineIdTest", "orderBody");		
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", "1");				
		orderBody = orderBody.replaceAll("@addressId", addressId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_ORDER_CART_INVALID, equalTo(CBTErrors.ORDER_CART_INVALID))
		.body("results", hasSize(0));	
	}
	
	@Test
	public void test_011_invalidAddressIdTest() throws Exception {

		rt.setDataLocation("invalidAddressIdTest", "orderBody");		
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", "1");
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_ADDRESS_NOT_EXIST, equalTo(CBTErrors.ADDRESS_NOT_EXIST));	
	}
	
	@Test
	public void test_012_invalidOrderIdTest() throws Exception {

		rt.setDataLocation("invalidOrderIdTest", "orderBody");		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body("{\"orderId\" : \"1\"}")
				.when()
				.post(CBTURI.ORDER_CONFIRM_RECEIVE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_INTERNAL_EXCEPTION, equalTo(CBTErrors.INTERNAL_EXCEPTION));	
	}
	
	@Test
	public void test_013_authorizationTest() throws Exception {
		
		rt.setDataLocation("sumbitOrderTest", "orderBody");
			
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(orderBody).when()
				.post(CBTURI.ORDER);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors" , hasItem(CBTErrors.NEED_LOGIN));
	}
	
	
	@Test
	public void test_014_oauth_null_orderTest() throws Exception {
		
		rt.setDataLocation("sumbitOrderTest", "orderBody");
				
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_AUTHORIZATION, containsString(CBTErrors.TOKEN_AUTHORIZATION));
	}
	
	@Test
	public void test_015_oauth_invalid_orderTest() throws Exception {
		
		rt.setDataLocation("invalidOauthTest", "orderBody");
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
			
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_TOKEN_NOT_MATCH , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	@Test
	public void test_016_multiTenant_Test() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "orderBody");
		//get tenantA oauth
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		String userToken = login("autoTest4", "Test123_", accessToken);
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order - Tenant User A submit Tenant-B order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_ADDRESS_NOT_EXIST, equalTo(CBTErrors.ADDRESS_NOT_EXIST));
	}
	
	@Test
	public void test_017_multiTenant_addressIdTest() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "orderBody");
		//get tenantA oauth
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		String userToken = login("autoTest4", "Test123_", accessToken);
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//get current address list size
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response.jsonPath().get("results.addressId");
		//clear address
		if(addressList.size()!= 0)
		{
			for(int i=0; i<addressList.size();i++)
			{
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", accessToken)
						.pathParam("token", userToken)
						.pathParam("addressId", addressList.get(i))
						.when()
						.delete(CBTURI.ADDRESS + "/{addressId}" + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true));	
			}			
		}
				
		//add address
		String addressPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressPath);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.addressId", notNullValue());
		
		String addressId = response.jsonPath().get("results.addressId");
				
		//submit order - Tenant User A submit Tenant-B order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_ORDER_CART_INVALID, equalTo(CBTErrors.ORDER_CART_INVALID))
		.body("results", hasSize(0));
		
	}
	
	@Test
	public void test_018_cancelOrder_createdTest() throws Exception {
		
		rt.setDataLocation("cancelCreatedOrderTest", "orderBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		while(true)
		{
			//get order
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//cancel order
				response = CBTUtility.cancelOrder(access_token,token,orderId);

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderId", equalTo(orderId))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("errors", nullValue());
				break;
			}
		}
		//get order by id after cancel		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);			
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.orderHistory")
				.body("createTimestamp[0]", notNullValue())
				.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId")))
				.body("description[0]", equalTo(rt.getOutputParameter("description1")))
				.body("createTimestamp[1]", notNullValue())
				.body("creatorId[1]", equalTo(rt.getOutputParameter("creatorId")))
				.body("description[1]", equalTo(rt.getOutputParameter("description2")));
				break;
			}			
		}		
	}
	
	@Test
	public void test_019_cancelOrder_paidTest() throws Exception {
		
		rt.setDataLocation("cancelPaidOrderTest", "orderBody");
		
		//add to cart		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
    	cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//payment
				String paymentPath = rt.getInputParameter("paymentBody");
				String paymentBody = rt.getMsgBodyfromJson(paymentPath);
				paymentBody = paymentBody.replaceAll("@orderId", orderId);
				
				response = CBTUtility.payOrder(access_token,token,paymentBody);
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")));
				break;
			}
		}
			
		//get order by id after payment		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results")
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus1")));
				
                response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				//cancel order
				response = CBTUtility.cancelOrder(access_token,token,orderId);

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderId", equalTo(orderId))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus2")))
				.body("errors", nullValue());
				break;
			}
		}
		
		//get order by id after cancel		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus2")));

                response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.orderHistory")
				.body("createTimestamp[0]", notNullValue())
				.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId")))
				.body("description[0]", equalTo(rt.getOutputParameter("description1")))
				.body("createTimestamp[1]", notNullValue())
				.body("creatorId[1]", equalTo(rt.getOutputParameter("creatorId")))
				.body("description[1]", equalTo(rt.getOutputParameter("description2")))
				.body("createTimestamp[2]", notNullValue())
				.body("creatorId[2]", equalTo(rt.getOutputParameter("creatorId")))
				.body("description[2]", equalTo(rt.getOutputParameter("description3")));
				break;
			}			
		}	
	}
	
	@Test
	public void test_020_cancelOrder_scheduledTest() throws Exception {
		
		rt.setDataLocation("cancelScheduledOrderTest", "orderBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//payment
				String paymentPath = rt.getInputParameter("paymentBody");
				String paymentBody = rt.getMsgBodyfromJson(paymentPath);
				paymentBody = paymentBody.replaceAll("@orderId", orderId);
				
				response = CBTUtility.payOrder(access_token,token,paymentBody);
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")));	
				break;
			 }
		}
		
		//schedule
		CBTUtility.scheduleOrder(orderId);
		
		//get order by id after scheduled		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderId", equalTo(orderId))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus1")));
				
                response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				//cancel order
				response = CBTUtility.cancelOrder(access_token,token,orderId);

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderId", equalTo(orderId))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus2")))
				.body("errors", nullValue());
				break;
			}
		}

		//get order by id after cancel		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus2")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.orderHistory")
				.body("createTimestamp[0]", notNullValue())
				.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[0]", equalTo(rt.getOutputParameter("description1")))
				.body("createTimestamp[1]", notNullValue())
				.body("creatorId[1]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[1]", equalTo(rt.getOutputParameter("description2")))
				.body("createTimestamp[2]", notNullValue())
				.body("creatorId[2]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[2]", equalTo(rt.getOutputParameter("description3")))
				.body("createTimestamp[3]", notNullValue())
				.body("creatorId[3]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[3]", equalTo(rt.getOutputParameter("description4")));
				break;
			}			
		}	
	}
	
	@Test
	public void test_021_cancelOrder_releasedTest() throws Exception {
		
		rt.setDataLocation("cancelReleasedOrderTest", "orderBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//payment
				String paymentPath = rt.getInputParameter("paymentBody");
				String paymentBody = rt.getMsgBodyfromJson(paymentPath);
				paymentBody = paymentBody.replaceAll("@orderId", orderId);
				
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.body(paymentBody).when()
						.post(CBTURI.PAYMENT + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")));	
				break;
			 }
		}
		
		//schedule
		CBTUtility.scheduleOrder(orderId);
		
		//release
		CBTUtility.releaseOrder(orderId);

		//get order by id after released		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderId", equalTo(orderId))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
				
                response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				break;
			}
		}
		
		//cancel order
		Response response = CBTUtility.cancelOrder(access_token,token,orderId);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_ORDER_CANNOT_CANCEL , equalTo(CBTErrors.ORDER_CANNOT_CANCEL));	
		
		//get order by id after cancel		
		while(true)
		{
			response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.orderHistory")
				.body("createTimestamp[0]", notNullValue())
				.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[0]", equalTo(rt.getOutputParameter("description1")))
				.body("createTimestamp[1]", notNullValue())
				.body("creatorId[1]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[1]", equalTo(rt.getOutputParameter("description2")))
				.body("createTimestamp[2]", notNullValue())
				.body("creatorId[2]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[2]", equalTo(rt.getOutputParameter("description3")))
				.body("createTimestamp[3]", notNullValue())
				.body("creatorId[3]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[3]", equalTo(rt.getOutputParameter("description4")));
				break;
			}			
		}
		
	}
	
	@Test
	public void test_022_cancelOrder_shippedTest() throws Exception {
		
		rt.setDataLocation("cancelShippedOrderTest", "orderBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//payment
				String paymentPath = rt.getInputParameter("paymentBody");
				String paymentBody = rt.getMsgBodyfromJson(paymentPath);
				paymentBody = paymentBody.replaceAll("@orderId", orderId);
				
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.body(paymentBody).when()
						.post(CBTURI.PAYMENT + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")));	
				break;
			 }
		}
		
		//schedule
		CBTUtility.scheduleOrder(orderId);
		
		//release
		CBTUtility.releaseOrder(orderId);
		
		//ship
		CBTUtility.shipOrder(orderId);

		//get order by id after released		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderId", equalTo(orderId))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
				
                response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				break;
			}
		}
		
		//cancel order
		Response response = CBTUtility.cancelOrder(access_token,token,orderId);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_ORDER_CANNOT_CANCEL , equalTo(CBTErrors.ORDER_CANNOT_CANCEL));	
		
		//get order by id after cancel		
		while(true)
		{
			response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.orderHistory")
				.body("createTimestamp[0]", notNullValue())
				.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[0]", equalTo(rt.getOutputParameter("description1")))
				.body("createTimestamp[1]", notNullValue())
				.body("creatorId[1]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[1]", equalTo(rt.getOutputParameter("description2")))
				.body("createTimestamp[2]", notNullValue())
				.body("creatorId[2]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[2]", equalTo(rt.getOutputParameter("description3")))
				.body("createTimestamp[3]", notNullValue())
				.body("creatorId[3]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[3]", equalTo(rt.getOutputParameter("description4")))
				.body("createTimestamp[4]", notNullValue())
				.body("creatorId[4]", equalTo(rt.getOutputParameter("creatorId3")))
				.body("description[4]", equalTo(rt.getOutputParameter("description5")));
				break;
			}			
		}	
	}
	
	@Test
	public void test_023_cancelOrder_finishedTest() throws Exception {
		
		rt.setDataLocation("cancelReceivedOrderTest", "orderBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		cartlineId = CBTUtility.addToCart(access_token,token,cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token,token,orderBody);

		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//payment
				String paymentPath = rt.getInputParameter("paymentBody");
				String paymentBody = rt.getMsgBodyfromJson(paymentPath);
				paymentBody = paymentBody.replaceAll("@orderId", orderId);
				
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.body(paymentBody).when()
						.post(CBTURI.PAYMENT + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")));	
				break;
			 }
		}
		
		//schedule
		CBTUtility.scheduleOrder(orderId);
		
		//release
		CBTUtility.releaseOrder(orderId);
		
		//ship
		CBTUtility.shipOrder(orderId);

		//confirm receive
		String receiveOrderPath = rt.getInputParameter("receiveOrderBody");
		String receiveOrderBody = rt.getMsgBodyfromJson(receiveOrderPath);		
		receiveOrderBody = receiveOrderBody.replace("@orderId", orderId);
		CBTUtility.confirmReceiveOrder(access_token,token,receiveOrderBody);
		
		//cancel order
		Response response = CBTUtility.cancelOrder(access_token,token,orderId);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_ORDER_CANNOT_CANCEL , equalTo(CBTErrors.ORDER_CANNOT_CANCEL));	
		
		//get order by id after cancel		
		while(true)
		{
			response = CBTUtility.getOrder(access_token,token,orderId);
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
                .rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("paymentNumber")))
				.body("paymentTimestamp", notNullValue());
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.orderHistory")
				.body("createTimestamp[0]", notNullValue())
				.body("creatorId[0]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[0]", equalTo(rt.getOutputParameter("description1")))
				.body("createTimestamp[1]", notNullValue())
				.body("creatorId[1]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[1]", equalTo(rt.getOutputParameter("description2")))
				.body("createTimestamp[2]", notNullValue())
				.body("creatorId[2]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[2]", equalTo(rt.getOutputParameter("description3")))
				.body("createTimestamp[3]", notNullValue())
				.body("creatorId[3]", equalTo(rt.getOutputParameter("creatorId2")))
				.body("description[3]", equalTo(rt.getOutputParameter("description4")))
				.body("createTimestamp[4]", notNullValue())
				.body("creatorId[4]", equalTo(rt.getOutputParameter("creatorId3")))
				.body("description[4]", equalTo(rt.getOutputParameter("description5")))
				.body("createTimestamp[5]", notNullValue())
				.body("creatorId[5]", equalTo(rt.getOutputParameter("creatorId1")))
				.body("description[5]", equalTo(rt.getOutputParameter("description6")));
				break;
			}			
		}	
	}
	
	
	@AfterClass
	public static void cleanData() throws Exception {
		
		Response response = given().contentType("application/json").header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART + "?=token={token}");
		
		//clear cartline
		ArrayList<String> l = response.jsonPath().get("results");	
		if(l!=null && l.size()!=0)
		{
			ArrayList<String> cartLineIdList = response.jsonPath().get("results.cartlineId");	
			
			for(int i=0; i<cartLineIdList.size();i++ )
			{
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.when()
						.delete(CBTURI.CART + "/" + cartLineIdList.get(i) + "?=token={token}");
			}
			
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.body("status", equalTo(true));
		}	
		
		//TenantA
		//get current address list size
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ADDRESS + "?=token={token}");
		
		ArrayList<String> addressList = response.jsonPath().get("results.addressId");
		//clear address
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
}
