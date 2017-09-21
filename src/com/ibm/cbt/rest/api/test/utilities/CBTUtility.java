package com.ibm.cbt.rest.api.test.utilities;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.*;
import java.util.*;

import com.ibm.cbt.rest.api.test.passport.RegisterTest;
import com.ibm.cbt.rest.test.restassured.*;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.*;
import com.jayway.restassured.response.Response;

public class CBTUtility 
{
	private static String USER_NAME = "";
    private static String PASSWORD = "";
    private static String CLIENT_ID = "";
    private static String CLIENT_SECRET = "";
    public static String ACCESS_TOKEN = "";
    public static String ITEM_NAME1="Bveeno婴儿洗发沐浴露二合一";
    public static String ITEM_NAME2="SODITHO妈妈身体按摩油";
	

    public static String oauth()
    {
    	try {
			InputStream input = new FileInputStream("resources/restAssured.config");
			Properties prop = new Properties();
			prop.load(input);
			String id = prop.getProperty("CLIENT_ID");
	        if(id.trim().length() > 0)
	        	CLIENT_ID = id;
	        String secret = prop.getProperty("CLIENT_SECRET");
	        if(secret.trim().length() > 0)
	        	CLIENT_SECRET = secret;	
			input.close();
		} catch (IOException e) {
			Log.info("Rest Assured setup. Config file not found : resources/restAssured.config");
			e.printStackTrace(System.err);
	    }
    	
		String oauthBody = "{\"grant_type\" : \"client_credentials\",\"client_id\" : " + "\"" + CLIENT_ID + "\" , \"client_secret\" : " + "\"" + CLIENT_SECRET + "\"}";
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.body(oauthBody)
				.when()
				.post(CBTURI.OAUTH);
		
		String token = "Bearer " + response.jsonPath().get("results.access_token");
		
		return token;
    }
    
	protected static String login(String userName, String password, String access_token)
    {
 
    	String loginBody = "{\"username\" : " + "\"" + userName + "\" , \"password\" : " + "\"" + password + "\"}";
    	Response response = given().log().ifValidationFails().contentType("application/json")
    			.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
    	
    	String cookie = response.getCookie("PHPSESSID");
        if(cookie != null && cookie.trim().length() > 0)
        {
            RestAssured.config = RestAssuredConfig.config().sessionConfig((new SessionConfig()).sessionIdName("PHPSESSID"));
            RestAssured.sessionId = cookie;
        } else
        {
            RestAssured.config = null;
            RestAssured.sessionId = null;
        }
        
        RestAssured.config = RestAssuredConfig.config().encoderConfig(new EncoderConfig().defaultContentCharset("UTF-8"));
        RestAssured.config = RestAssuredConfig.config().decoderConfig(new DecoderConfig().defaultContentCharset("UTF-8"));
        
        String token = response.jsonPath().get("results.accessToken");
        
            
        return token;
    }
	
	protected static String login(String access_token)
    {	
		String loginBody;
		
		try {
			InputStream input = new FileInputStream("resources/restAssured.config");
			Properties prop = new Properties();
			prop.load(input);
			String user = prop.getProperty("AUTH_USER");
	        if(user.trim().length() > 0)
	            USER_NAME = user;
	        String pwd = prop.getProperty("AUTH_PW");
	        if(pwd.trim().length() > 0)
	            PASSWORD = pwd;
	        String port = prop.getProperty("PORT");
			if((port!=null) && (port.trim().length() > 0)){
	        	RestAssured.port = Integer.parseInt(port);
	        }
			String url = prop.getProperty("BASEURI");
			if((url!=null) && (url.trim().length() > 0)){
				RestAssured.baseURI= url;
	        }	
			input.close();
		} catch (IOException e) {
			Log.info("Rest Assured setup. Config file not found : resources/restAssured.config");
			e.printStackTrace(System.err);
	    }
		
		if(RegisterTest.USERNAME!=null && RegisterTest.USERNAME.length()!=0)
		{
			loginBody = "{\"username\" : " + "\"" + RegisterTest.USERNAME + "\" , \"password\" : " + "\"" + PASSWORD + "\"}";
		}else
		{
			loginBody = "{\"username\" : " + "\"" + USER_NAME + "\" , \"password\" : " + "\"" + PASSWORD + "\"}";
		}
		
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.body(loginBody)
				.when()
				.post(CBTURI.LOGIN);
		
		RestAssured.config = RestAssuredConfig.config().encoderConfig(new EncoderConfig().defaultContentCharset("UTF-8"));
		RestAssured.config = RestAssuredConfig.config().encoderConfig(new EncoderConfig().defaultQueryParameterCharset("UTF-8"));
        RestAssured.config = RestAssuredConfig.config().decoderConfig(new DecoderConfig().defaultContentCharset("UTF-8"));
    
        String cookie = response.getCookie("PHPSESSID");
        if(cookie != null && cookie.trim().length() > 0)
        {
            RestAssured.config = RestAssuredConfig.config().sessionConfig((new SessionConfig()).sessionIdName("PHPSESSID"));
            RestAssured.sessionId = cookie;
        } else
        {
            RestAssured.config = null;
            RestAssured.sessionId = null;
        }
        
        String token = response.jsonPath().get("results.accessToken");
        
        return token;
    }
	
