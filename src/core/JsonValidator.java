package core;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import exceptions.InvalidValidationJson;

/**
 * This is a Utility library meant to make JSON operations, particularly
 * comparison of deserialized complex objects easier.
 * 
 * Doing a comparison in complex JSON objects has been tedious because it's
 * either too strict a comparison (straight string comparison) or not strict
 * enough (random false positives).
 * 
 * Here's an example of something we've run into for automated verification:
 * 
 * {value1: [{status:false},{cause:service}],
 * value2:[{status:true},{cause:device}]} {value1:
 * [{status:false},{cause:service}], value2:[{status:false},{cause:device}]}
 * {value1: [{status:false},{cause:service}],
 * value2:[{status:true},{cause:device}]}
 * 
 * 
 * The goal is to have as flexible a way to verify that this data is there
 * without having to completely reproducing the data in a verification input.
 * 
 * The input for the verifier, in order to provide flexibility and verbosity
 * needed to convey the verification constraints will be accepted as a JSON
 * string.
 * 
 * The following format will define what is needed: 
 * --- A JSON Object OR a JSON Array of objects that represents the attribute(s) you want to verify and how
 * you want them to be verified 
 * 
 * [ 
 * 		{
 * 			attribute:'name of attribute to verify',
 * 			value:(optional, default:none)'a (string representation) value or list of
 * 					values if multiples of this attribute are expected', 
 * 			**quantity:(optional, default: 1)'number of times we expect to see this attribute',
 * 			composite:(optional, default: none)'a list of composite identifiers to search for that are in the scope of this specific object',
 * 			hierarchy:(optional, default: none)'an expected comprehensive hierarchy that leads to the attribute, in order from the root', 
 * 			ancestor:(optional, default: none)'an expected ancestor belongs to this attribute'
 * 		} 
 * ]
 * 
 * -- **Since JSON keys within an object are meant to be unique, quantity
 * represents the number of attributes within a whole JSON. i.e. {mainAttribute:
 * [{minor-attibute: val1}, {minor-attribute: val2}, {minor-attribute: val3} }
 * 
 * for the following validation: 'attribute':'minor-attribute', quantity:3 in
 * this case.
 * 
 * 
 * Example (simple attribute + value, and attribute with complex value):
 * 
 * {foo: 'bar', foo2: { bar1: 'value1'} }
 * 
 * We can verify that the attribute 'foo' exists with:
 * 
 * {'attribute': 'foo'}
 * 
 * that 'foo' has value 'bar':
 * 
 * {'attribute': 'foo', value: 'bar'}
 * 
 * that 'foo' occurs once (the value attribute is optional here):
 * 
 * {'attribute': 'foo', quantity: 1, value: 'bar'}
 * 
 * that 'bar1' exists with the ancestor 'foo2':
 * 
 * {'attribute': 'bar1', 'ancestor':'foo2'}
 * 
 * and it's hierarchy is from foo2:
 * 
 * {'attribute': 'bar1', 'hierarchy':'foo2'}
 * 
 * 
 * @author kofong
 * 
 */
public class JsonValidator {

	/**
	 * result of the last validate run 
	 */
	private boolean lastResult;
	
	/**
	 * validation detail object of the last validation run
	 */
	private HashMap<JsonObject, JsonObject> validationDetail;
	
	/**
	 * 
	 * @return result of the last validation run 
	 */
	public boolean result() {
		return lastResult;
	}

	/**
	 * JsonObject Key is the Validation Run
	 * JsonObject Value is the result of the validation run
	 * @return HashMap of validation details of last validation run
	 */
	public HashMap<JsonObject, JsonObject> getLastValidationDetail() {
		return validationDetail;
	}

	/**
	 * Default Constructor
	 */
	public JsonValidator() {
		lastResult = false;
		validationDetail = new HashMap<JsonObject, JsonObject>();		
	}

	/**
	 * Validate call with string json representation
	 * 
	 * @param validateJsonString
	 *            - string rep of validation json
	 * @param toValidateJsonString
	 *            - string rep of to validate json
	 * @return - Validation pass / fail
	 */
	public boolean validate(String validateJsonString,
			String toValidateJsonString) {		
		JsonParser parser = new JsonParser();
		JsonElement validateJson = parser.parse(validateJsonString);
		JsonObject toValidate = (JsonObject) parser.parse(toValidateJsonString);		
		return validate(validateJson, toValidate);
	}

	/**
	 * Validates that a JSON is meeting the criteria you expect
	 * 
	 * @param validationJson
	 *            - definition of expected content as formatted in Javadoc
	 * @param toValidate
	 *            - the JSON object to validate
	 * @return - Validation pass / fail
	 */
	public boolean validate(JsonElement validationJson, JsonObject toValidate) {
		JsonArray validationJsons = new JsonArray();
		
		// Checking for single validation or a collection of them
		if (validationJson instanceof JsonArray)
		{
			validationJsons = (JsonArray) validationJson;
		}
		else if (validationJson instanceof JsonObject)
		{
			validationJsons.add(validationJson);
		}
		else
		{
			System.out.println("Invalid Json Element Subclass: " + validationJson.getClass());
			return false;
		}
		
		boolean overallPass = true;
		
		for(JsonElement validation : validationJsons)
		{
			try {
				JsonObject currValidation = (JsonObject) validation;
				// create worker and the validation unit
				ValidationWorker vw = new ValidationWorker();
				ValidationUnit validationUnit = new ValidationUnit(currValidation);
				// retrieve results
				JsonObject result = vw.validate(validationUnit, toValidate);
				boolean validationResult = vw.isOverallPass();
				lastResult = validationResult;
				// instantiate objects to store
				JsonObject resultObject = new JsonObject();
				resultObject.addProperty("validation-result", validationResult);
				resultObject.add("validation-details", result);
				// add to results detail hash
				validationDetail.put(currValidation, resultObject);
				// tally up overall pass / fail
				overallPass = overallPass && validationResult;
			} catch (InvalidValidationJson e) {
				System.out.println("validation Json has an invalid format");
				e.printStackTrace();
				overallPass = false;
			}
		}
		
		return overallPass;
	}
}
