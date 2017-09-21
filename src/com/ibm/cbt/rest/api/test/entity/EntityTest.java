package com.ibm.cbt.rest.api.test.entity;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.CBTURI;
import com.ibm.cbt.rest.api.test.utilities.CBTUtility;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import com.jayway.restassured.response.Response;
import com.ibm.cbt.rest.test.restassured.RestTest;

/**
 * @author yifzhang
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityTest extends CBTUtility{

	final static String preLoadDataFileName = "resources/entity/_preLoadData.config";
	final static String testDataFileName = "resources/entity/entityTest.xml";
	static RestTest rt = new RestTest();
	private static String token;
	private static String access_token;	
	private static String cartlineId = "";
	private static String itemId1 = "";
	private static String addressId = "";
	private static String orderId = "";
	private String tenantId = "autoTest";
	
	@BeforeClass
	public static void setUp() throws Exception {		
		rt.initialize(preLoadDataFileName);
		rt.setDataFile(testDataFileName);	
		access_token = CBTUtility.oauth();
		token = login(access_token);
		//get item id
		itemId1 = CBTUtility.getItemId1(access_token);
		
		//add address
		rt.setDataLocation("setup", "entityBody");
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
	public void test_000_search_orderId_equalToTest() throws Exception {
		
		rt.setDataLocation("orderIdEqualToTest", "entityBody");	
		
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
		
		cartlineId = response.jsonPath().get("results.cartlineId");
		
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
		
		//get order from entity
		changePortToBackEnd();
		while(true){
			Thread.sleep(1000);
			response = given().log().ifValidationFails().contentType("application/json")
					.pathParam("tenantId", rt.getPreLoadParameter("$orgId"))
					.pathParam("orderId", orderId)
					.when()
					.get(CBTURI.ENTITY + "/{tenantId}/order/EC/{orderId}");
			response.then().log().all();
			if(response.getStatusCode() == 200)
			{
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.body("id", notNullValue())
				.body("orderId", equalTo(orderId))
				.body("sourcePlatform", equalTo(rt.getOutputParameter("sourcePlatform")))
				.body("buyerId", equalTo(rt.getPreLoadParameter("$userId")))
				.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
				.body("createTimestamp", notNullValue())
				.body("updateTimestamp", notNullValue())
				.body("creatorId", equalTo(rt.getPreLoadParameter("$userId")))
				.body("updaterId", equalTo(rt.getPreLoadParameter("$userId")))
				.body("tenantId", equalTo(rt.getPreLoadParameter("$orgId")))
				.body("channelId", equalTo(rt.getOutputParameter("channelId")))
				.body("ownerId", equalTo(rt.getPreLoadParameter("$orgId")))
				.body("orderComment", equalTo(rt.getOutputParameter("orderComment")))
				.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
				.body("shippingOriginalAmount", equalTo(Float.parseFloat(rt.getOutputParameter("shippingOriginalAmount"))))
				.body("shippingAmount", equalTo(Float.parseFloat(rt.getOutputParameter("shippingAmount"))))
				.body("taxAmount", equalTo(Float.parseFloat(rt.getOutputParameter("taxAmount"))))
				.body("totalPaymentAmount", equalTo(Float.parseFloat(rt.getOutputParameter("totalPaymentAmount"))))
				.body("totalProductAmount", equalTo(Float.parseFloat(rt.getOutputParameter("totalProductAmount"))))
				.body("totalAdjustmentAmount", equalTo(Float.parseFloat(rt.getOutputParameter("totalAdjustmentAmount"))))
				.body("currency", equalTo(rt.getOutputParameter("currency")));
				
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("orderShippingInfo")
				.body("address", equalTo(rt.getOutputParameter("address")))
				.body("receiverName", equalTo(rt.getOutputParameter("receiverName")))
				.body("receiverMobile", equalTo(rt.getOutputParameter("receiverMobile")))
				.body("countryCode", equalTo(rt.getOutputParameter("countryCode")))
				.body("countryName", equalTo(rt.getOutputParameter("countryName")))
				.body("cityName", equalTo(rt.getOutputParameter("cityName")))
				.body("cityCode", equalTo(rt.getOutputParameter("cityCode")))
				.body("stateName", equalTo(rt.getOutputParameter("stateName")))
				.body("stateCode", equalTo(rt.getOutputParameter("stateCode")))
				.body("postcode", equalTo(rt.getOutputParameter("postcode")));
				
				response.then().log().ifValidationFails().statusCode(200).assertThat()
				.rootPath("orderLineList[0]")
				.body("orderLineId", notNullValue())
				.body("itemId", equalTo(itemId1))
				.body("itemPartnumber", equalTo(rt.getPreLoadParameter("$itemPartnumber")))
				.body("unitPrice", equalTo(Float.parseFloat(rt.getOutputParameter("unitPrice"))))
				.body("tax", equalTo(Float.parseFloat(rt.getOutputParameter("tax"))))
				.body("quantity", equalTo(Integer.parseInt(rt.getOutputParameter("quantity"))))
				.body("uom", equalTo(rt.getOutputParameter("uom")))
				.body("createTimestamp", notNullValue())
				.body("updateTimestamp", notNullValue())	
				.body("itemImageLink", equalTo(rt.getOutputParameter("itemImageLink")));
				break;
			}
		}
		changePortToBAL();
	}
	
	@Ignore
	@Test
	public void test_001_search_createTime_betweenTest() throws Exception {
		
		rt.setDataLocation("createTimeBetweenTest", "entityBody");			
		Response response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("tenantId", tenantId)
				.pathParam("startTime", rt.getInputParameter("startTime"))
				.pathParam("endTime", rt.getInputParameter("endTime"))
				.when()
				.get(CBTURI.ENTITY + "/{tenantId}/order?where=createTimestamp>=" + "\"{startTime}\"&createTimestamp<=" + "\"{endTime}\"");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("totalCount", equalTo(1))
		.body("currentCount", equalTo(1));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("data[0]")
		.body("id", notNullValue())
		.body("createTimestamp", equalTo(rt.getOutputParameter("createTimestamp")))
		.body("tenantId", equalTo(rt.getOutputParameter("tenantId")))
		.body("orderId", equalTo(rt.getOutputParameter("orderId")))
		.body("sourcePlatform", equalTo(rt.getOutputParameter("sourcePlatform")))
		.body("buyerId", equalTo(rt.getPreLoadParameter("$userId")))
		.body("orderStatus", equalTo(rt.getOutputParameter("orderStatus")))
		.body("paymentType", equalTo(rt.getOutputParameter("paymentType")))
		.body("paymentTimestamp", equalTo(rt.getOutputParameter("paymentTimestamp")))
		.body("carrierNumber", notNullValue())
		.body("paymentMethod", equalTo(rt.getOutputParameter("paymentMethod")))
		.body("shippingOriginalAmount", equalTo(Float.parseFloat(rt.getOutputParameter("shippingOriginalAmount"))))
		.body("shippingAmount", equalTo(Float.parseFloat(rt.getOutputParameter("shippingAmount"))))
		.body("taxAmount", equalTo(Float.parseFloat(rt.getOutputParameter("taxAmount"))))
		.body("totalPaymentAmount", equalTo(Float.parseFloat(rt.getOutputParameter("totalPaymentAmount"))))
		.body("totalProductAmount", equalTo(Float.parseFloat(rt.getOutputParameter("totalProductAmount"))))
		.body("adjustmentAmount", equalTo(Float.parseFloat(rt.getOutputParameter("adjustmentAmount"))))
		.body("currency", equalTo(rt.getOutputParameter("currency")));
		
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("orderShippingInfo")
		.body("address", equalTo(rt.getOutputParameter("address")))
		.body("receiverName", equalTo(rt.getOutputParameter("receiverName")))
		.body("receiverMobile", equalTo(rt.getOutputParameter("receiverMobile")))
		.body("countryCode", equalTo(rt.getOutputParameter("countryCode")))
		.body("countryName", equalTo(rt.getOutputParameter("countryName")))
		.body("cityName", equalTo(rt.getOutputParameter("cityName")))
		.body("cityCode", equalTo(rt.getOutputParameter("cityCode")))
		.body("stateName", equalTo(rt.getOutputParameter("stateName")))
		.body("stateCode", equalTo(rt.getOutputParameter("stateCode")))
		.body("postcode", equalTo(rt.getOutputParameter("postcode")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("orderLineList[0]")
		.body("orderLineId", notNullValue())
		.body("quantity", equalTo(Integer.parseInt(rt.getOutputParameter("quantity"))))
		.body("uom", equalTo(rt.getOutputParameter("uom")))
		.body("unitPrice", equalTo(Float.parseFloat(rt.getOutputParameter("unitPrice"))))
		.body("tax", equalTo(Float.parseFloat(rt.getOutputParameter("tax"))))
		.body("adjustment", equalTo(Float.parseFloat(rt.getOutputParameter("adjustment"))))
		.body("itemId", equalTo(rt.getOutputParameter("itemId")))
		.body("itemPartnumber", equalTo(rt.getOutputParameter("itemPartnumber")))
		.body("itemLink", equalTo(rt.getOutputParameter("itemLink")))
		.body("itemImageLink", equalTo(rt.getOutputParameter("itemImageLink")));
	}
	
	@AfterClass
	public static void cleanData() throws Exception {
		changePortToBAL();
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
