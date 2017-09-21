package com.ibm.cbt.rest.api.test.payment;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.*;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaymentTest extends CBTUtility{
	
	final static String testDataFileName = "resources/payment/payment/paymentTest.xml";
	final static String preLoadDataFileName = "resources/payment/payment/_preLoadData.config";
	static RestTest rt = new RestTest();
	private static String token;
	private static String access_token;
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
		
		Response response = given().contentType("application/json")
				.header("Authorization", access_token)
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
		
		//get address id
		rt.setDataLocation("setup", "paymentBody");
		String addressPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressPath);
		addressId = CBTUtility.addAddress(access_token, token, addressBody);
	}
	
	@Test(timeout=10000)
	public void test_000_paymentTest() throws Exception {
		
		rt.setDataLocation("paymentTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);

		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("status");
			if(status)
			{
				//payment
				String paymentPath = rt.getInputParameter("paymentBody");
				String paymentBody = rt.getMsgBodyfromJson(paymentPath);
				paymentBody = paymentBody.replaceAll("@orderId", orderId);
				
				response = CBTUtility.payOrder(access_token, token, paymentBody);		
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));		
				break;
			 }
		}
		
	    //get order by id after payment		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("results.orderStatus").equals(rt.getOutputParameter("orderStatus"));
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("buyerId", notNullValue())
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")));

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("trade_no")))
				.body("paymentTimestamp", notNullValue());
				
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
	public void test_001_getPayment_totalTest() throws Exception {
		
		rt.setDataLocation("getPaymentTest", "paymentBody");
		
		//payment
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("orderId", orderId)
				.when()
				.get(CBTURI.PAYMENT + "/{orderId}?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue())
		.body("refunds", hasSize(0));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", notNullValue())
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
	}
	
	@Test(timeout=10000) 
	public void test_002_payment_partialTest() throws Exception {
		
		rt.setDataLocation("paymentPartialTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		
		//payment - partial
		String paymentPath = rt.getInputParameter("paymentBody1");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("status");
			if(status)
			{		
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.body(paymentBody).when()
						.post(CBTURI.PAYMENT + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("order_id", equalTo(orderId))
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status1")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount1")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount1")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no1")));
				break;
			}
		}
		
		//get order by id after partial payment
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("results.orderStatus").equals(rt.getOutputParameter("orderStatus1"));
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.body("results.orderStatus", equalTo(rt.getOutputParameter("orderStatus1")))
				.body("results.paymentInfo[0]", Matchers.hasSize(0));
				break;
			}
		}
		
		//payment - partial2
		paymentPath = rt.getInputParameter("paymentBody2");
		paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status2")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount2")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue())
		.body("refunds", hasSize(0));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[1]")
		.body("payment_id", notNullValue())
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("amount2")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no2")));
		
		//get order by id after total payment
		while(true)
		{
			response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("results.orderStatus").equals(rt.getOutputParameter("orderStatus2"));
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("buyerId", notNullValue())
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus2")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")));

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("trade_no2")))
				.body("paymentTimestamp", notNullValue());
				
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
	public void test_003_getPayment_partialTest() throws Exception {
		
		rt.setDataLocation("getPaymentPartialTest", "paymentBody");
		
		//payment
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("orderId", orderId)
				.when()
				.get(CBTURI.PAYMENT + "/{orderId}?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue())
		.body("refunds", hasSize(0));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments")
		.body("payment_id[0]", notNullValue())
		.body("payment_id[1]", notNullValue())
		.body("pay_method[0]", equalTo(rt.getOutputParameter("pay_method")))
		.body("pay_method[1]", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", hasItems(rt.getOutputParameter("amount1"),rt.getOutputParameter("amount2")))
		.body("currency[0]", equalTo(rt.getOutputParameter("currency")))
		.body("currency[1]", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account[0]", equalTo(rt.getOutputParameter("pay_account")))
		.body("pay_account[1]", equalTo(rt.getOutputParameter("pay_account")))
		.body("status[0]", equalTo(rt.getOutputParameter("status")))		
		.body("status[1]", equalTo(rt.getOutputParameter("status")))
		.body("trade_no", hasItems(rt.getOutputParameter("trade_no1"),rt.getOutputParameter("trade_no2")));
	}
	
	@Test(timeout=10000) 
	public void test_004_payment_exceedTest() throws Exception {
		
		rt.setDataLocation("paymentExceedTest", "paymentBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);	
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		
		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
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
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
				break;
			}
		}
		
		//get order by id after payment		
		while(true)
		{
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("results.orderStatus").equals(rt.getOutputParameter("orderStatus"));
			if(status)
			{
				//get order by id after payment
				response = given().log().ifValidationFails().contentType("application/json")
						.header("Authorization", access_token)
						.pathParam("token", token)
						.pathParam("orderId", orderId)
						.when()
						.get(CBTURI.ORDER + "/{orderId}" + "?=token={token}");
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("buyerId", notNullValue())
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")));

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("trade_no")))
				.body("paymentTimestamp", notNullValue());
				
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
	public void test_005_requiredField_orderIdTest() throws Exception {
		
		rt.setDataLocation("requiredFieldOrderIdTest", "paymentBody");
		
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".order_id", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_006_requiredField_payMethodTest() throws Exception {
		
		rt.setDataLocation("requiredFieldPayMethodTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".pay_method", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_007_requiredField_amountTest() throws Exception {
		
		rt.setDataLocation("requiredFieldAmountTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);		
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".amount", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_008_requiredField_statusTest() throws Exception {
		
		rt.setDataLocation("requiredFieldStatusTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".status", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_009_requiredField_tradeNoTest() throws Exception {
		
		rt.setDataLocation("requiredFieldTradeNoTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".trade_no", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	
	@Test(timeout=10000)
	public void test_010_optionField_payAccountTest() throws Exception {
		
		rt.setDataLocation("optionFieldPayAccountTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
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
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", nullValue())
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
				break;
			 }
		}
		
		//get order by id after payment
		while(true){
			Response response = CBTUtility.getOrder(access_token, token, orderId);		
			Boolean status = response.jsonPath().get("results.orderStatus").equals(rt.getOutputParameter("orderStatus"));
			response.then().log().all();
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("buyerId", notNullValue())
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")));

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("trade_no")))
				.body("paymentTimestamp", notNullValue());
				
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
	public void test_011_invalid_orderIdTest() throws Exception {
		
		rt.setDataLocation("invalidOrderIdTest", "paymentBody");
		
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_ORDERID, equalTo(CBTErrors.PAYMENT_INVALID_ORDERID));
	}
	
	@Test
	public void test_012_invalid_payMethodTest() throws Exception {
		rt.setDataLocation("invalidPayMethodTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_PAYMETHOD, equalTo(CBTErrors.PAYMENT_INVALID_PAYMETHOD));
	}
	
	@Test
	public void test_013_invalid_amount_zeroTest() throws Exception {
		
		rt.setDataLocation("invalidAmountZeroTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);		
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_AMOUNT, equalTo(CBTErrors.PAYMENT_INVALID_AMOUNT));
	}
	
	@Test
	public void test_014_invalid_amount_negativeTest() throws Exception {
		
		rt.setDataLocation("invalidAmountNegativeTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_AMOUNT, equalTo(CBTErrors.PAYMENT_INVALID_AMOUNT));
	}
	
	@Test
	public void test_015_invalid_amount_wrongTypeTest() throws Exception {
		
		rt.setDataLocation("invalidAmountWrongTypeTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_WRONGTYPE_AMOUNT, equalTo(CBTErrors.PAYMENT_WRONGTYPE_AMOUNT));
	}
	
	@Test
	public void test_016_invalid_statusTest() throws Exception {
		
		rt.setDataLocation("invalidStatusTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_STATUS, equalTo(CBTErrors.PAYMENT_INVALID_STATUS));
	}
	
	@Test
	public void test_017_invalid_tradeNoTest() throws Exception {
		
		rt.setDataLocation("invalidTradeNoTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//pay with duplicated tradeNo
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_DUPLICATED_TRADENO, equalTo(CBTErrors.PAYMENT_DUPLICATED_TRADENO));
	}
	
	@Test(timeout=10000)
	public void test_018_status_failedTest() throws Exception {
		
		rt.setDataLocation("statusFailedTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);	
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
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
				break;
			}
		}
		
		//get order by id after payment
		Response response = CBTUtility.getOrder(access_token, token, orderId);	
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
		.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
		.body("paymentInfo[0]", Matchers.hasSize(0));
	}
	
	@Test(timeout=10000)
	public void test_019_status_canceledTest() throws Exception {
		
		rt.setDataLocation("statusCanceledTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);	
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
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
				break;
			}
		}
	
		//get order by id after payment
		Response response = CBTUtility.getOrder(access_token, token, orderId);	
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
		.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
		.body("paymentInfo[0]", Matchers.hasSize(0));
	}
	
	@Test(timeout=10000)
	public void test_020_status_preparedTest() throws Exception {
		
		rt.setDataLocation("statusPreparedTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);	
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		String orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//get order	after submit order
		while(true){	
			Response response = CBTUtility.getOrder(access_token, token, orderId);	
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
				.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
				.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
				.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
				.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
				.body("create_timestamp", notNullValue())
				.body("update_timestamp", notNullValue())
				.body("refunds", hasSize(0));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.payments[0]")
				.body("payment_id", notNullValue())
				.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
				.body("amount", equalTo(rt.getOutputParameter("amount")))
				.body("currency", equalTo(rt.getOutputParameter("currency")))
				.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
				.body("status", equalTo(rt.getOutputParameter("status")))
				.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
				break;
			}
		}
		
		//get order by id after payment
		Response response = CBTUtility.getOrder(access_token, token, orderId);	
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
		.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
		.body("paymentInfo[0]", Matchers.hasSize(0));
	}
	
	@Test
	public void test_021_getPayment_invalid_orderIdTest() throws Exception {
		
		rt.setDataLocation("getPaymentTest", "paymentBody");
		
		//payment
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.PAYMENT + "/1?token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_ORDERID, equalTo(CBTErrors.PAYMENT_INVALID_ORDERID));
	}
	
	
	@Test
	public void test_022_authorizationTest() throws Exception {
		
		rt.setDataLocation("paymentTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors", hasItem(CBTErrors.NEED_LOGIN));
	}
	
	
	@Test
	public void test_023_oauth_null_paymentTest() throws Exception {

        rt.setDataLocation("paymentTest", "paymentBody");
        //add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);

		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
				
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_AUTHORIZATION, containsString(CBTErrors.TOKEN_AUTHORIZATION));
	}
	
	@Test
	public void test_024_oauth_invalid_paymentTest() throws Exception {
		
		rt.setDataLocation("invalidOauthTest", "paymentBody");
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
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_TOKEN_NOT_MATCH , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	@Test
	public void test_025_multiTenant_Test() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "paymentBody");
		
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
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_PAYMENT_MT , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	@Test
	public void test_026_payment_withoutSleepTest() throws Exception {
		
		rt.setDataLocation("paymentTest", "paymentBody");
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		String cartlineId = CBTUtility.addToCart(access_token, token, cartBody);
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		orderId = CBTUtility.submitOrder(access_token, token, orderBody);
		
		//payment
		String paymentPath = rt.getInputParameter("paymentBody");
		String paymentBody = rt.getMsgBodyfromJson(paymentPath);
		paymentBody = paymentBody.replaceAll("@orderId", orderId);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue())
		.body("refunds", hasSize(0));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", notNullValue())
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		while(true){	
			response = CBTUtility.getOrder(access_token, token, orderId);	
			Boolean status = response.jsonPath().get("results.orderStatus").equals(rt.getOutputParameter("orderStatus"));
			if(status)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true))
				.rootPath("results")
				.body("orderId", equalTo(orderId))
				.body("buyerId", notNullValue())
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")));

				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.paymentInfo[0]")
				.body("payAmount", equalTo(rt.getOutputParameter("payAmount")))
				.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
				.body("paymentNumber", equalTo(rt.getOutputParameter("trade_no")))
				.body("paymentTimestamp", notNullValue());
				
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
	
	@AfterClass
	public static void cleanData() throws Exception {
		
		Response response = given().contentType("application/json")
				.header("Authorization", access_token)
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
