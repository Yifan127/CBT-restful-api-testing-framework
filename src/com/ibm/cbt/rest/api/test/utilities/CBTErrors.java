package com.ibm.cbt.rest.api.test.utilities;

public class CBTErrors {
	
	//ERROR CODE
	public final static String CODE_FIELD_VALIDATION = "99010";
	public final static String CODE_INVALID_CREDENTIALS = "99011";
	public final static String CODE_AUTHORIZATION = "98401";
	public final static String CODE_TOKEN_NOT_MATCH = "98403";
	public final static String CODE_TOKEN_ORG_WRONG = "3031";
	public final static String CODE_INTERNAL_EXCEPTION = "1500";		

	//Passport
	public final static String CODE_WRONG_PASSWORD = "16010";
	public final static String CODE_INVALID_USERNAME = "16126";
	public final static String CODE_RESET_PASSWORD_WRONG_PASSWORD = "16015";
	//Cart
	public final static String CODE_INVALID_QUANTITY = "6000";
	public final static String CODE_NO_ADDRESS = "6017";
	public final static String CODE_SEARCH_INVALID_ITEMID = "12005";
	public final static String CODE_CART_NO_INVENTORY = "18001";
	public final static String CODE_CART_INVALID_CARTLINEID = "18002";
	public final static String CODE_CART_INVALID_CARRIERID = "14013";
	public final static String CODE_SHIP_INVALID_JURISDICTION_CODE = "14001";
	//Order
	public final static String CODE_ORDER_INVALID = "17001";
	public final static String CODE_ORDER_CART_INVALID = "99041";
	public final static String CODE_ORDER_CANNOT_CANCEL = "8009";
	//Address
	public final static String CODE_ADDRESS_NOT_EXIST = "2404";
	public final static String CODE_EXCEED_MAX_ADDRESS = "2107";
	//Product
	public final static String CODE_PRODUCT_INVALID_ITEMID = "3200";  
	public final static String CODE_PRODUCT_MAX_IDS = "3299"; 
	//Payment
	public final static String CODE_PAYMENT_INVALID_ORDERID = "9004"; 
	public final static String CODE_PAYMENT_WRONGTYPE_AMOUNT = "9105"; 
	public final static String CODE_PAYMENT_DUPLICATED_TRADENO = "9110";
	public final static String CODE_PAYMENT_INVALID_AMOUNT = "9111"; 
	public final static String CODE_PAYMENT_INVALID_STATUS = "9112";
	public final static String CODE_PAYMENT_INVALID_PAYMETHOD = "9113"; 
	public final static String CODE_REFUND_INVALID_AMOUNT = "9114"; 	
	public final static String CODE_PAYMENT_MT = "9203";
	
	//COMMON
	public final static String REQUIRED_FIELD = "此项为必填";	
	public final static String INTERNAL_EXCEPTION = "内部服务异常";
	public final static String SYSTEM_EXCEPTION = "系统异常，请稍后重试";
	public final static String SYSTEM_NO_RESPONSE = "系统无响应:";	
	
	//PASSPORT
	public final static String NEED_LOGIN = "需要登录";
	public final static String TOKEN_AUTHORIZATION = "非法令牌，无权操作该接口！";	
	public final static String TOKEN_NOT_MATCH = "当前用户令牌和租户令牌不匹配，无权操作该接口！";
	public final static String TOKEN_ORG_WRONG = "请求无权限访问此资源，组织标识token错误";
	public final static String INVALID_CREDENTIALS = "The client credentials are invalid";
	public final static String INVALID_GRANTTYPE = "Grant type \"client_credential\" not supported";
	public final static String NULL_GRANTTYPE = "The grant type was not specified in the request";
	public final static String NULL_CLIENTID = "Client credentials were not found in the headers or body";
	public final static String NULL_CLIENTSECRET = "client credentials are required";
	
