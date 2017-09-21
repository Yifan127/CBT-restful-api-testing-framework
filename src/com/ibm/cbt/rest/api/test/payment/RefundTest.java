package com.ibm.cbt.rest.api.test.payment;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.AfterClass;
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
public class RefundTest extends CBTUtility{
	
	final static String testDataFileName = "resources/payment/refund/refundTest.xml";
	final static String preLoadDataFileName = "resources/payment/refund/_preLoadData.config";
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
				
		//get address id
		rt.setDataLocation("setup", "refundBody");
		String addressPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.addressId", notNullValue());
		
		addressId = response.jsonPath().get("results.addressId");
	}
	
	@Test
	public void test_000_refund_totalTest() throws Exception {
		
		rt.setDataLocation("refundTotalTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));
		
		//TODO - verify order status after refunded
	}
	
	@Test
	public void test_001_refund_partialTest() throws Exception {
		
		rt.setDataLocation("refundPartialTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody1");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		refundBackendPath = rt.getInputParameter("refundBackendBody2");
		refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());
		
		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		ArrayList<String> refundId = response.jsonPath().get("refunds.refundId");
		
		//refund - partial1
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody1");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId.get(0));
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status1")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount1")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId.get(0)))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount1")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));
		
		//refund - partial2
		refundPath = rt.getInputParameter("refundBody2");
		refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId.get(1));
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status2")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount2")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[1]")
		.body("refund_id", equalTo(refundId.get(1)))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount2")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));
				
		//TODO - verify order status after refunded
	}
	
	@Test
	public void test_002_refund_exceedTest() throws Exception {
		
		rt.setDataLocation("refundExceedTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));
		
		//TODO - verify order status after refunded
	}
	
	@Test
	public void test_003_requiredField_refundIdTest() throws Exception {
		
		rt.setDataLocation("requiredFieldRefundIdTest", "refundBody");
		
		//refund
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".refund_id", hasItem(CBTErrors.REQUIRED_FIELD));

	}
	
	@Test
	public void test_004_requiredField_refundNoTest() throws Exception {
		
		rt.setDataLocation("requiredFieldRefundNoTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".refund_no", hasItem(CBTErrors.REQUIRED_FIELD));
		
	}
	
	@Test
	public void test_005_requiredField_amountTest() throws Exception {
		
		rt.setDataLocation("requiredFieldAmountTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".amount", hasItem(CBTErrors.REQUIRED_FIELD));

	}
	
	@Test
	public void test_006_requiredField_statusTest() throws Exception {
		
		rt.setDataLocation("requiredFieldStatusTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".status", hasItem(CBTErrors.REQUIRED_FIELD));
		
	}
	
	@Test
	public void test_007_invalid_refundIdTest() throws Exception {
		
		rt.setDataLocation("invalidRefundIdTest", "refundBody");
		
		//refund
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_ORDERID, equalTo(CBTErrors.PAYMENT_INVALID_ORDERID));

	}
	
	@Test
	public void test_008_invalid_statusTest() throws Exception {
		
		rt.setDataLocation("invalidStatusTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_INVALID_STATUS, equalTo(CBTErrors.PAYMENT_INVALID_STATUS));
			
	}
	
	@Test
	public void test_009_invalid_amount_zeroTest() throws Exception {
		
		rt.setDataLocation("invalidAmountZeroTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));
			
	}
	
	@Test
	public void test_010_invalid_amount_negativeTest() throws Exception {
		
		rt.setDataLocation("invalidAmountNegativeTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_REFUND_INVALID_AMOUNT, equalTo(CBTErrors.REFUND_INVALID_AMOUNT));
			
	}
	
	@Test
	public void test_011_invalid_amount_wrongTypeTest() throws Exception {
		
		rt.setDataLocation("invalidAmountWrongTypeTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PAYMENT_WRONGTYPE_AMOUNT, equalTo(CBTErrors.PAYMENT_WRONGTYPE_AMOUNT));
			
	}
	
	@Test
	public void test_012_status_failedTest() throws Exception {
		
		rt.setDataLocation("statusFailedTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));;
			
	}
	
	@Test
	public void test_013_status_canceledTest() throws Exception {
		
		rt.setDataLocation("statusCanceledTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));;
			
	}
	
	@Test
	public void test_014_status_preparedTest() throws Exception {
		
		rt.setDataLocation("statusPreparedTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		String paymentId = response.jsonPath().get("results.payments[0].payment_id");
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
				
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.rootPath("results")
		.body("order_id", equalTo(orderId))
		.body("total_amount", equalTo(rt.getOutputParameter("total_amount")))
		.body("pay_status", equalTo(rt.getOutputParameter("pay_status")))
		.body("paid_amount", equalTo(rt.getOutputParameter("paid_amount")))
		.body("refunded_amount", equalTo(rt.getOutputParameter("refunded_amount")))
		.body("create_timestamp", notNullValue())
		.body("update_timestamp", notNullValue());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.payments[0]")
		.body("payment_id", equalTo(paymentId))
		.body("pay_method", equalTo(rt.getOutputParameter("pay_method")))
		.body("amount", equalTo(rt.getOutputParameter("pay_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("pay_account", equalTo(rt.getOutputParameter("pay_account")))
		.body("status", equalTo(rt.getOutputParameter("payment_status")))
		.body("trade_no", equalTo(rt.getOutputParameter("trade_no")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.refunds[0]")
		.body("refund_id", equalTo(refundId))
		.body("payment_id", equalTo(paymentId))
		.body("refund_method", equalTo(rt.getOutputParameter("refund_method")))
		.body("amount", equalTo(rt.getOutputParameter("refund_amount")))
		.body("currency", equalTo(rt.getOutputParameter("currency")))
		.body("refund_account", equalTo(rt.getOutputParameter("refund_account")))
		.body("status", equalTo(rt.getOutputParameter("refund_status")))
		.body("refund_no", equalTo(rt.getOutputParameter("refund_no")));;
			
	}
	
	@Test
	public void test_015_authorizationTest() throws Exception {
		
		rt.setDataLocation("refundTotalTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(refundBody).when()
				.post(CBTURI.REFUND);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors", hasItem(CBTErrors.NEED_LOGIN));
	}
	
	@Test
	public void test_016_oauth_null_refundTest() throws Exception {
		
		rt.setDataLocation("refundTotalTest", "refundBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
		response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_AUTHORIZATION, containsString(CBTErrors.TOKEN_AUTHORIZATION));
	}
	
	@Test
	public void test_017_oauth_invalid_refundTest() throws Exception {
		
		rt.setDataLocation("invalidOauthTest", "refundBody");
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", token)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_TOKEN_NOT_MATCH , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	@Test
	public void test_018_multiTenant_Test() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "refundBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		String userToken = login("autoTest4", "Test123_", accessToken);
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		//add to cart
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", notNullValue());
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//submit order
		String orderPath = rt.getInputParameter("orderBody");
		String orderBody = rt.getMsgBodyfromJson(orderPath);
		orderBody = orderBody.replaceAll("@cartlineId", cartlineId);
		orderBody = orderBody.replaceAll("@addressId", addressId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		orderId = response.jsonPath().get("results.orderId");
		
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
		.body("status", equalTo(true));
		//get refundId from backend API
		changePortToBackEnd();
		String refundBackendPath = rt.getInputParameter("refundBackendBody");
		String refundBackendBody = rt.getMsgBodyfromJson(refundBackendPath);
		Timestamp now = new Timestamp(System.currentTimeMillis()); 
		refundBackendBody = refundBackendBody.replaceAll("@orderId", orderId);
		refundBackendBody = refundBackendBody.replaceAll("@createTimestamp", now.toString());

		response = given().log().ifValidationFails().contentType("application/json")
				.body(refundBackendBody).when()
				.post(CBTURI.REFUND_BACKEND);
		
		response.then().log().ifValidationFails().assertThat().statusCode(200);		
		String refundId = response.jsonPath().get("refunds.refundId[0]");
		
		//refund
		changePortToBAL();
		String refundPath = rt.getInputParameter("refundBody");
		String refundBody = rt.getMsgBodyfromJson(refundPath);
		refundBody = refundBody.replaceAll("@refundId", refundId);
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.body(refundBody).when()
				.post(CBTURI.REFUND + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_PAYMENT_MT , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	@AfterClass
	public static void cleanData() throws Exception {
		
//		String token = login(access_token);
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
