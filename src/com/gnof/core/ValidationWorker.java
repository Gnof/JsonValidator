package com.gnof.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.gnof.exceptions.InvalidValidationJson;
import com.google.gson.*;

/**
 * Performs validation work given a ValidationUnit object as criteria and a JSON
 * object to validate
 * 
 * The validation worker will return a result JSON with the following
 * properties:
 * 
 * 
 * (bool) has-attribute: whether or not the attribute being searched for is in
 * the JSON
 * 
 * ----------------- The following only show up if applicable "none" is output
 * if there is nothing found -----------------
 * 
 * (bool) match-value: whether or not the attribute's value(s) match
 * 
 * (string / list) value-found: the value found
 * 
 * (bool) match-quantity: whether or not the attribute count matches
 * 
 * (int) quantity-found: the number of attributes found
 * 
 * (bool) match-composite: whether or not the composite validation was found
 * 
 * (json) composite-result: the result of the composite search
 * 
 * (bool) match-hierarchy: whether or not the hierarchy was found
 * 
 * (string / list) hierarchy-found: the path that was found to the attribute,
 * blank if attribute wasn't found
 * 
 * (bool) match-ancestor: whether or not the ancestor(s) were found
 * 
 * (string / list) ancestors-found: ancestors found
 * 
 * @author kofong
 * 
 */
public class ValidationWorker {

	/**
	 * Result property attribute names
	 */
	public static final String HAS_ATTRIBUTE = "has-attribute";
	public static final String MATCH_QUANTITY = "match-quantity";
	public static final String QUANTITY_FOUND = "quantity-found";
	public static final String MATCH_VALUE = "match-value";
	public static final String VALUE_FOUND = "value-found";
	public static final String MATCH_COMPOSITE = "match-composite";
	public static final String COMPOSITE_RESULT = "composite-result";
	public static final String MATCH_HIERARCHY = "match-hierarchy";
	public static final String HIERARCHY_FOUND = "hierarchy-found";
	public static final String MATCH_ANCESTOR = "match-ancestor";
	public static final String ANCESTOR_FOUND = "ancestor-found";

	/**
	 * null if validate has not been run, JsonObject with result details
	 * otherwise
	 */
	private JsonObject resultData;
	/**
	 * defaults false before validate has been run.
	 */
	private boolean overallPass;

	/**
	 * Default constructor
	 */
	public ValidationWorker() {
		resultData = null;
		overallPass = false;
	}