	public static void changePortToBackEnd() throws IOException
	{
		try {
			InputStream input = new FileInputStream("resources/restAssured.config");
			Properties prop = new Properties();
			prop.load(input);
	        	
			String port = prop.getProperty("PORT_BACKEND");
			if((port!=null) && (port.trim().length() > 0)){
	        	RestAssured.port = Integer.parseInt(port);
	        }
			String url = prop.getProperty("BASEURI_BACKEND");
			if((url!=null) && (url.trim().length() > 0)){
				RestAssured.baseURI= url;
	        }		
			input.close();
		} catch (IOException e) {
			Log.info("Rest Assured setup. Config file not found : resources/restAssured.config");
			e.printStackTrace(System.err);
	    }		
	}
	
	public static void changePortToBAL() throws IOException
	{
		try {
			InputStream input = new FileInputStream("resources/restAssured.config");
			Properties prop = new Properties();
			prop.load(input);
	        	
			String port = prop.getProperty("PORT");
			if((port!=null) && (port.trim().length() > 0)){
	        	RestAssured.port = Integer.parseInt(port);
	        }
			String url = prop.getProperty("BASEURI");
			if((url!=null) && (url.trim().length() > 0)){
				RestAssured.baseURI= url;
	        }		
			input.close();
		} catch (IOException e) {
			Log.info("Rest Assured setup. Config file not found : resources/restAssured.config");
			e.printStackTrace(System.err);
	    }		
	}
	
	public static ArrayList<String> getProductItemIds(String access_token)
	{
		ArrayList<String> ids = new ArrayList<String>();
		//search product by keyword
		String productId = "";
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("keyword", "Bveeno")
				.when()
				.get(CBTURI.SEARCH + "?keyword={keyword}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		productId = response.jsonPath().get("results.items[0].id");
		ids.add(productId);
		//get sku map
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", productId)
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		ArrayList<String> value = response.jsonPath().get("results.skuMap.value");
		for(String v : value)
		{
			String[] s = v.split("\\|");
			ids.add(s[0]);
		}

		return ids;
	}
	
	public static String getItemId1(String access_token)
	{
		//search product by keyword
		String itemId = "";
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("keyword", "Bveeno")
				.when()
				.get(CBTURI.SEARCH + "?keyword={keyword}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		itemId = response.jsonPath().get("results.items[0].id");
		//get sku map
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", itemId)
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		String value = response.jsonPath().get("results.skuMap[0].value");
		String[] itemIds = value.split("\\|");
		return itemIds[0];
		
//		ArrayList<String> itemNameList = response.jsonPath().get("results.items.name");
//		ArrayList<String> itemIdList = response.jsonPath().get("results.items.id");
//		Map<String, String> map = new HashMap<String, String>();
//		for(int i =0;i<itemIdList.size();i++){
//            map.put(itemNameList.get(i), itemIdList.get(i));  
//        }
//		
//		//get itemId
//		itemId = map.get(ITEM_NAME1);	
	}
	
	public static String getItemId2(String access_token)
	{
		//search product by keyword
		String itemId = "";
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("keyword", "SODITHO")
				.when()
				.get(CBTURI.SEARCH + "?keyword={keyword}");

		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true));
		
		itemId = response.jsonPath().get("results.items[0].id");
		//get sku map
		response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("itemId", itemId)
				.when()
				.get(CBTURI.PRODUCT + "/{itemId}");
		
		String value = response.jsonPath().get("results.skuMap[0].value");
		String[] itemIds = value.split("\\|");
		return itemIds[0];
		
//		ArrayList<String> itemNameList = response.jsonPath().get("results.items.name");
//		ArrayList<String> itemIdList = response.jsonPath().get("results.items.id");
//		Map<String, String> map = new HashMap<String, String>();
//		for(int i =0;i<itemIdList.size();i++){
//            map.put(itemNameList.get(i), itemIdList.get(i));  
//        }
//		//get itemId
//		itemId = map.get(ITEM_NAME2);	
	}
	
