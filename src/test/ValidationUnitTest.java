package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.*;

import core.ValidationUnit;
import exceptions.InvalidValidationJson;

public class ValidationUnitTest {

	JsonObject invalidValidationJson;
	JsonObject validationJson;
	JsonObject validationWithQuantity;
	JsonObject validationWithInvalidQuantity;
	JsonObject validationWithValue;
	JsonObject validationWithValueList;	
	JsonObject validationWithComposite;
	JsonObject validationWithAncestor;
	JsonObject validationWithHierarchy;

	@Before
	public void setUp() throws Exception {
		invalidValidationJson = new JsonObject();
		
		validationJson = new JsonObject();
		validationJson.addProperty("attribute", "foo");

		validationWithQuantity = new JsonObject();
		validationWithQuantity.addProperty("attribute", "foo");
		validationWithQuantity.addProperty("quantity", 2);
		
		validationWithInvalidQuantity = new JsonObject();
		validationWithInvalidQuantity.addProperty("attribute", "foo");
		validationWithInvalidQuantity.addProperty("quantity", "bar");
	
		validationWithValue = new JsonObject();
		validationWithValue.addProperty("attribute", "foo");
		validationWithValue.addProperty("value", "hello");
		
		validationWithValueList = new JsonObject();
		validationWithValueList.addProperty("attribute", "foo");
		validationWithValueList.addProperty("value", "[hello, goodbye]");		
		
		validationWithComposite = new JsonObject();
		validationWithComposite.addProperty("attribute", "foo");
		validationWithComposite.addProperty("quantity", 1);
		validationWithComposite.add("composite", validationJson);

		validationWithAncestor = new JsonObject();
		validationWithAncestor.addProperty("attribute", "foo");
		validationWithAncestor.addProperty("quantity", 3);		
		validationWithAncestor.addProperty("ancestor", "foo-an");
		
		validationWithHierarchy = new JsonObject();
		validationWithHierarchy.addProperty("attribute", "foo");
		validationWithHierarchy.addProperty("quantity", "2");
		validationWithHierarchy.addProperty("ancestor", "[old1, old4]");
		validationWithHierarchy.addProperty("hierarchy", "[node1, node2, node3]");
		validationWithHierarchy.addProperty("value", "hello");
	}

	@Test
	public void testInvalidValidationJson() {
		ValidationUnit vu = null;
		try
		{
			vu = new ValidationUnit(invalidValidationJson);			
		} 
		catch (InvalidValidationJson e)
		{			
			String error = "exceptions.InvalidValidationJson: 'attribute' missing from Validation JSON";
			assertEquals(error, e.toString());
		}
		assertNull(vu);		
	}
	
	@Test
	public void testConstructor() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationJson);
		assertEquals("foo", vu.getAttribute());
		// quantity not set defaults to -1
		assertEquals(-1, vu.getQuantity());
		assertNull(vu.getValue());
		assertFalse(vu.hasValue());
	}

	@Test
	public void testConstructorWithQuantity() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationWithQuantity);
		assertEquals("foo", vu.getAttribute());
		assertEquals(2, vu.getQuantity());
		assertNull(vu.getValue());
	}
	
	@Test
	public void testConstructorWithInvalidQuantity() throws InvalidValidationJson {
					
		ValidationUnit vu = new ValidationUnit(validationWithInvalidQuantity);
		//invalid quantity defaults to 1
		assertEquals(1, vu.getQuantity());
			
	}
	
	@Test
	public void testConstructorWithValue() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationWithValue);
		assertEquals("foo", vu.getAttribute());
		assertEquals("hello", vu.getValue());
	}
	
	@Test
	public void testConstructorWithValueList() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationWithValueList);
		assertEquals("foo", vu.getAttribute());
		@SuppressWarnings("unchecked")
		ArrayList<String> valuelist = (ArrayList<String>)vu.getValue();
		assertEquals("hello", valuelist.get(0));
		assertEquals("goodbye", valuelist.get(1));
	}

	@Test
	public void testConstructorWithComposite() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationWithComposite);
		assertEquals("foo", vu.getAttribute());
		JsonObject compJson = (JsonObject)vu.getComposite();
		ValidationUnit compVu = new ValidationUnit(compJson);
		assertNotNull(compVu);
		assertEquals("foo", compVu.getAttribute());
	}
	
	@Test
	public void testConstructorWithAncestor() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationWithAncestor);
		assertEquals("foo", vu.getAttribute());		
		assertEquals("foo-an", vu.getAncestor());
	}
	
	@Test
	public void testConstructorWithHierarchy() throws InvalidValidationJson {
		ValidationUnit vu = new ValidationUnit(validationWithHierarchy);
		assertEquals("foo", vu.getAttribute());
		@SuppressWarnings("unchecked")
		ArrayList<String> ancestorList = (ArrayList<String>)vu.getAncestor();
		assertEquals("old1", ancestorList.get(0));
		
	}

}
