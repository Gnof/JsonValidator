package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import core.JsonValidator;

public class JsonValidatorTest {

	String basicValidation;
	String valueValidation;
	String multipleCompositeValidationFalse;
	String multipleCompositeValidationTrue;
	String hierarchyValidation;
	String ancestorValidationTrue;
	String ancestorValidationFalse;
	
	String basicValidationTarget;
	String multipleCompositeTarget;
	String hierarchyValidationTarget;
	
	@Before
	public void setup()
	{
		basicValidation = "{\"attribute\":\"foo\"}";
		valueValidation = "{\"attribute\":\"foo\", \"value\":\"blam\"}";
		multipleCompositeValidationFalse = "{\"attribute\":\"foo\",\"value\":\"bar\", \"composite\":[{\"attribute\":\"comp1\", \"value\":\"yes\"},{\"attribute\":\"comp2\", \"value\":\"no\"}]}";
		multipleCompositeValidationTrue = "{\"attribute\":\"foo\",\"value\":\"bat\", \"composite\":[{\"attribute\":\"comp1\", \"value\":\"yes\"},{\"attribute\":\"comp2\", \"value\":\"no\"}]}";
		hierarchyValidation = "{\"attribute\":\"foo\", \"value\":\"bat\", \"composite\":{\"attribute\":\"foo\", \"value\":\"bat\",\"hierarchy\":\"[[properties,wah]]\"}}";
		ancestorValidationFalse = "{\"attribute\":\"foo\", \"composite\":{\"attribute\":\"foo\", \"value\":\"bat\",\"ancestor\":\"[[properties,wah]]\"}}";
		ancestorValidationTrue = "{\"attribute\":\"foo\", \"composite\":{\"attribute\":\"foo\", \"value\":\"bat\",\"ancestor\":\"[[properties]]\"}}";
		
		basicValidationTarget = "{\"properties\":[{\"foo\":\"bar\",\"comp1\":\"yes\"},{\"comp2\":\"no\",\"foo\":\"bat\"},{\"wah\":{\"foo\":\"blam\"}}]}";
		multipleCompositeTarget = "{\"properties\":[{\"foo\":\"bar\",\"comp1\":\"yes\"},{\"comp2\":\"no\",\"foo\":\"bat\",\"comp1\":\"yes\"},{\"wah\":{\"foo\":\"blam\"}}]}";
		hierarchyValidationTarget = "{\"properties\":[{\"foo\":\"blam\",\"comp1\":\"yes\"},{\"comp2\":\"no\",\"foo\":\"bat\"},{\"wah\":{\"foo\":\"blam\"}}]}";
		
	}
	@Test
	public void testAttribute() {
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(basicValidation, basicValidationTarget);
		assertTrue(result);
	}
	
	@Test
	public void testAttributeValue()
	{
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(valueValidation, basicValidationTarget);
		assertTrue(result);
	}
	
	@Test
	public void testMultipleCompositeValue() {
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(multipleCompositeValidationTrue, multipleCompositeTarget);
		assertTrue(result);
	}
	
	@Test
	public void testNegativeMultipleCompositeValue() {
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(multipleCompositeValidationFalse, multipleCompositeTarget);
		assertFalse(result);
	}
	
	@Test
	public void testHierarchy() {
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(hierarchyValidation, basicValidationTarget);
		assertFalse(result);
	}
	
	@Test
	public void testAncestorsFalse() {
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(ancestorValidationFalse, basicValidationTarget);
		assertFalse(result);
	}
	
	@Test
	public void testAncestorsTrue() {
		JsonValidator validator = new JsonValidator();		
		boolean result = validator.validate(ancestorValidationTrue, basicValidationTarget);
		assertFalse(result);
	}

}
