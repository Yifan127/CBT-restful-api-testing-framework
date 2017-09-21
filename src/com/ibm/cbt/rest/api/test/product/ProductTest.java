package com.ibm.cbt.rest.api.test.product;

import static org.hamcrest.Matchers.*;
import static com.jayway.restassured.RestAssured.given;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.*;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductTest extends CBTUtility{

	final static String testDataFileName = "resources/product/productTest.xml";
	final static String preLoadDataFileName = "resources/product/_preLoadData.config";
	static RestTest rt = new RestTest();
	private static String access_token;
	private static String itemId1 = "";
	private static String itemId2 = "";
	private static ArrayList<String> ids = new ArrayList<String>();

	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);
		rt.setDataFile(testDataFileName);
		access_token = CBTUtility.oauth();
		//get item id
		itemId1 = CBTUtility.getItemId1(access_token);
		itemId2 = CBTUtility.getItemId2(access_token);
		ids = CBTUtility.getProductItemIds(access_token);
	}
	
	@Test
	public void test_000_getProduct_byProductIdTest() throws Exception {
		
		rt.setDataLocation("getProductByProductIdTest", "productBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", ids.get(0))
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//basic info
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("id", equalTo(ids.get(0)))
		.body("type", equalTo(rt.getOutputParameter("type")))
		.body("buyable", equalTo(Integer.parseInt(rt.getPreLoadParameter("$buyable"))))
		.body("partNumber", equalTo(rt.getOutputParameter("partNumber")))
		.body("memberId", equalTo(rt.getPreLoadParameter("$memberId")))
		.body("manufactureName", equalTo(rt.getOutputParameter("manufactureName")))
		.body("price", hasSize(0))
		.body("inventory", hasSize(0))
		.body("name", equalTo(rt.getOutputParameter("name")))
		.body("shortDescription", equalTo(rt.getOutputParameter("shortDescription")))
//		.body("longDescription", equalTo((rt.getOutputParameter("longDescription"))))
		.body("imageLink", equalTo(rt.getOutputParameter("imageLink")))
		.body("taxRate", equalTo(rt.getPreLoadParameter("$taxRate")))
		.body("parentId", equalTo(rt.getOutputParameter("parentId")))
		.body("pics", hasSize(0));
		
		//definingAttributes
		ArrayList<String>  list = response.jsonPath().get("results.definingAttributes");
		Assert.assertEquals(1, list.size());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.definingAttributes")
		.body("id[0]", equalTo(rt.getOutputParameter("definingId")))
		.body("name[0]", equalTo(rt.getOutputParameter("definingName")))
		.body("key[0]", notNullValue())
		.body("assignedValue[0]", nullValue());
		
		list = response.jsonPath().get("results.definingAttributes[0].possibleValues");
		Assert.assertEquals(2, list.size());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.definingAttributes[0].possibleValues")
		.body("displayValue", hasItems(rt.getOutputParameter("displayValue1"),rt.getOutputParameter("displayValue2")));
		
		//cates
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.cates")
		.body("categoryId", equalTo(rt.getOutputParameter("categoryId")));
		
		list = response.jsonPath().get("results.cates.categorypath");
		Assert.assertEquals(1, list.size());

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.cates.categorypath")
		.body("id[0]", notNullValue())
		.body("name[0]", equalTo(rt.getOutputParameter("categorypath1")));

		//brand
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.brand")
		.body("countryId", equalTo(rt.getOutputParameter("countryId")))
		.body("countryName", equalTo(rt.getOutputParameter("countryName")))
		.body("name", equalTo(rt.getOutputParameter("brandName")))
		.body("fullimage", nullValue());
		
		//descriptiveAttributes
		list = response.jsonPath().get("results.descriptiveAttributes");
		Assert.assertEquals(4, list.size());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.descriptiveAttributes")
		.body("id[0]", equalTo(rt.getOutputParameter("descriptiveId1")))
		.body("name[0]", equalTo(rt.getOutputParameter("descriptiveName1")))
		.body("value[0]", equalTo(rt.getOutputParameter("descriptiveValue1")))
		.body("id[1]", equalTo(rt.getOutputParameter("descriptiveId2")))
		.body("name[1]", equalTo(rt.getOutputParameter("descriptiveName2")))
		.body("value[1]", equalTo(rt.getOutputParameter("descriptiveValue2")))
		.body("id[2]", equalTo(rt.getOutputParameter("descriptiveId3")))
		.body("name[2]", equalTo(rt.getOutputParameter("descriptiveName3")))
		.body("value[2]", equalTo(rt.getOutputParameter("descriptiveValue3")))
		.body("id[3]", equalTo(rt.getOutputParameter("descriptiveId4")))
		.body("name[3]", equalTo(rt.getOutputParameter("descriptiveName4")))
		.body("value[3]", equalTo(rt.getOutputParameter("descriptiveValue4")));

		//skuMap
		list = response.jsonPath().get("results.skuMap");
		Assert.assertEquals(2, list.size());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.skuMap")
		.body("key[0]", notNullValue())
		.body("value[0]", equalTo(ids.get(1) + "|" + rt.getPreLoadParameter("$partNumber")))
		.body("key[1]", notNullValue())
		.body("value[1]", equalTo(ids.get(2) + "|" + rt.getPreLoadParameter("$partNumber1")));
				
	}
	
	@Test
	public void test_001_getProduct_byItemIdTest() throws Exception {
		
		rt.setDataLocation("getProductByItemIdTest", "productBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", ids.get(1))
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		//basic info
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("id", equalTo(ids.get(1)))
		.body("type", equalTo(rt.getOutputParameter("type")))
		.body("buyable", equalTo(Integer.parseInt(rt.getPreLoadParameter("$buyable"))))
		.body("partNumber", equalTo(rt.getPreLoadParameter("$partNumber")))
		.body("memberId", equalTo(rt.getPreLoadParameter("$memberId")))
		.body("manufactureName", equalTo(rt.getOutputParameter("manufactureName")))
		.body("price", hasSize(0))
		.body("inventory", hasSize(0))
		.body("name", equalTo(rt.getOutputParameter("name")))
		.body("shortDescription", equalTo(rt.getOutputParameter("shortDescription")))
//		.body("longDescription", equalTo((rt.getOutputParameter("longDescription"))))
		.body("imageLink", equalTo(rt.getPreLoadParameter("$imageLink")))
		.body("taxRate", equalTo(rt.getPreLoadParameter("$taxRate")))
		.body("parentId", equalTo(ids.get(0)))
		.body("pics", hasSize(0));
		
		//definingAttributes
		ArrayList<String>  list = response.jsonPath().get("results.definingAttributes");
		Assert.assertEquals(1, list.size());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.definingAttributes")
		.body("id[0]", equalTo(rt.getOutputParameter("definingId")))
		.body("name[0]", equalTo(rt.getOutputParameter("definingName")))
		.body("key[0]", notNullValue())
		.body("assignedValue[0].key", notNullValue())
		.body("assignedValue[0].displayValue", equalTo(rt.getOutputParameter("displayValue")))
		.body("assignedValue[0].image", equalTo(rt.getOutputParameter("image")))
		.body("possibleValues[0]", nullValue());
		
		//cates
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.cates")
		.body("categoryId", equalTo(rt.getOutputParameter("categoryId")));
		
		list = response.jsonPath().get("results.cates.categorypath");
		Assert.assertEquals(1, list.size());

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.cates.categorypath")
		.body("id[0]", notNullValue())
		.body("name[0]", equalTo(rt.getOutputParameter("categorypath1")));

		//brand
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.brand")
		.body("countryId", equalTo(rt.getOutputParameter("countryId")))
		.body("countryName", equalTo(rt.getOutputParameter("countryName")))
		.body("name", equalTo(rt.getOutputParameter("brandName")))
		.body("fullimage", nullValue());
		
		//descriptiveAttributes
		list = response.jsonPath().get("results.descriptiveAttributes");
		Assert.assertEquals(2, list.size());
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.descriptiveAttributes")
		.body("id[0]", equalTo(rt.getOutputParameter("descriptiveId1")))
		.body("name[0]", equalTo(rt.getOutputParameter("descriptiveName1")))
		.body("value[0]", equalTo(rt.getOutputParameter("descriptiveValue1")))
		.body("id[1]", equalTo(rt.getOutputParameter("descriptiveId2")))
		.body("name[1]", equalTo(rt.getOutputParameter("descriptiveName2")))
		.body("value[1]", equalTo(rt.getOutputParameter("descriptiveValue2")));

	}
	
	@Test
	public void test_002_getProduct_realTimeTest() throws Exception {
		
		rt.setDataLocation("getProductRealTimeTest", "productBody");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", itemId1)
				.when()
				.get(CBTURI.PRODUCT_REALTIME + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
//		response.then().log().ifValidationFails().statusCode(200).assertThat()
//		.rootPath("results")
//		.body("inventory", equalTo(Integer.parseInt(rt.getOutputParameter("inventory"))));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.price")
		.body("listPrice", equalTo(rt.getPreLoadParameter("$listPrice")))
		.body("offerPrice", equalTo(rt.getPreLoadParameter("$offerPrice")))
		.body("salesPrice", equalTo(rt.getPreLoadParameter("$salesPrice")))
		.body("currency", equalTo(rt.getPreLoadParameter("$currency")));
	}
	

	@Test
	public void test_003_getProduct_byIdsTest() throws Exception {
		
		rt.setDataLocation("getProductByIdsTest", "productBody");
		String itemIds = itemId1 + "|" + itemId2;
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemIds", itemIds)
				.when()
				.get(CBTURI.PRODUCT_IDS + "{itemIds}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		ArrayList<String> list = response.jsonPath().get("results");
		Assert.assertEquals(2, list.size());
		
		//item1
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("id[0]", equalTo(itemId1))
		.body("type[0]", equalTo(rt.getOutputParameter("type1")))
		.body("buyable[0]", equalTo(Integer.parseInt(rt.getPreLoadParameter("$buyable"))))
		.body("partNumber[0]", equalTo(rt.getPreLoadParameter("$partNumber")))
		.body("memberId[0]", equalTo(rt.getPreLoadParameter("$memberId")))
//		.body("manufactureName", equalTo(rt.getOutputParameter("manufactureName1")))
		.body("name[0]", equalTo(rt.getOutputParameter("name1")))
		.body("shortDescription[0]", equalTo(rt.getOutputParameter("shortDescription1")))
		.body("imageLink[0]", equalTo(rt.getPreLoadParameter("$imageLink")));
//		.body("taxRate[0]", equalTo(rt.getPreLoadParameter("$taxRate")))
//		.body("parentId", equalTo(ids.get(0)));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.price")
		.body("listPrice[0]", equalTo(rt.getPreLoadParameter("$listPrice")))
		.body("offerPrice[0]", equalTo(rt.getPreLoadParameter("$offerPrice")))
		.body("salesPrice[0]", equalTo(rt.getPreLoadParameter("$salesPrice")))
		.body("currency[0]", equalTo(rt.getPreLoadParameter("$currency")));
		
		//item2
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results")
		.body("id[1]", equalTo(itemId2))
		.body("type[1]", equalTo(rt.getOutputParameter("type2")))
		.body("buyable[1]", equalTo(Integer.parseInt(rt.getOutputParameter("buyable2"))))
		.body("partNumber[1]", equalTo(rt.getOutputParameter("partNumber2")))
		.body("memberId[1]", equalTo(rt.getPreLoadParameter("$memberId")))
//		.body("manufactureName", equalTo(rt.getOutputParameter("manufactureName2")))
		.body("name[1]", equalTo(rt.getOutputParameter("name2")))
		.body("shortDescription[1]", equalTo(rt.getOutputParameter("shortDescription2")))
		.body("imageLink[1]", equalTo(rt.getOutputParameter("imageLink2")));
//		.body("taxRate[1]", equalTo(rt.getPreLoadParameter("$taxRate")));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.price")
		.body("listPrice[1]", equalTo(rt.getOutputParameter("listPrice2")))
		.body("offerPrice[1]", equalTo(rt.getOutputParameter("offerPrice2")))
		.body("salesPrice[1]", equalTo(rt.getOutputParameter("salesPrice2")))
		.body("currency[1]", equalTo(rt.getOutputParameter("currency2")));
	}

	@Test
	public void test_004_getProduct_invalidIdTest() throws Exception {
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.PRODUCT + "/1");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PRODUCT_INVALID_ITEMID, equalTo(CBTErrors.PRODUCT_INVALID_ITEMID));
	}
	
	@Test
	public void test_005_getProduct_realTime_invalidIdTest() throws Exception {
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.PRODUCT_REALTIME + "/1");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PRODUCT_INVALID_ITEMID, equalTo(CBTErrors.PRODUCT_INVALID_ITEMID));
	}
	
	@Test
	public void test_006_getProduct_invalidIdsTest() throws Exception {
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.PRODUCT_IDS + "1|2");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", hasSize(0));
	}
	
	@Ignore
	@Test
	public void test_007_getProduct_byIds_maxTest() throws Exception {
		
		rt.setDataLocation("getProductByIdsMaxTest", "productBody");
		String itemIds = rt.getInputParameter("itemIds");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemIds", itemIds)
				.when()
				.get(CBTURI.PRODUCT_IDS + "{itemIds}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results", hasSize(50));
	}
	
	@Ignore
	@Test
	public void test_008_getProduct_byIds_boundaryTest() throws Exception {
		
		rt.setDataLocation("getProductByIdsBoundaryTest", "productBody");
		String itemIds = rt.getInputParameter("itemIds");
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemIds", itemIds)
				.when()
				.get(CBTURI.PRODUCT_IDS + "{itemIds}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_PRODUCT_MAX_IDS, equalTo(CBTErrors.PRODUCT_MAX_IDS));;
	}
	
	
	@Test
	public void test_009_oauth_null_productTest() throws Exception {
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.pathParam("itemId", itemId1)
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_AUTHORIZATION, containsString(CBTErrors.TOKEN_AUTHORIZATION));
	}
	
	@Test
	public void test_010_oauth_invalid_productTest() throws Exception {
		
		rt.setDataLocation("invalidOauthTest", "productBody");
		
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("itemId", itemId1)
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("result", nullValue())
		.body("errors." + CBTErrors.CODE_TOKEN_ORG_WRONG, containsString(CBTErrors.TOKEN_ORG_WRONG));
	}
	
	@Test
	public void test_011_multiTenant_Test() throws Exception {
		
		rt.setDataLocation("multiTenantTest", "productBody");
		//get tenantA oauth
		String oauthBodyPath = rt.getInputParameter("oauthBody");
		String oauthBody = rt.getMsgBodyfromJson(oauthBodyPath);
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post("oauth2/token");
		
		String accessToken = "Bearer " + response.jsonPath().get("results.access_token");
		
		//Tenant User A get Tenant-B product
		
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", accessToken)
				.pathParam("itemId", itemId1)
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(false))
		.body("results", hasSize(0))
		.body("errors." + CBTErrors.CODE_TOKEN_ORG_WRONG, equalTo(CBTErrors.TOKEN_ORG_WRONG));
	}
}