	/**
	 * Validates a JsonObject based on the validation criteria passed in
	 * 
	 * @param vu
	 *            - the validation unit specifying the criteria of the json
	 *            expected
	 * @param toValidate
	 *            - the target json to be validated
	 * @return = a JSON object with validation results
	 */
	public JsonObject validate(ValidationUnit vu, JsonObject toValidate) {
		JsonObject resultData = new JsonObject();

		// the main attribute to look for, not optional
		String attribute = vu.getAttribute();
		// the value we're looking for, optional
		Object value;

		// Processing the JSON for some data we'll use shortly
		HashMap<String, JsonObject> objectData = gatherObjectData(toValidate);

		// We do this explicitly because all other validations pretty much
		// rely on this
		boolean hasAttribute = objectData.containsKey(attribute);
		resultData.addProperty(HAS_ATTRIBUTE, hasAttribute);

		JsonObject attributeData = objectData.get(attribute);
		// We'll only instantiate this if we need it
		ArrayList<ArrayList<String>> paths = null;

		// if quantity attribute exists, we validate
		if (vu.hasQuantity()) {
			if (!hasAttribute) {
				resultData.addProperty(MATCH_QUANTITY, false);
				resultData.addProperty(QUANTITY_FOUND, 0);
			} else {
				int attributeCount = attributeData.get("count").getAsInt();
				resultData.addProperty(MATCH_QUANTITY,
						attributeCount == vu.getQuantity());
				resultData.addProperty(QUANTITY_FOUND, attributeCount);
			}
		}

		// if the value attribute exists, we need to perform this validation
		if (vu.hasValue()) {
			if (!hasAttribute) {
				resultData.addProperty(MATCH_VALUE, false);
				resultData.addProperty(VALUE_FOUND, "none");
			} else {				
				JsonArray attributeValues = attributeData.get("values")
						.getAsJsonArray();
				boolean valuesVerification = valueVerificationHelper(vu,
						attributeValues);
				resultData.addProperty(MATCH_VALUE, valuesVerification);
				resultData.add(VALUE_FOUND, attributeValues);
			}
		}

		// validate composite identifier if necessary
		if (vu.hasComposite()) {
			if (!hasAttribute) {
				resultData.addProperty(MATCH_COMPOSITE, false);
				resultData.addProperty(COMPOSITE_RESULT, "none");
			} else {
				compositeVerificationHelper(vu, toValidate, resultData);
			}
		}

		// validate ancestors if necessary
		if (vu.hasAncestor()) {
			if (!hasAttribute) {
				resultData.addProperty(MATCH_ANCESTOR, false);
				resultData.addProperty(ANCESTOR_FOUND, "none");
			} else {
				if (paths == null) {
					value = vu.getValue();
					paths = findAttribute(toValidate, attribute, value);
				}

				JsonObject ancestorResult = ancestorVerificationHelper(vu,
						paths, toValidate);
				boolean ancestorVerification = ancestorResult.get("result")
						.getAsBoolean();
				String ancestors = ancestorResult.get("ancestors")
						.getAsString();
				resultData.addProperty(MATCH_ANCESTOR, ancestorVerification);
				resultData.addProperty(ANCESTOR_FOUND, ancestors);
			}
		}

		// validate hierarchy if necessary
		if (vu.hasHierarchy()) {
			if (!hasAttribute) {
				resultData.addProperty(MATCH_HIERARCHY, false);
				resultData.addProperty(HIERARCHY_FOUND, "none");
			} else {				
				if (paths == null) {
					value = vu.getValue();
					paths = findAttribute(toValidate, attribute, value);
				}

				boolean hierarchyVerification = hierarchyVerificationHelper(vu,
						paths, toValidate);
				Gson gson = new Gson();
				JsonElement pathElement = gson.toJsonTree(paths);
				resultData.addProperty(MATCH_HIERARCHY, hierarchyVerification);
				resultData.add(HIERARCHY_FOUND, pathElement);
			}

		}

		this.resultData = resultData;
		updateOverall();
		return resultData;
	}

	/**
	 * 
	 * @return detailed result data of validation
	 */
	public JsonObject getResultData() {
		return resultData;
	}

	/**
	 * 
	 * @return whether the validation passed overall, false if validate not run
	 *         && resultdata == null
	 */
	public boolean isOverallPass() {
		return overallPass;
	}

	/**
	 * Internal method to update overall result when validate is run for the
	 * first time right now, it checks the results keys prepended with 'match'
	 */
	private void updateOverall() {
		if (resultData != null) {
			Set<Entry<String, JsonElement>> results = resultData.entrySet();
			boolean runningResult = true;
			for (Entry<String, JsonElement> result : results) {
				String currResult = result.getKey();
				if (currResult.startsWith("match-")) {
					boolean testResult = result.getValue().getAsBoolean();
					runningResult = runningResult && testResult;
				}
			}
			this.overallPass = runningResult;
		} else {
			System.out
					.println("Result Data is null, overall pass remains false");
		}
	}

