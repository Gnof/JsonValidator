package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import core.ValidationUnit;
import core.ValidationWorker;

public class ValidationWorkerTest {

	ValidationUnit baseValidation;
	ValidationUnit falseValidation;
	ValidationUnit oneLevelValidation;
	ValidationUnit multipleValidation;
	ValidationUnit hierarchyValidation;
	ValidationUnit ancestorValidation;
	ValidationUnit ancestor2Validation;
	ValidationUnit compositeValidation;
	ValidationUnit carlosValidation;

	JsonObject baseToValidate;
	JsonObject oneLevelToValidate;
	JsonObject multipleBaseToValidate;
	JsonObject hierarchyToValidate;
	JsonObject ancestorToValidate;
	JsonObject ancestor2ToValidate;
	JsonObject compositeToValidate;
	JsonObject carlosScenario;

	@Before
	public void setUp() throws Exception {
		// ---- Base instantiation
		JsonObject validationJson = new JsonObject();
		validationJson.addProperty("attribute", "foo");
		validationJson.addProperty("quantity", 1);
		baseValidation = new ValidationUnit(validationJson);

		baseToValidate = new JsonObject();
		baseToValidate.addProperty("foo", "bar");

		// ---- False instantiation
		JsonObject falseJson = new JsonObject();
		falseJson.addProperty("attribute", "bar");
		falseJson.addProperty("quantity", 1);
		falseValidation = new ValidationUnit(falseJson);

		// ---- One level in instantiation
		JsonObject oneLevelValidationJson = new JsonObject();
		oneLevelValidationJson.addProperty("attribute", "foo");
		oneLevelValidationJson.addProperty("quantity", "1");
		oneLevelValidation = new ValidationUnit(oneLevelValidationJson);

		oneLevelToValidate = new JsonObject();
		JsonObject oneJson = new JsonObject();
		oneJson.addProperty("foo", "bar");
		oneLevelToValidate.add("main", oneJson);

		// ---- Multiple instantiation
		JsonObject multiJson = new JsonObject();
		multiJson.addProperty("attribute", "foo");
		multiJson.addProperty("value", "[bar, bat]");
		multiJson.addProperty("quantity", 2);
		multipleValidation = new ValidationUnit(multiJson);

		multipleBaseToValidate = new JsonObject();
		JsonObject multi1 = new JsonObject();
		JsonObject multi2 = new JsonObject();
		multi1.addProperty("foo", "bar");
		multi2.addProperty("foo", "bat");
		JsonArray jsonlist = new JsonArray();
		jsonlist.add(multi1);
		jsonlist.add(multi2);
		multipleBaseToValidate.add("properties", jsonlist);

		// ---- Hierarchy instantiation
		JsonObject hierarchyJson = new JsonObject();
		hierarchyJson.addProperty("attribute", "foo");
		hierarchyJson.addProperty("quantity", 3);
		hierarchyJson.addProperty("hierarchy", "[[\"properties\"]]");
		hierarchyValidation = new ValidationUnit(hierarchyJson);

		hierarchyToValidate = new JsonObject();
		JsonObject hr1 = new JsonObject();
		JsonObject hr2 = new JsonObject();
		JsonObject hr3 = new JsonObject();
		hr1.addProperty("foo", "bar");
		hr2.addProperty("foo", "bat");
		JsonObject hr4 = new JsonObject();
		hr4.addProperty("foo", "blam");
		hr3.add("wah", hr4);
		JsonArray hrlist = new JsonArray();
		hrlist.add(hr1);
		hrlist.add(hr2);
		hrlist.add(hr3);
		hierarchyToValidate.add("properties", hrlist);

		// ---- Ancestor instantiation
		JsonObject ancestorJson = new JsonObject();
		ancestorJson.addProperty("attribute", "foo");
		ancestorJson.addProperty("quantity", 3);
		ancestorJson.addProperty("hierarchy", "[[\"properties\"]]");
		ancestorJson.addProperty("ancestor", "[\"properties\", \"wah\"]");
		ancestorValidation = new ValidationUnit(ancestorJson);

		ancestorToValidate = new JsonObject();
		JsonObject as1 = new JsonObject();
		JsonObject as2 = new JsonObject();
		JsonObject as3 = new JsonObject();
		as1.addProperty("foo", "bar");		
		as2.addProperty("foo", "bat");
		JsonObject as4 = new JsonObject();
		as4.addProperty("foo", "blam");
		as3.add("wah", as4);
		JsonArray aslist = new JsonArray();
		aslist.add(as1);
		aslist.add(as2);
		aslist.add(as3);
		ancestorToValidate.add("properties", aslist);
		
		// -- ancestor 2 instantiation
		JsonObject ancestor2Json = new JsonObject();
		ancestor2Json.addProperty("attribute", "foo");
		ancestor2Json.addProperty("value", "blam");
		ancestor2Json.addProperty("quantity", 3);
		ancestor2Json.addProperty("hierarchy", "[[\"properties\", \"wah\"]]");
		ancestor2Json.addProperty("ancestor", "[\"properties\", wah]");
		ancestor2Validation = new ValidationUnit(ancestor2Json);

		ancestor2ToValidate = new JsonObject();
		JsonObject ances1 = new JsonObject();
		JsonObject ances2 = new JsonObject();
		JsonObject ances3 = new JsonObject();
		ances1.addProperty("foo", "bar");		
		ances2.addProperty("foo", "bat");
		JsonObject ances4 = new JsonObject();
		ances4.addProperty("foo", "blam");
		ances3.add("wah", ances4);
		JsonArray anceslist = new JsonArray();
		anceslist.add(ances1);
		anceslist.add(ances2);
		anceslist.add(ances3);
		ancestor2ToValidate.add("properties", anceslist);

		// ---- Composite instantiation
		JsonObject compositeJson = new JsonObject();
		compositeJson.addProperty("attribute", "foo");
		compositeJson.addProperty("value", "bat");
		String compositeString = "{\"attribute\":\"comp2\", \"value\":\"no\"}";
		compositeJson.addProperty("composite", compositeString);
		compositeValidation = new ValidationUnit(compositeJson);

		compositeToValidate = new JsonObject();
		JsonObject f1 = new JsonObject();
		JsonObject f2 = new JsonObject();
		JsonObject f3 = new JsonObject();
		f1.addProperty("foo", "bar");
		f1.addProperty("comp1", "yes");
		f2.addProperty("comp2", "no");
		f2.addProperty("foo", "bat");
		JsonObject f4 = new JsonObject();
		f4.addProperty("foo", "blam");
		f3.add("wah", f4);
		JsonArray flist = new JsonArray();
		flist.add(f1);
		flist.add(f2);
		flist.add(f3);
		compositeToValidate.add("properties", flist);

		// ---- Carlos' example, to show him that the example he
		// gave me works in this utility
		JsonObject carlosJson = new JsonObject();
		carlosJson.addProperty("attribute", "status");		
		carlosJson.addProperty("quantity", 2);
		carlosJson.addProperty("hierarchy", "[[\"value1\"], [\"value2\"]]");
		carlosJson.addProperty("value", "[\"false\", \"true\"]");

		carlosValidation = new ValidationUnit(carlosJson);

		String carlosJsonString = "{value1: [{status:false},{cause:service}], value2:[{status:true},{cause:device}]}";
		JsonParser parser = new JsonParser();
		carlosScenario = (JsonObject) parser.parse(carlosJsonString);

	}