	public static String addAddress(String access_token, String token, String addressBody)
	{
		//add to cart
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(addressBody)
				.when()
				.post(CBTURI.ADDRESS + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.addressId", notNullValue());
		
		String addressId = response.jsonPath().get("results.addressId");
		return addressId;
	}
	
	public static String addToCart(String access_token, String token, String cartBody)
	{
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
		return cartlineId;
	}
	
	public static String submitOrder(String access_token, String token, String orderBody)
	{
		//submit order
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(orderBody).when()
				.post(CBTURI.ORDER + "?=token={token}");
		
		response.then().log().ifValidationFails().statusCode(200).assertThat()
		.body("status", equalTo(true))
		.body("results.orderId", notNullValue());
		
		String orderId = response.jsonPath().get("results.orderId");
		return orderId;
	}
	
	public static Response cancelOrder(String access_token, String token, String orderId)
	{
		//cancel order
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("orderId", orderId)
				.delete(CBTURI.ORDER + "/{orderId}?=token={token}");
		
		return response;
	}
	
	public static Response getOrder(String access_token, String token, String orderId)
	{
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//get order
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.pathParam("orderId", orderId)
				.when()
				.get(CBTURI.ORDER + "/{orderId}" + "?=token={token}");
		response.then().log().all().statusCode(200);
		return response;
	}
	
	public static Response payOrder(String access_token, String token, String paymentBody)
	{
		//pay order
		Response response = given().log().ifValidationFails().contentType("application/json")
				.header("Authorization", access_token)
				.pathParam("token", token)
				.body(paymentBody).when()
				.post(CBTURI.PAYMENT + "?=token={token}");
		return response;
	}
	
	public static void scheduleOrder(String orderId) throws IOException
	{
		changePortToBackEnd();
		String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z").format(new Date());
		String scheduleBody = "{\"updateTimestamp\" : " + "\"" + timestamp + "\" , \"updaterId\" : \"Tester\"}";
		//schedule order
		given().log().ifValidationFails().contentType("application/json")
				.pathParam("orderId", orderId)
				.body(scheduleBody).when()
				.post(CBTURI.OMS + "/{orderId}/schedule");
		
		changePortToBAL();
	}
	
	public static void releaseOrder(String orderId) throws IOException
	{
		changePortToBackEnd();
		String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z").format(new Date());
		String releaseBody = "{\"updateTimestamp\" : " + "\"" + timestamp + "\" , \"updaterId\" : \"Tester\"}";
		//schedule order
		given().log().ifValidationFails().contentType("application/json")
				.pathParam("orderId", orderId)
				.body(releaseBody).when()
				.post(CBTURI.OMS + "/{orderId}/release");
		
		changePortToBAL();
	}
	
	public static void shipOrder(String orderId) throws IOException
	{
		changePortToBackEnd();
		String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z").format(new Date());
		String shipBody = "{\"updateTimestamp\" : " + "\"" + timestamp + "\" , \"carrierCompany\" : \"YTO\", \"carrierNumber\" : \"123456\"}";
		//schedule order
		given().log().ifValidationFails().contentType("application/json")
				.pathParam("orderId", orderId)
				.body(shipBody).when()
				.post(CBTURI.OMS + "/{orderId}/ship");
		
		changePortToBAL();
	}
	
	public static void confirmReceiveOrder(String access_token, String token, String receiveOrderBody) throws IOException
	{
		//confirm receive order
		given().log().ifValidationFails().contentType("application/json")
		.header("Authorization", access_token)
		.pathParam("token", token)
		.body(receiveOrderBody)
		.when()
		.post(CBTURI.ORDER_CONFIRM_RECEIVE + "?=token={token}");
		
	}
	
}