	/**
	 * Value Verification helper method
	 * 
	 * @param vu
	 *            - Verification Unit being used by verify function
	 * @param attributeValues
	 *            - object containing the attributeData's values
	 * @param resultData
	 *            - the reporting result data
	 * @return whether the value verification passed
	 */
	@SuppressWarnings("unchecked")
	private boolean valueVerificationHelper(ValidationUnit vu,
			JsonArray attributeValues) {
		Object validationValues = vu.getValue();

		ArrayList<String> valuesToValidate = new ArrayList<String>();
		if (validationValues instanceof ArrayList) {
			valuesToValidate = new ArrayList<String>((ArrayList<String>)validationValues);
		} else {
			valuesToValidate.add((String) validationValues);
		}

		for (int i = 0; i < attributeValues.size(); i++) {
			String currValue = attributeValues.get(i).getAsString();
			if (valuesToValidate.contains(currValue)) {
				valuesToValidate.remove(currValue);
			}
		}
		// If valuesToValidate is empty, then we've seen all the values we were
		// expecting
		return valuesToValidate.isEmpty();
	}

	/**
	 * Hierarchy verification helper method
	 * 
	 * @param vu
	 *            - Validation unit criteria
	 * @param toValidate
	 *            - the json to validate
	 * @param paths
	 *            - the paths that have been gathered to the attribute specified
	 * @return whether the hierarchy verification passed
	 */
	@SuppressWarnings("unchecked")
	private boolean hierarchyVerificationHelper(ValidationUnit vu,
			ArrayList<ArrayList<String>> paths, JsonObject toValidate) {
		Object hierarchyValues = vu.getHierarchy();
		ArrayList<ArrayList<String>> pathsToValidate = new ArrayList<ArrayList<String>>((ArrayList<ArrayList<String>>)hierarchyValues);

		for (ArrayList<String> path : paths) {
			if (pathsToValidate.contains(path)) {
				pathsToValidate.remove(path);
			}
		}

		return pathsToValidate.size() == 0;
	}

	/**
	 * Helper method to perform ancestor verification
	 * 
	 * @param vu
	 *            - Validation unit criteria
	 * @param toValidate
	 *            - the json to validate
	 * @param paths
	 *            - the paths that have been gathered to the attribute specified
	 * @return JsonObject of whether the the validation passed, and the
	 *         ancestors found
	 */
	@SuppressWarnings("unchecked")
	private JsonObject ancestorVerificationHelper(ValidationUnit vu,
			ArrayList<ArrayList<String>> paths, JsonObject toValidate) {
		Object ancestorValues = vu.getAncestor();

		ArrayList<String> ancestorsToValidate = new ArrayList<String>();
		if (ancestorValues instanceof ArrayList) {
			ancestorsToValidate = new ArrayList<String>((ArrayList<String>) ancestorValues);
		} else {
			ancestorsToValidate.add((String) ancestorValues);
		}

		HashSet<String> ancestors = new HashSet<String>();
		for (ArrayList<String> path : paths) {
			if(path.containsAll(ancestorsToValidate))
			{
				ancestors.addAll(path);				
			}
		}

		JsonObject ancestorResult = new JsonObject();
		// if ancestors is not empty, then it was found and this check passed
		ancestorResult.addProperty("result", !ancestors.isEmpty());
		ancestorResult.addProperty("ancestors", ancestors.toString());
		return ancestorResult;
	}

