package com.ibm.cbt.rest.api.test.order;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.runners.*;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.ibm.cbt.rest.api.test.utilities.*;
import com.ibm.cbt.rest.test.restassured.RestTest;


/**
 * @author yifzhang
 *
 */

@SuppressWarnings("deprecation")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrderListTest extends CBTUtility{
	
	final static String preLoadDataFileName = "resources/order/_preLoadData.config";
	final static String testDataFileName = "resources/order/orderTest.xml";
	private static String token;
	private static String access_token;
	static RestTest rt = new RestTest();
	
	private static String cartlineId = "";
	private static String addressId = "";
	private static String orderId = "";
	private static Integer totalResult;
	private static String itemId1 = "";
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);	
		rt.setDataFile(testDataFileName);	
		access_token = CBTUtility.oauth();	
		token = login("cbt1", "Test123_", access_token);
		//get item id
		itemId1 = CBTUtility.getItemId1(access_token);
				
		Response response = given().log().ifValidationFails().contentType("application/json")
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
		
		//get address
		rt.setDataLocation("setup", "orderBody");
		String addressPath = rt.getInputParameter("addressBody");
		String addressBody = rt.getMsgBodyfromJson(addressPath);
		addressId = CBTUtility.addAddress(access_token,token,addressBody);
				
		//get current order list
		changePortToBackEnd();
		response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("orgId", rt.getPreLoadParameter("$orgId"))
				.pathParam("buyerId", rt.getPreLoadParameter("$userId"))
				.when()
				.get(CBTURI.ENTITY + "/{orgId}/order/EC?where=buyerId=\"{buyerId}\"") ;
		
		totalResult = response.jsonPath().get("pageInfo.totalResult");
		
		//reset port
		rt.initialize(preLoadDataFileName);
	}
	
	@Test(timeout=10000)
	public void test_000_getOrderListTest() throws Exception {

		rt.setDataLocation("getOrderListTest", "orderBody");
		
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

		//get order list
		while(true){
			Thread.sleep(500);
			Response response = given().log().ifValidationFails().contentType("application/json")
					.header("Authorization", access_token)
					.pathParam("token", token)
					.when()
					.get(CBTURI.ORDER + "?=token={token}");
			Boolean status = response.jsonPath().get("results.items[0].orderId").equals(orderId);	
			if(status){
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("status", equalTo(true));
				
				//order
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.items[0]")
				.body("orderId", equalTo(orderId))
				.body("buyerId", equalTo(rt.getPreLoadParameter("$userId")))
				.body("orderComment", equalTo(rt.getOutputParameter("orderComment")))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
				.body("createTimestamp", notNullValue())
				.body("updateTimestamp", notNullValue());
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.items[0].shippingInfo")
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
				.rootPath("results.items[0].orderPriceInfo")
				.body("shippingOriginalPrice", equalTo(rt.getOutputParameter("shippingOriginalPrice")))
				.body("shippingPrice", equalTo(rt.getOutputParameter("shippingPrice")))
				.body("taxPrice", equalTo(rt.getOutputParameter("taxPrice")))
				.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
				.body("itemsPrice", equalTo(rt.getOutputParameter("itemsPrice")))
				.body("adjustmentPrice", equalTo(rt.getOutputParameter("adjustmentPrice")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results.items[0].orderLineList[0]")
				.body("orderLineId", notNullValue())
				.body("orderId", equalTo(orderId))
				.body("itemId", equalTo(itemId1))
				.body("quantity", equalTo(Integer.parseInt(rt.getOutputParameter("quantity"))))
				.body("itemDisplayText", equalTo(rt.getOutputParameter("itemDisplayText")))
				.body("itemImageLink", equalTo(rt.getOutputParameter("itemImageLink")))
				.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice")))
				.body("tax", equalTo(rt.getOutputParameter("tax")))
				.body("adjustment", equalTo(rt.getOutputParameter("adjustment")));
				
				//meta
				Integer totalCount = totalResult + 1;
				Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
				Double pageCount = Math.ceil(totalCount/perPage);
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("results._meta")
				.body("totalCount", equalTo(totalCount))
				.body("pageCount", equalTo(pageCount.intValue()))
				.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
				.body("perPage", equalTo(perPage.intValue()));
				
				//links
				if(totalCount <= perPage){	
					response.then().log().ifValidationFails().statusCode(200).assertThat()
					.rootPath("results._links")
					.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&page=1"));
				}
				else
				{
					response.then().log().ifValidationFails().statusCode(200).assertThat()
					.rootPath("results._links")
					.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&page=1"))
					.body("next.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&page=2"))
					.body("last.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&page=" + pageCount.intValue()));		
				}
				break;	
			}
		}
		
	}
	

	@Test
	public void test_001_getOrderList_byStatusCreatedTest() throws Exception {

		rt.setDataLocation("getOrderListCreatedTest", "orderBody");
		
		//get order list by status - created
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ORDER + "?=token={token}" + "&status=0");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//check order status
		ArrayList<String> list = response.jsonPath().get("results.items.orderStatus");
		for(int i = 0; i<list.size(); i++)
		{
			Assert.assertEquals(rt.getOutputParameter("orderStatus"), list.get(i));			
		}
		
		//meta
		Integer totalCount = response.jsonPath().get("results._meta.totalCount");
		Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
		Double pageCount = Math.ceil(totalCount/perPage);
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", notNullValue())
		.body("pageCount", equalTo(pageCount.intValue()))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(perPage.intValue()));
			
		//links
		if(totalCount <= perPage){	
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results._links")
			.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=0&page=1"));
		}
		else
		{
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results._links")
			.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=0&page=1"))
			.body("next.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=0&page=2"))
			.body("last.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=0&page=" + pageCount.intValue()));		
		}
	}
	
	@Test
	public void test_002_getOrderList_byStatusPaidTest() throws Exception {

		rt.setDataLocation("getOrderListPaidTest", "orderBody");
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
		
		//get order list by status - paid
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ORDER + "?=token={token}" + "&status=2");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//check order status
		ArrayList<String> list = response.jsonPath().get("results.items.orderStatus");
		for(int i = 0; i<list.size(); i++)
		{
			Assert.assertEquals(rt.getOutputParameter("orderStatus"), list.get(i));			
		}
		
		//meta
		Integer totalCount = response.jsonPath().get("results._meta.totalCount");
		Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
		Double pageCount = Math.ceil(totalCount/perPage);
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", greaterThanOrEqualTo(1))
		.body("pageCount", equalTo(pageCount.intValue()))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(perPage.intValue()));
			
		//links
		if(totalCount <= perPage){	
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results._links")
			.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=2&page=1"));
			}
		else
			{		
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results._links")
			.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=2&page=1"))
			.body("next.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=2&page=2"))
			.body("last.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=2&page=" + pageCount.intValue()));	
		
		}
	}
	
	@Test
	public void test_003_getOrderList_byStatusFinishedTest() throws Exception {

		rt.setDataLocation("getOrderListFinishedTest", "orderBody");
		
		//get order list by status - closed
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ORDER + "?=token={token}" + "&status=7");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//check order status
		ArrayList<String> list = response.jsonPath().get("results.items.orderStatus");
		for(int i = 0; i<list.size(); i++)
		{
			Assert.assertEquals(rt.getOutputParameter("orderStatus"), list.get(i));			
		}
		
		//meta
		Integer totalCount = response.jsonPath().get("results._meta.totalCount");
		Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
		Double pageCount = Math.ceil(totalCount/perPage);
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", greaterThanOrEqualTo(1))
		.body("pageCount", equalTo(pageCount.intValue()))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(perPage.intValue()));
			
		//links
		if(totalCount <= perPage){	
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results._links")
			.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=7&page=1"));
			}
		else
			{		
			response.then().log().ifValidationFails().statusCode(200).assertThat()
			.rootPath("results._links")
			.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=7&page=1"))
			.body("next.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=7&page=2"))
			.body("last.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=7&page=" + pageCount.intValue()));	
		
		}
	}
	
	@Ignore
	@Test
	public void test_004_getOrderList_byStatusReturnCreatedTest() throws Exception {
//		String token = login(access_token);
		rt.setDataLocation("getOrderListReturnCreatedTest", "orderBody");
		
		//get order list by status - ReturnCreated
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.ORDER + "?=token={token}" + "&status=9");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//check order status
		ArrayList<String> list = response.jsonPath().get("results.items.orderStatus");
		for(int i = 0; i<list.size(); i++)
		{
			Assert.assertEquals(rt.getOutputParameter("orderStatus"), list.get(i));			
		}
		
		//meta
		Integer totolCount = Integer.parseInt(rt.getOutputParameter("totolCount"));
		Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
		Double pageCount = Math.ceil(totolCount/perPage);
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", greaterThanOrEqualTo(totolCount))
		.body("pageCount", equalTo(pageCount.intValue()))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(perPage.intValue()));
			
		//links
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._links")
		.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.ORDER + "?token=" + token + "&status=9&page=1"));
	}
	
	@AfterClass
	public static void cleanData() throws Exception {
		
//		String token = login(access_token);
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
