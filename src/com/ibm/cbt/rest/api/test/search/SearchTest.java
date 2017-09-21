package com.ibm.cbt.rest.api.test.search;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.ibm.cbt.rest.api.test.utilities.CBTURI;
import com.ibm.cbt.rest.api.test.utilities.CBTUtility;
import com.ibm.cbt.rest.test.restassured.RestTest;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

@SuppressWarnings("deprecation")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchTest extends CBTUtility{

	final static String preLoadDataFileName = "resources/search/_preLoadData.config";
	final static String testDataFileName = "resources/search/searchTest.xml";

	private static String access_token;
	private static Integer totalPage;
	static RestTest rt = new RestTest();
	
	@BeforeClass
	public static void setUp() throws Exception {
		rt.initialize(preLoadDataFileName);	
		rt.setDataFile(testDataFileName);	
		access_token = CBTUtility.oauth();	
	}
	
	@Test
	public void test_000_searchAll() throws Exception {

		rt.setDataLocation("searchAllTest", "searchBody");
	
		//get all products
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SEARCH);

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
	    //items
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.items")
		.body("name", hasItems(rt.getOutputParameter("name1"),rt.getOutputParameter("name2"),
				rt.getOutputParameter("name3"),rt.getOutputParameter("name4"),
				rt.getOutputParameter("name5")));
		
		//meta
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount",  greaterThanOrEqualTo(Integer.parseInt(rt.getOutputParameter("totalCount"))))
		.body("pageCount", greaterThanOrEqualTo(Integer.parseInt(rt.getOutputParameter("pageCount"))))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(Integer.parseInt(rt.getOutputParameter("perPage"))));
		
		//links
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._links")
		.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?page=1"));	
	}
	
	@Test
	public void test_001_search_keywordTest() throws Exception {

		rt.setDataLocation("searchKeywordTest", "searchBody");
	
		//get all products
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SEARCH + "?keyword=Bveeno");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
	    //items
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results.items")
		.body("name", hasItem(rt.getOutputParameter("name")));
		
		//meta
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", equalTo(Integer.parseInt(rt.getOutputParameter("totalCount"))))
		.body("pageCount", equalTo(Integer.parseInt(rt.getOutputParameter("pageCount"))))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(Integer.parseInt(rt.getOutputParameter("perPage"))));
		
		//links
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._links")
		.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?keyword=Bveeno&page=1"));	
	}
	
	@Test
	public void test_002_search_perPageTest() throws Exception {

		rt.setDataLocation("searchPerpageTest", "searchBody");
	
		//get all products
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SEARCH + "?per-page=3");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
	    //items
		ArrayList<String> itemList = response.jsonPath().get("results.items.id");
		Assert.assertEquals(3, itemList.size());

		//meta
		Integer totalCount = response.jsonPath().get("results._meta.totalCount");
		Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
		Double pageCount = Math.ceil(totalCount/perPage);
		totalPage = pageCount.intValue();
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", greaterThanOrEqualTo(Integer.parseInt(rt.getOutputParameter("totalCount"))))
		.body("pageCount", equalTo(totalPage))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(perPage.intValue()));
		
		//links
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._links")
		.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=1"))
		.body("next.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=2"))
		.body("last.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=" + totalPage));	
	}
	
	@Test
	public void test_003_search_middlePageTest() throws Exception {

		rt.setDataLocation("searchMiddlePageTest", "searchBody");
	
		//get all products
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SEARCH + "?per-page=3&page=2");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
	    //items
		ArrayList<String> itemList = response.jsonPath().get("results.items.id");
		Assert.assertEquals(3, itemList.size());
		//meta
		Double perPage = Double.parseDouble(rt.getOutputParameter("perPage"));
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", greaterThanOrEqualTo(Integer.parseInt(rt.getOutputParameter("totalCount"))))
		.body("pageCount", equalTo(totalPage))
		.body("currentPage", equalTo(Integer.parseInt(rt.getOutputParameter("currentPage"))))
		.body("perPage", equalTo(perPage.intValue()));
		
		//links
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._links")
		.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=2"))
		.body("first.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=1"))
		.body("prev.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=1"))
		.body("next.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=3"))
		.body("last.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=" + totalPage));	
	}
	
	@Test
	public void test_004_search_lastPageTest() throws Exception {

		rt.setDataLocation("searchLastPageTest", "searchBody");
			
		//get all products
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.when()
				.get(CBTURI.SEARCH + "?per-page=3&page=" + totalPage);

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
	    //items
		Integer totalCount = response.jsonPath().get("results._meta.totalCount");
		int listSize = totalCount % 3;
		ArrayList<String> itemList = response.jsonPath().get("results.items.id");
		Assert.assertEquals(listSize, itemList.size());
		
		//meta
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._meta")
		.body("totalCount", greaterThanOrEqualTo(Integer.parseInt(rt.getOutputParameter("totalCount"))))
		.body("pageCount", equalTo(totalPage))
		.body("currentPage", equalTo(totalPage))
		.body("perPage", equalTo(Integer.parseInt(rt.getOutputParameter("perPage"))));
		
		//links
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.rootPath("results._links")
		.body("self.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=" + totalPage))
		.body("first.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=1"))
		.body("prev.href", equalTo(RestAssured.baseURI + ":" + RestAssured.port + "/" + CBTURI.SEARCH + "?per-page=3&page=" + (totalPage - 1)));	
	}
}