	/**
	 * Helper method to verify composite key
	 * 
	 * @param vu
	 *            - the original validation unit
	 * @param toValidate
	 *            - the json to validate
	 */
	private void compositeVerificationHelper(ValidationUnit vu,
			JsonObject toValidate, JsonObject resultData) {
		Object validationObject = vu.getComposite();
		JsonArray compositeValidations = new JsonArray();
		if (validationObject instanceof JsonArray) {
			compositeValidations = (JsonArray) validationObject;
		} else if (validationObject instanceof JsonObject) {
			compositeValidations.add((JsonObject) validationObject);
		} else {
			System.out
					.println("Unrecognized Composite Validation Object. Not performing composite");
			return;
		}

		for (JsonElement comp : compositeValidations) {
			JsonObject currCompositeObject = (JsonObject) comp;
			ValidationUnit composite;
			try {
				boolean overallCompositeResult = true;
				composite = new ValidationUnit(currCompositeObject);

				// get the contexts that match each of the composite
				// keys
				JsonArray contexts = getJsonContext(composite, toValidate);
				JsonArray compositeResultCollection = new JsonArray();

				// if contexts is empty, we didn't find any matching contexts
				// and we
				// will mark as a fail
				if (contexts.size() == 0) {
					overallCompositeResult = false;
				} else {
					// we need a clone of the validation unit used in the
					// original request, minus the composite key
					// otherwise we have an infinite loop :)
					ValidationUnit vuMinusComp = new ValidationUnit(vu);

					for (JsonElement context : contexts) {
						ValidationWorker vw = new ValidationWorker();
						JsonObject contextObject = (JsonObject) context;
						JsonObject compositeResult = vw.validate(vuMinusComp,
								contextObject);
						compositeResultCollection.add(compositeResult);
						overallCompositeResult = overallCompositeResult
								&& vw.isOverallPass();
					}
				}
				resultData.addProperty(MATCH_COMPOSITE, overallCompositeResult);
				resultData.add(COMPOSITE_RESULT, compositeResultCollection);
			} catch (InvalidValidationJson e) {
				resultData.addProperty(MATCH_COMPOSITE, false);
				resultData.addProperty(COMPOSITE_RESULT, "invalid composite");
			}
		}
	}

	/**
	 * Helper method to return all of the objects that match a attribute / value
	 * pair in a json
	 * 
	 * @param attribute
	 *            - the attribute to search for
	 * @param value
	 *            - the value of the attribute
	 * @param toSearch
	 *            - the json to search
	 * @return - JsonArray of JsonObjects that match the attribute / value
	 *         criteria
	 */
	private JsonArray getJsonContext(ValidationUnit validation,
			JsonObject toSearch) {
		return getJsonContextHelper(validation, toSearch, new JsonArray());
	}

	/**
	 * Helper method to return all of the objects that match a attribute / value
	 * pair in a json
	 * 
	 * @param attribute
	 *            - the attribute to search for
	 * @param value
	 *            - the value of the attribute
	 * @param toSearch
	 *            - the json to search
	 * @param contexts
	 *            - the collection of contexts
	 * @return - JsonArray of JsonObjects that match the attribute / value
	 *         criteria
	 */
	private JsonArray getJsonContextHelper(ValidationUnit validation,
			JsonObject toSearch, JsonArray contexts) {

		String attribute = validation.getAttribute();

		// context attribute match
		// We do a base level search first, if it's here
		// then we check the rest of the context for validity
		if (toSearch.has(attribute)) {
			ValidationWorker vw = new ValidationWorker();
			vw.validate(validation, toSearch);
			boolean validateResult = vw.isOverallPass();
			if (validateResult) {
				contexts.add(toSearch);
			}
		} else {
			// otherwise, we look at the new entries and break down their scope
			Set<Entry<String, JsonElement>> entrySet = toSearch.entrySet();

			for (Entry<String, JsonElement> entry : entrySet) {
				JsonElement currVal = entry.getValue();
				if (currVal instanceof JsonArray) {
					for (JsonElement j : (JsonArray) currVal) {
						getJsonContextHelper(validation, (JsonObject) j,
								contexts);
					}
				} else if (currVal instanceof JsonObject) {
					getJsonContextHelper(validation, (JsonObject) currVal,
							contexts);
				}
			}

		}
		return contexts;
	}

	/**
	 * Recursively finds all paths to an element and returns a list of those
	 * paths
	 * 
	 * @param tree
	 *            - the JSON object to traverse
	 * @param key
	 *            - the key we're looking for
	 * @return a list of paths (represented in lists) to the element found
	 */
	private ArrayList<ArrayList<String>> findAttribute(JsonObject tree,
			String key, Object value) {
		ArrayList<ArrayList<String>> hierarchy = findAttributeHelper(tree, key, value,
				null, null);
		return hierarchy;
	}

