package com.ibm.cbt.rest.api.test.utilities;

public class CBTURI {
	
	/*----------------Provision---------------*/
    public final static String  PROVISION = "provision/provision";
		
	/*----------------Address---------------*/
	public final static String  ADDRESS = "v2/addresses";
	public final static String  ADDRESS_DEFAULT = "v2/addresses/default";
	
	/*----------------Shipping---------------*/
	public final static String JURST_ALL_PROVINCE = "v2/jurst/province?code=CN";
	public final static String JURST_SUB_PROVINCE = "v2/jurst/sub-jurisdiction?code=";
	public final static String SHIP_PROVIDERS = "v2/ship/providers";

	/*----------------Passport---------------*/
	//oauth
	public final static String  OAUTH = "oauth2/token";
	//login
	public final static String  LOGIN = "v2/users/login";
	//logout
	public final static String  LOGOUT = "v2/users/logout";
	//register
	public final static String  REGISTER = "v2/users/register";
	//password
	public final static String  PASSWORD = "v2/users/password";
	
	
	/*----------------Trade---------------*/
	//cart
	public final static String  CART = "v2/carts";
	public final static String  CART_CALCULATE = "v2/carts/calculate";
	public final static String  CART_CARTLINEIDS = "v2/carts/cartlineids?ids=";
	
	//order
	public final static String  ORDER = "v2/orders";
	public final static String  ORDER_CONFIRM_RECEIVE = "v2/orders/confirmreceived";
	
	public final static String  ORDER_BACKEND_BASE = "trade-web/rest/v1/order";
	
	/*----------------Product---------------*/
	public final static String  PRODUCT = "v2/products";
	public final static String  PRODUCT_REALTIME = "v2/products/realtimeinfo"; 
	public final static String  PRODUCT_IDS = "v2/products/ids?ids=";
	
	/*----------------Search---------------*/
	public final static String  SEARCH = "v2/searches";
	
	/*----------------Entity---------------*/
	public final static String  ENTITY = "entity-web/rest/v1";
	
	/*----------------OMS---------------*/
	public final static String  OMS = "integration-oms/rest/v1/inbound/order/EC";
	
	/*--------------------Payment------------------*/
	public final static String  PAYMENT = "v2/payments";
	public final static String  REFUND_BACKEND = "trade-web/rest/v2/payment/refund";
	public final static String  REFUND = "v2/payment/refund";
}
