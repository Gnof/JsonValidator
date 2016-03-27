package com.gnof.test;


import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.gnof.core.JsonValidator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.google.gson.JsonObject;

public class BugFixTest {

	// ---- Bug #1 filed by Kamran:
	/**
	 * {
		"version": "4.2",
		"dictionary": {
			"products": [{
				"id": "xyz",
				"features": ["feature1.xyz",
				"feature2.xyz"],
				"name": {
					"en": "test"
				},
				"description": {
					"en": "test product description"
				},
				"grace_period": 30,
				"admin_block_period": 30,
				"user_block_period": 30
			}]
		}
		}
		
		// validation:
		 * {attribute:"en", value:"test product description"}
		 * {attribute:"features", value:["feature1.xyz"]}
	 */
	String kamranBug1Json;
	String kamranBug1Validation1;
	String kamranBug1Validation2;		
		
	@Before
	public void setUp() throws Exception {
		kamranBug1Json = "{\"version\": \"4.2\",\"dictionary\": {\"products\": [{\"id\": \"xyz\",\"features\": [\"feature1.xyz\",\"feature2.xyz\"],\"name\": {\"en\": \"test\"},\"description\": {\"en\": \"test product description\"},\"grace_period\": 30,\"admin_block_period\": 30,\"user_block_period\": 30}]}}";
		kamranBug1Validation1 = "{attribute:\"en\", value:\"test product description\"}";
		kamranBug1Validation2 = "{attribute:\"features\", value:[\"feature1.xyz\"]}";
	}
	
	@Test
	public void testBug1() {
		JsonValidator jv = new JsonValidator();
		boolean result = jv.validate(kamranBug1Validation1, kamranBug1Json);
		assertTrue(result);
		boolean secondResult = jv.validate(kamranBug1Validation2, kamranBug1Json);
		assertTrue(secondResult);
	}

	@After
	public void tearDown() throws Exception {
	}

}