	/**
	 * Helper method to recursively find an attribute in a nested JSON
	 * 
	 * @param tree
	 *            - the json to traverse
	 * @param key
	 *            - the key we're looking for
	 * @param history
	 *            - the path so far to the new tree
	 * @param found
	 *            - a list of found paths so far
	 * @return - a list of paths (represented in lists) to the attributes
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<ArrayList<String>> findAttributeHelper(JsonObject tree,
			String key, Object value, ArrayList<String> history,
			ArrayList<ArrayList<String>> found) {
		if (history == null) {
			history = new ArrayList<String>();
		}

		if (found == null) {
			found = new ArrayList<ArrayList<String>>();
		}

		Set<Entry<String, JsonElement>> entrySet = tree.entrySet();
		for (Entry<String, JsonElement> e : entrySet) {
			String currKey = e.getKey();

			if (currKey.equals(key)) {
				boolean add = true;
				ArrayList<String> values = new ArrayList<String>();
				if(value != null)
				{
					// if we have a value to check, then it's false
					//until we find it
					add = false;
					if (value instanceof ArrayList)
					{
						values = (ArrayList<String>) value;
						
					}
					else if (value instanceof String)
					{
						values.add((String)value);
					}
					
					for (String v : values)
					{
						if(e.getValue().getAsString().equals(v))
						{
							add = true;
						}
					}
				}
				
				if(add){
					ArrayList<String> pathToKey = (ArrayList<String>) history
							.clone();
					found.add(pathToKey);
				}
			} else {
				JsonElement currVal = e.getValue();
				history.add(currKey);
				if (currVal.isJsonObject()) {
					findAttributeHelper((JsonObject) currVal, key, value, history,
							found);
				} else if (currVal.isJsonArray()) {
					for (JsonElement j : (JsonArray) currVal) {
						findAttributeHelper((JsonObject) j, key, value, history, found);
					}
				}
				// if we don't find the key, then we want to get rid of the
				// history for this node
				history.remove(history.size() - 1);
			}
		}
		return found;
	}

	/**
	 * Returns a HashMap of the various metrics of the json object
	 * 
	 * @param json
	 *            the json to analyze
	 * @return - hashmap of data for json
	 */
	private HashMap<String, JsonObject> gatherObjectData(JsonObject json) {
		HashMap<String, JsonObject> objectData = new HashMap<String, JsonObject>();
		gatherObjectDataHelper(json, objectData);
		return objectData;
	}

	/**
	 * A helper method to traverse the JSON object to validate and gather the
	 * keys viewed and their counts and values
	 * 
	 * @return HashMap with key as viewed attribute, and value as their count
	 */
	private void gatherObjectDataHelper(JsonObject json,
			HashMap<String, JsonObject> objectData) {
		Set<Entry<String, JsonElement>> entrySet = json.entrySet();

		for (Entry<String, JsonElement> e : entrySet) {
			String currKey = e.getKey();
			JsonElement value = e.getValue();
			JsonObject currVal = new JsonObject();

			if (objectData.containsKey(currKey)) {
				currVal = objectData.get(currKey);
				// increment count
				int count = currVal.get("count").getAsInt();
				currVal.remove("count");
				currVal.addProperty("count", count + 1);

				// append value to list
				JsonArray valueList = currVal.get("values").getAsJsonArray();
				valueList.add(value);
				currVal.remove("values");
				currVal.add("values", valueList);
			} else {
				currVal.addProperty("count", 1);
				JsonArray valueList = new JsonArray();
				valueList.add(value);
				currVal.add("values", valueList);
			}

			objectData.put(currKey, currVal);

			// handle recursive cases
			if (value.isJsonObject()) {
				gatherObjectDataHelper((JsonObject) value, objectData);
			} else if (value.isJsonArray()) {
				for (JsonElement j : (JsonArray) value) {
					gatherObjectDataHelper((JsonObject) j, objectData);
				}
			}
		}
	}
}