	//USER
	public final static String USER_NAME_ALREADY_USED = "该用户名已经被注册";
	public final static String USER_NAME_MIN = "用户名应该包含至少6个字符。";
	public final static String USER_NAME_MAX = "用户名只能包含至多20个字符。";
	public final static String USER_PASSWORD_MIN = "密码应该包含至少6个字符。";
	public final static String USER_PASSWORD_MAX = "密码只能包含至多20个字符。";
	public final static String USER_UNMATCH_PASSWORD = "两次密码不一致";
	public final static String USER_WRONG_PASSWORD = "用户名密码错误";
	public final static String USER_WRONG_USERNAME = "该用户名未注册";
	public final static String USER_INVALID_USERNAME = "非法用户名格式,用户名只能是字符串,邮箱,手机号";
	public final static String USER_REQUIRED_USERNAME = "用户名不能为空。";
	public final static String USER_REQUIRED_PASSWORD = "密码不能为空。";
	public final static String USER_REQUIRED_CONFIRMPASSWORD = "确认密码不能为空。";
	public final static String USER_RESET_PASSWORD = "修改密码成功";
	public final static String USER_RESET_PASSWORD_WRONG_PASSWORD = "旧密码错误,修改密码失败";
	public final static String USER_RESET_PASSWORD_REQUIRED_PASSWORD = "旧密码不能为空。";
	public final static String USER_RESET_PASSWORD_REQUIRED_NEWPASSWORD = "新密码不能为空。";
	public final static String USER_RESET_PASSWORD_REQUIRED_CONFIRMPASSWORD = "确认密码不能为空。";
	
	
	//ADDRESS
	public final static String ADDRESS_EXCEED_MAX = "用户的地址不能超过10条";
	public final static String ADDRESS_INVALID_POSTCODE = "邮编：必须是整数。";
	public final static String ADDRESS_INVALID_RECEIVERMOBILE = "手机：必须是整数。";
	public final static String ADDRESS_ADDRESS = "只能是中文、字母、数字和-_,且长度不能小于10,不能大于200";
	public final static String ADDRESS_RECEIVERNAME = "只能是中文、字母、数字和-_,且长度不能小于2,不能大于15";
	public final static String ADDRESS_VALIDATION_POSTCODE = "请输入正确的邮编（6位数字）";
	public final static String ADDRESS_VALIDATION_RECEIVERMOBILE = "手机格式错误";
	public final static String ADDRESS_VALIDATION_RECEIVERPHONE = "座机格式错误，请输入真实的电话信息";
	public final static String ADDRESS_NOT_EXIST = "该条记录不存在或已经被删除";
	
	//CART
	public final static String CART_INVALID_QUANTITY = "数量必须为整数且大于0";
	public final static String CART_INVALID_ITEMID = "产品不存在，产品ID：";
	public final static String CART_CALCULATION_NO_ADDRESS = "计算运费必须提供地址";
	public final static String CART_CALCULATION_NO_STATECODE = "state_code为必填字段";
	public final static String CART_NO_INVENTORY = "该商品库存不够";
	public final static String CART_INVALID_CARTLINEID = "该购物车并不存在，请确认后重试";
	public final static String CART_INVALID_CARRIERID = "快递运行商不存在";
	
	//SHIP
	public final static String SHIP_INVALID_JURISDICTION_CODE = "输入区域代码有误, 找不到相应区域.";
	
	//ORDER
	public final static String ORDER_ORDERID_INVALID = "订单号为空或格式错误";
	public final static String ORDER_NOT_BUYABLE = "该商品已下架";
	public final static String ORDER_CART_INVALID = "购物车不存在，或者已经被结算！";
	public final static String ORDER_CANNOT_CANCEL = "订单已发货，不可取消";
	
	//PRODUCT
	public final static String PRODUCT_INVALID_ITEMID = "查询商品不存在，请核对后再试";
	public final static String PRODUCT_MAX_IDS = "一次性查询id数目不得超过50个";
	
	//PAYMENT
	public final static String PAYMENT_DUPLICATED_TRADENO = "交易编码已存在";
	public final static String PAYMENT_INVALID_ORDERID = "订单编号不存在";
	public final static String PAYMENT_INVALID_STATUS = "状态值必须为SUCCESS, FAILED, CANCELED, 或者PREPARED"; 
	public final static String PAYMENT_INVALID_PAYMETHOD = "支付方式值非法"; 
	public final static String PAYMENT_INVALID_AMOUNT = "金额不能为0或负值"; 
	public final static String PAYMENT_WRONGTYPE_AMOUNT = "金额必须为数字";
	public final static String REFUND_INVALID_AMOUNT = "金额不得小于0";
	
	
}
