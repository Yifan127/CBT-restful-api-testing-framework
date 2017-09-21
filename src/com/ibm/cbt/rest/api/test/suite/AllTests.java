package com.ibm.cbt.rest.api.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.ibm.cbt.rest.api.test.address.AddressTest;
import com.ibm.cbt.rest.api.test.address.JurisdictionsTest;
import com.ibm.cbt.rest.api.test.address.ShipmentTest;
import com.ibm.cbt.rest.api.test.cart.CartTest;
import com.ibm.cbt.rest.api.test.order.OrderListTest;
import com.ibm.cbt.rest.api.test.order.OrderTest;
import com.ibm.cbt.rest.api.test.passport.LoginTest;
import com.ibm.cbt.rest.api.test.passport.OauthTest;
import com.ibm.cbt.rest.api.test.passport.RegisterTest;
import com.ibm.cbt.rest.api.test.payment.PaymentTest;
import com.ibm.cbt.rest.api.test.payment.RefundTest;
import com.ibm.cbt.rest.api.test.product.ProductTest;
import com.ibm.cbt.rest.api.test.search.SearchTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	OauthTest.class,
	RegisterTest.class, 
	LoginTest.class,
	AddressTest.class,
	JurisdictionsTest.class,
	ShipmentTest.class,
	ProductTest.class,
	SearchTest.class,
	CartTest.class,
	OrderTest.class,
	OrderListTest.class,
	PaymentTest.class,
	RefundTest.class
	})
public class AllTests {

}