	// TODO: Update these unit tests with validations as they come

	@Test
	public void testBaseValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(baseValidation, baseToValidate);
		assertEquals(true, validResult.get(ValidationWorker.HAS_ATTRIBUTE)
				.getAsBoolean());
	}

	@Test
	public void testFalseValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(falseValidation, baseToValidate);
		assertNotEquals(true, validResult.get(ValidationWorker.HAS_ATTRIBUTE)
				.getAsBoolean());
	}

	@Test
	public void testOneLevelValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(oneLevelValidation,
				oneLevelToValidate);
		assertEquals(true, validResult.get(ValidationWorker.HAS_ATTRIBUTE)
				.getAsBoolean());
	}

	@Test
	public void testMultipleBaseValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(multipleValidation,
				multipleBaseToValidate);
		assertEquals(true, validResult.get(ValidationWorker.HAS_ATTRIBUTE)
				.getAsBoolean());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_VALUE)
				.getAsBoolean());

	}

	@Test
	public void testQuantityBaseValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(multipleValidation,
				multipleBaseToValidate);
		assertEquals(true, validResult.get(ValidationWorker.MATCH_QUANTITY)
				.getAsBoolean());
		assertEquals(2, validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt());
	}

	@Test
	public void testHierarchyValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(hierarchyValidation,
				hierarchyToValidate);
		assertEquals(true, validResult.get(ValidationWorker.MATCH_HIERARCHY)
				.getAsBoolean());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_QUANTITY)
				.getAsBoolean());
		assertEquals(3, validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt());
		// this check is to verify that the quantity found matches the number of
		// paths found
		System.out.println(validResult);
		assertEquals(validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt(), validResult.get(ValidationWorker.HIERARCHY_FOUND)
				.getAsJsonArray().size());
	}

	@Test
	public void testAncestorValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(ancestorValidation,
				ancestorToValidate);		
		assertEquals(true, validResult.get(ValidationWorker.MATCH_HIERARCHY)
				.getAsBoolean());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_QUANTITY)
				.getAsBoolean());
		assertEquals(3, validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_ANCESTOR)
				.getAsBoolean());
		// this check is to verify that the quantity found matches the number of
		// paths found
		assertEquals(validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt(), validResult.get(ValidationWorker.HIERARCHY_FOUND)
				.getAsJsonArray().size());
	}
	
	@Test
	public void testAncestorComplexValidation()
	{
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(ancestor2Validation,
				ancestor2ToValidate);
		assertEquals(true, validResult.get(ValidationWorker.MATCH_HIERARCHY)
				.getAsBoolean());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_QUANTITY)
				.getAsBoolean());
		assertEquals(3, validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_ANCESTOR)
				.getAsBoolean());		
	}

	@Test
	public void testCompositeBaseValidation() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(compositeValidation, compositeToValidate);		
		System.out.println(compositeToValidate);
		System.out.println(validResult);
	}

	@Test
	public void testCarlosScenario() {
		ValidationWorker vw = new ValidationWorker();
		JsonObject validResult = vw.validate(carlosValidation, carlosScenario);
		assertEquals(true, validResult.get(ValidationWorker.MATCH_HIERARCHY)
				.getAsBoolean());
		assertEquals(true, validResult.get(ValidationWorker.MATCH_QUANTITY)
				.getAsBoolean());
		assertEquals(2, validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt());
		assertEquals(validResult.get(ValidationWorker.QUANTITY_FOUND)
				.getAsInt(), validResult.get(ValidationWorker.HIERARCHY_FOUND)
				.getAsJsonArray().size());
	}
}
