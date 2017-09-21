package com.ibm.cbt.rest.api.test.cart;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
public class CartTest extends CBTUtility{
	
	final static String preLoadDataFileName = "resources/cart/_preLoadData.config";
	final static String testDataFileName = "resources/cart/cartTest.xml";
	private static String token;
	private static String access_token;
	static RestTest rt = new RestTest();
	
	private static String cartlineId1 = "";
	private static String cartlineId2 = "";
	private static String itemId1 = "";
	private static String itemId2 = "";
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);
		rt.setDataFile(testDataFileName);	
		access_token = CBTUtility.oauth();
		token = login(access_token);
		//get item id
		itemId1 = CBTUtility.getItemId1(access_token);
		itemId2 = CBTUtility.getItemId2(access_token);
		
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
	}
	
	@Test
	public void test_000_addToCartTest() throws Exception {
		
		rt.setDataLocation("addToCartTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("cartlineId", notNullValue())
		.body("cartlineQuantity", equalTo(Integer.parseInt(rt.getPreLoadParameter("$cartlineQuantity"))))
		.body("itemId", equalTo(itemId1))
		.body("itemPartNumber", equalTo(rt.getPreLoadParameter("$itemPartNumber")))
		.body("itemDisplayText", equalTo(rt.getOutputParameter("itemDisplayText")))
		.body("itemImageLink", equalTo(rt.getPreLoadParameter("$itemImageLink")))
		.body("itemListPrice", equalTo(rt.getPreLoadParameter("$itemListPrice")))
		.body("itemOfferPrice", equalTo(rt.getPreLoadParameter("$itemOfferPrice")))
		.body("isGift", equalTo(rt.getPreLoadParameter("$isGift")));
		
		cartlineId1 = response.jsonPath().get("results.cartlineId");
	}
	
	@Test
	public void test_001_getCartTest() throws Exception {

		rt.setDataLocation("getCartTest", "cartBody");
		
		Response response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("taxRate", hasItem(rt.getPreLoadParameter("$taxRate")))
		.body("buyable", hasItem(Integer.parseInt(rt.getPreLoadParameter("$buyable"))))
		.body("cartlineId", hasItem(cartlineId1))
		.body("cartlineQuantity", hasItem(Integer.parseInt(rt.getPreLoadParameter("$cartlineQuantity"))))
		.body("itemId", hasItem(itemId1))
		.body("itemPartNumber", hasItem(rt.getPreLoadParameter("$itemPartNumber")))
		.body("itemDisplayText", hasItem(rt.getOutputParameter("itemDisplayText")))
		.body("itemImageLink", hasItem(rt.getPreLoadParameter("$itemImageLink")))
		.body("itemListPrice", hasItem(rt.getPreLoadParameter("$itemListPrice")))
		.body("itemOfferPrice", hasItem(rt.getPreLoadParameter("$itemOfferPrice")))
		.body("itemSalesPrice", hasItem(rt.getPreLoadParameter("$itemSalesPrice")))
		.body("isGift", hasItem(rt.getPreLoadParameter("$isGift")));
	}
	
	@Test
	public void test_002_getCartIdsTest() throws Exception {

		rt.setDataLocation("getCartByIdsTest", "cartBody");
		//add another item to cart
		String cartPath = rt.getInputParameter("addToCartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId2);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("cartlineId", notNullValue())
		.body("cartlineQuantity", equalTo(Integer.parseInt(rt.getOutputParameter("cartlineQuantity1"))))
		.body("itemId", equalTo(itemId2))
		.body("itemPartNumber", equalTo(rt.getPreLoadParameter("$itemPartNumber1")))
		.body("itemDisplayText", equalTo(rt.getOutputParameter("itemDisplayText1")))
		.body("itemImageLink", equalTo(rt.getPreLoadParameter("$itemImageLink1")))
		.body("itemListPrice", equalTo(rt.getPreLoadParameter("$itemListPrice1")))
		.body("itemOfferPrice", equalTo(rt.getPreLoadParameter("$itemOfferPrice1")))
		.body("isGift", equalTo(rt.getPreLoadParameter("$isGift")));
		
		cartlineId2 = response.jsonPath().get("results.cartlineId");
		//get cartlineList
		response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.cartlineId", hasItems(cartlineId1,cartlineId2));
		
		//get cartline by ids
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART_CARTLINEIDS  + cartlineId1 + "|" + cartlineId2 + "&token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("cartlineId", hasItems(cartlineId1,cartlineId2));
		
		
//		response.then().log().ifValidationFails().statusCode(200).assertThat()
//		.rootPath("results[0]")
//		.body("cartlineId", equalTo(cartlineId2))
//		.body("cartlineQuantity", equalTo(Integer.parseInt(rt.getOutputParameter("cartlineQuantity1"))))
//		.body("itemId", equalTo(rt.getPreLoadParameter("$itemId1")))
//		.body("itemPartNumber", equalTo(rt.getPreLoadParameter("$itemPartNumber1")))
//		.body("itemDisplayText", equalTo(rt.getOutputParameter("itemDisplayText1")))
//		.body("itemImageLink", equalTo(rt.getPreLoadParameter("$itemImageLink1")))
//		.body("itemListPrice", equalTo(rt.getPreLoadParameter("$itemListPrice1")))
//		.body("itemOfferPrice", equalTo(rt.getPreLoadParameter("$itemOfferPrice1")))
//		.body("isGift", equalTo(rt.getPreLoadParameter("$isGift")));
//		
//		response.then().log().ifValidationFails().statusCode(200).assertThat()
//		.rootPath("results[1]")
//		.body("cartlineId", equalTo(cartlineId1))
//		.body("cartlineQuantity", equalTo(Integer.parseInt(rt.getOutputParameter("cartlineQuantity"))))
//		.body("itemId", equalTo(rt.getPreLoadParameter("$itemId")))
//		.body("itemPartNumber", equalTo(rt.getPreLoadParameter("$itemPartNumber")))
//		.body("itemDisplayText", equalTo(rt.getOutputParameter("itemDisplayText")))
//		.body("itemImageLink", equalTo(rt.getPreLoadParameter("$itemImageLink")))
//		.body("itemListPrice", equalTo(rt.getPreLoadParameter("$itemListPrice")))
//		.body("itemOfferPrice", equalTo(rt.getPreLoadParameter("$itemOfferPrice")))
//		.body("isGift", equalTo(rt.getPreLoadParameter("$isGift")));

	}
	
	
	@Test
	public void test_003_updateCartQuantityTest() throws Exception {

		rt.setDataLocation("updateCartQuantityTest", "cartBody");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.put(CBTURI.CART + "/" + cartlineId1 + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("cartlineId", equalTo(cartlineId1))
		.body("itemId", equalTo(itemId1))
		.body("cartlineQuantity", equalTo(Integer.parseInt(rt.getOutputParameter("cartlineQuantity"))));
	}

	@Test
	public void test_004_calculateCartTest() throws Exception {

		rt.setDataLocation("calculateCartTest", "cartBody");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@cartlineList", cartlineId1);
		//calculate
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.itemDetail." + itemId1)	
		.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice")))
		.body("itemTax", equalTo(rt.getOutputParameter("itemTax")))
		.body("totalPrice", equalTo(rt.getOutputParameter("totalPrice")))
		.body("adjustPrice", equalTo(rt.getOutputParameter("adjustPrice")));
	
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.total")
		.body("originalPrice", equalTo(rt.getOutputParameter("originalPrice")))
		.body("shipping", equalTo(rt.getOutputParameter("shipping")))
		.body("tax", equalTo(rt.getOutputParameter("tax")))
		.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
		.body("itemNum", equalTo(rt.getOutputParameter("itemNum")));
	}
	
	@Test
	public void test_005_calculateCart_shippingTest() throws Exception {

		rt.setDataLocation("calculateCartShippingTest", "cartBody");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);

		cartBody = cartBody.replaceAll("@cartlineList", cartlineId1);
		//calculate
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.itemDetail." + itemId1)	
		.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice")))
		.body("itemTax", equalTo(rt.getOutputParameter("itemTax")))
		.body("totalPrice", equalTo(rt.getOutputParameter("totalPrice")))
		.body("adjustPrice", equalTo(rt.getOutputParameter("adjustPrice")));
	
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.total")
		.body("originalPrice", equalTo(rt.getOutputParameter("originalPrice")))
		.body("shipping", equalTo(rt.getOutputParameter("shipping")))
		.body("tax", equalTo(rt.getOutputParameter("tax")))
		.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
		.body("itemNum", equalTo(rt.getOutputParameter("itemNum")));
	}
	
	@Ignore
	@Test
	public void test_006_calculateCart_taxTest() throws Exception {

		rt.setDataLocation("calculateCartTaxTest", "cartBody");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		//add to cart, tax > 50
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		cartPath = rt.getInputParameter("calculateCartBody");
		cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@cartlineList", cartlineId1);
		//calculate		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.itemDetail." + itemId1)	
		.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice")))
		.body("itemTax", equalTo(rt.getOutputParameter("itemTax")))
		.body("totalPrice", equalTo(rt.getOutputParameter("totalPrice")))
		.body("adjustPrice", equalTo(rt.getOutputParameter("adjustPrice")));
	
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.total")
		.body("originalPrice", equalTo(rt.getOutputParameter("originalPrice")))
		.body("shipping", equalTo(rt.getOutputParameter("shipping")))
		.body("tax", equalTo(rt.getOutputParameter("tax")))
		.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
		.body("itemNum", equalTo(rt.getOutputParameter("itemNum")));
	}
	
	@Test
	public void test_007_calculateCart_multiItemsTest() throws Exception {
		
		rt.setDataLocation("calculateCartMultiItemsTest", "cartBody");				
		
		//calculate
		String cartPath = rt.getInputParameter("calculateCartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@cartlineList", cartlineId1 + "\" , \"" + cartlineId2);
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.itemDetail." + itemId1)	
		.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice1")))
		.body("itemTax", equalTo(rt.getOutputParameter("itemTax1")))
		.body("totalPrice", equalTo(rt.getOutputParameter("totalPrice1")))
		.body("adjustPrice", equalTo(rt.getOutputParameter("adjustPrice1")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.itemDetail." + itemId2)	
		.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice2")))
		.body("itemTax", equalTo(rt.getOutputParameter("itemTax2")))
		.body("totalPrice", equalTo(rt.getOutputParameter("totalPrice2")))
		.body("adjustPrice", equalTo(rt.getOutputParameter("adjustPrice2")));
	
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.total")
		.body("originalPrice", equalTo(rt.getOutputParameter("originalPrice")))
		.body("shipping", equalTo(rt.getOutputParameter("shipping")))
		.body("tax", equalTo(rt.getOutputParameter("tax")))
		.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
		.body("itemNum", equalTo(rt.getOutputParameter("itemNum")));
	}
	
	
	@Test
	public void test_008_deleteCart_byIdTest() throws Exception {

		//delete cartlineId1
		Response response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.delete(CBTURI.CART + "/" + cartlineId1 + "?=token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", equalTo(cartlineId1));
	
		//delete cartlineId2
		response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.delete(CBTURI.CART + "/" + cartlineId2 + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", equalTo(cartlineId2));
		
		//get cartline
		response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", Matchers.hasSize(0));
	}
	
	@Test
	public void test_009_deleteCart_byIdsTest() throws Exception {

		//add item to cart
		rt.setDataLocation("deteletCartByIdsTest", "cartBody");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		String cartlineId1 = response.jsonPath().get("results.cartlineId");
		
		//add another item to cart
		cartPath = rt.getInputParameter("cartBody");
		cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId2);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		String cartlineId2 = response.jsonPath().get("results.cartlineId");
		
		//delete cart by ids
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.delete(CBTURI.CART_CARTLINEIDS  + cartlineId1 + "|" + cartlineId2 + "&token={token}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", hasItems(cartlineId1,cartlineId2));
		
		//get cartline
		response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", Matchers.hasSize(0));
	}
	
	@Test
	public void test_010_requiredField_itemIdTest() throws Exception {
		
		rt.setDataLocation("requiredFieldItemIdTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".itemId", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_011_requiredField_quantityTest() throws Exception {
		
		rt.setDataLocation("requiredFieldQuantityTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_012_requiredField_updateQuantityTest() throws Exception {
		
		rt.setDataLocation("requiredFieldUpdateQuantityTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_013_null_itemIdTest() throws Exception {
		
		rt.setDataLocation("nullItemIdTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".itemId", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_014_null_quantityTest() throws Exception {
		
		rt.setDataLocation("nullQuantityTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	
	@Test
	public void test_015_null_updateQuantityTest() throws Exception {
		
		rt.setDataLocation("nullUpdateQuantityTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.REQUIRED_FIELD));
	}
	
	@Test
	public void test_016_invalid_itemIdTest() throws Exception {
		
		rt.setDataLocation("invalidItemIdTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_PRODUCT_INVALID_ITEMID, equalTo(CBTErrors.PRODUCT_INVALID_ITEMID));
	}
	
	@Test
	public void test_017_quantity_wrongTypeTest() throws Exception {
		
		rt.setDataLocation("wrongTypeQuantityTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.CART_INVALID_QUANTITY));
	}
	
	@Test
	public void test_018_quantity_negativeTest() throws Exception {
		
		rt.setDataLocation("quantityNegativeTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.CART_INVALID_QUANTITY));
	}
	
	@Test
	public void test_019_quantity_zeroTest() throws Exception {
		
		rt.setDataLocation("quantityZeroTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION + ".quantity", hasItem(CBTErrors.CART_INVALID_QUANTITY));
	}
	
	@Test
	public void test_020_caculateCart_noAddressTest() throws Exception {
		
		rt.setDataLocation("caculateCartNoAddressTest", "cartBody");	
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		String calculatePath = rt.getInputParameter("calculateBody");
		String calculateBody = rt.getMsgBodyfromJson(calculatePath);
		
		calculateBody = calculateBody.replaceAll("@cartlineList", cartlineId);
		//calculate
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(calculateBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION, equalTo(CBTErrors.CART_CALCULATION_NO_ADDRESS));
	}
	
	@Test
	public void test_021_caculateCart_requiredField_stateCodeTest() throws Exception {
		
		rt.setDataLocation("caculateCartRequiredFieldStateCodeTest", "cartBody");			
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		String calculatePath = rt.getInputParameter("calculateBody");
		String calculateBody = rt.getMsgBodyfromJson(calculatePath);
		
		calculateBody = calculateBody.replaceAll("@cartlineList", cartlineId);
		//calculate
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(calculateBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION, equalTo(CBTErrors.CART_CALCULATION_NO_STATECODE));
	}
	
	@Test
	public void test_022_calculateCart_null_stateCodeTest() throws Exception {
		
		rt.setDataLocation("caculateCartNullStateCodeTest", "cartBody");			
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		String calculatePath = rt.getInputParameter("calculateBody");
		String calculateBody = rt.getMsgBodyfromJson(calculatePath);
		
		calculateBody = calculateBody.replaceAll("@cartlineList", cartlineId);
		//calculate
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(calculateBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_FIELD_VALIDATION , equalTo(CBTErrors.CART_CALCULATION_NO_STATECODE));
	}
	
	@Test
	public void test_023_invalid_cartId_calculateCartTest() throws Exception {

		rt.setDataLocation("invalidCartIdCalculateCartTest", "cartBody");
		String cartPath = rt.getInputParameter("calculateBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);

		//calculate
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", hasSize(0));
	}
	
	@Test
	public void test_024_invalid_cartId_getCartByIdsTest() throws Exception {

		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.get(CBTURI.CART_CARTLINEIDS  + "1|2&token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", hasSize(0));
	}
	

	@Test
	public void test_025_invalid_cartId_updateCartQuantityTest() throws Exception {

		rt.setDataLocation("invalidCartIdUpdateCartQuantityTest", "cartBody");
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody).when()
				.put(CBTURI.CART + "/1?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_CART_INVALID_CARTLINEID, equalTo(CBTErrors.CART_INVALID_CARTLINEID));
	}

	@Test
	public void test_026_invalid_cartId_deleteCartTest() throws Exception {
		
		Response response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.when()
				.delete(CBTURI.CART + "/1?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_CART_INVALID_CARTLINEID, equalTo(CBTErrors.CART_INVALID_CARTLINEID));
	}

	
	@Test
	public void test_027_authorizationTest() throws Exception {
		
		rt.setDataLocation("addToCartTest", "cartBody");	
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors", hasItem(CBTErrors.NEED_LOGIN));
	}
	
	
	@Test
	public void test_028_oauth_null_cartTest() throws Exception {
		
		rt.setDataLocation("addToCartTest", "cartBody");	
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_AUTHORIZATION , equalTo(CBTErrors.TOKEN_AUTHORIZATION));	
	}
	
	@Test
	public void test_029_oauth_invalid_cartTest() throws Exception {
		
		rt.setDataLocation("invalidOauthTest", "cartBody");
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
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_TOKEN_NOT_MATCH , equalTo(CBTErrors.TOKEN_NOT_MATCH));	
	}
	
	//add carrierId, cart weight to 1kg
	@Test 
	public void test_030_calculateCart_carrierIdTest() throws Exception {

		rt.setDataLocation("calculateCartCarrierIdTest", "cartBody");
		
		//get cartline
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
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//get shipping provider id
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SHIP_PROVIDERS);
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		String carrierId = response.jsonPath().get("results.ship[0].id");	
		
		//calculate
		String calculatePath = rt.getInputParameter("calculateBody");
		String calculateBody = rt.getMsgBodyfromJson(calculatePath);
		calculateBody = calculateBody.replaceAll("@cartlineList", cartlineId);
		calculateBody = calculateBody.replaceAll("@carrierId", carrierId);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(calculateBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.itemDetail." + itemId1)	
		.body("unitPrice", equalTo(rt.getOutputParameter("unitPrice")))
		.body("itemTax", equalTo(rt.getOutputParameter("itemTax")))
		.body("totalPrice", equalTo(rt.getOutputParameter("totalPrice")))
		.body("adjustPrice", equalTo(rt.getOutputParameter("adjustPrice")));
	
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.total")
		.body("originalPrice", equalTo(rt.getOutputParameter("originalPrice")))
		.body("shipping", equalTo(rt.getOutputParameter("shipping")))
		.body("tax", equalTo(rt.getOutputParameter("tax")))
		.body("actualPrice", equalTo(rt.getOutputParameter("actualPrice")))
		.body("itemNum", equalTo(rt.getOutputParameter("itemNum")));
	}
	
	@Test
	public void test_031_calculateCart_invalid_carrierIdTest() throws Exception {

		rt.setDataLocation("calculateCartCarrierIdTest", "cartBody");
		
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		String cartlineId = response.jsonPath().get("results.cartlineId");
		
		//calculate
		String calculatePath = rt.getInputParameter("calculateBody");
		String calculateBody = rt.getMsgBodyfromJson(calculatePath);
		calculateBody = calculateBody.replaceAll("@cartlineList", cartlineId);
		calculateBody = calculateBody.replaceAll("@carrierId", "1");
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(calculateBody).when()
				.post(CBTURI.CART_CALCULATE + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_CART_INVALID_CARRIERID, equalTo(CBTErrors.CART_INVALID_CARRIERID))
		.body("results", hasSize(0));;
	}
	
	@Test
	public void test_032_buyable_Test() throws Exception {
		
		rt.setDataLocation("buyableTest", "cartBody");
		
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PRODUCT_INVALID_ITEMID, equalTo(CBTErrors.PRODUCT_INVALID_ITEMID));
	}
	
	@Test
	public void test_033_inventory_Test() throws Exception {
		
		rt.setDataLocation("inventoryTest", "cartBody");
			
		//add to cart
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);

		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_CART_NO_INVENTORY, equalTo(CBTErrors.CART_NO_INVENTORY));
	}
	
	
	@Test
	public void test_034_inventory_updateCartQuantityTest() throws Exception {

		rt.setDataLocation("inventoryUpdateCartTest", "cartBody");
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
		.body("status", equalTo(true));
		
		cartlineId1 = response.jsonPath().get("results.cartlineId");
		//update cart		
		String updatePath = rt.getInputParameter("updateBody");
		String updateBody = rt.getMsgBodyfromJson(updatePath);
		
		response = given().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(updateBody).when()
				.put(CBTURI.CART + "/" + cartlineId1 + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("errors." + CBTErrors.CODE_CART_NO_INVENTORY, equalTo(CBTErrors.CART_NO_INVENTORY))
		.body("results", hasSize(0));
		
	}
	
	@Test
	public void test_035_multiTenant_Test() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "cartBody");
		//get tenantA oauth
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		String userToken = login("autoTest4", "Test123_", accessToken);
		
		//add to cart - Tenant User A add Tenant-B product
		String cartPath = rt.getInputParameter("cartBody");
		String cartBody = rt.getMsgBodyfromJson(cartPath);
		cartBody = cartBody.replaceAll("@itemId", itemId1);
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("token", userToken)
				.body(cartBody)
				.when()
				.post(CBTURI.CART + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_TOKEN_ORG_WRONG, equalTo(CBTErrors.TOKEN_ORG_WRONG));
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
		
	}
}
