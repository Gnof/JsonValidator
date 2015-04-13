package com.gnof.core;

import java.util.ArrayList;

import com.gnof.exceptions.InvalidValidationJson;
import com.google.gson.*;

/**
 * 
 * A validation object that represents the criteria expected of a JSON object
 * 
 * @author kofong
 * 
 */
public class ValidationUnit {

	/**
	 * The attribute to validate
	 */

	private String attribute;

	/**
	 * The value of the attribute that is expected. Note this can be a single
	 * string or a list of strings, if quantity is present
	 */
	private Object value;

	/**
	 * The quantity of attributes to appear
	 */
	private int quantity = -1;

	/**
	 * a composite validation required in the context of this validation
	 * can be a JSONArray or a single json object that we construct the ValidationUnit out of
	 */
	private Object composite;

	/**
	 * a sorted list that comprehensively lists a path from the root to the
	 * attribute
	 */
	private Object hierarchy;

	/**
	 * an unsorted list of ancestors of the attribute being validated
	 */
	private Object ancestor;

	/**
	 * Constructor that clones another ValidationUnit but removes the composite key
	 * @param another - the ValidationUnit to copy
	 */
	public ValidationUnit(ValidationUnit another)
	{
		this.attribute = another.attribute;
		this.value = another.value;
		this.quantity = another.quantity;
		this.hierarchy = another.hierarchy;
		this.ancestor = another.ancestor;		
	}
	
	/**
	 * Default constructor for validation unit
	 * 
	 * @param validationJson
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ValidationUnit(JsonObject validationJson)
			throws InvalidValidationJson {
		if (!validationJson.has("attribute")) {
			throw new InvalidValidationJson(
					"'attribute' missing from Validation JSON");
		}

		JsonElement attributeElement = validationJson.get("attribute");
		attribute = attributeElement.getAsString();

		Gson gson = new Gson();
		JsonParser parser = new JsonParser();

		// We check for all the optional attributes
		if (validationJson.has("quantity")) {
			JsonElement quantityElement = validationJson.get("quantity");
			try {
				quantity = quantityElement.getAsInt();
			} catch (NumberFormatException e) {
				System.out
						.println("quantity attribute is not a valid integer: "
								+ e);
				System.out.println("quantity defaulting to 1");
				quantity = 1;
			}
		}

		if (validationJson.has("value")) {
			JsonElement valueElement = validationJson.get("value");

			String valueString = valueElement.getAsString();

			try {
				value = (ArrayList<String>) gson.fromJson(valueString,
						ArrayList.class);
			} catch (JsonSyntaxException e) {
				value = valueString;
			}
		}

		if (validationJson.has("composite")) {
			JsonElement compositeElement = validationJson.get("composite");
			// it's a string, let's wrap it up and process it as a json object
			if(compositeElement.isJsonPrimitive())
			{
				String compositeString = compositeElement.getAsString();
				JsonElement parsedCompositeElement = parser.parse(compositeString);
				if(parsedCompositeElement instanceof JsonArray)
				{
					composite = (JsonArray)parsedCompositeElement;
				}
				else if(parsedCompositeElement instanceof JsonObject)
				{
					composite = (JsonObject)parsedCompositeElement;
				}
				else
				{
					System.out.println("Parsed Json Element not recognized: " + parsedCompositeElement.getClass());
					composite = null;
				}				
			}
			else if (compositeElement.isJsonObject())
			{
				composite = compositeElement.getAsJsonObject();
			}
			else if (compositeElement.isJsonArray())
			{
				composite = (JsonArray)compositeElement;
			}
			else
			{
				System.out.println("Composite Element Not Recognized: " + compositeElement);
				composite = null;
			}
			
		}

		if (validationJson.has("hierarchy")) {
			JsonElement hierarchyElement = validationJson.get("hierarchy");
			String hierarchyString = hierarchyElement.getAsString();
			try {
				hierarchy = (ArrayList<ArrayList<String>>) gson.fromJson(hierarchyString, ArrayList.class);				
			} catch (JsonSyntaxException | ClassCastException e) {
				System.out.println("Unrecognized format for hierarchy. Should be a list of arrays. ex: [[path1, path2], [path1]]");							
				hierarchy = null;
			}
		}

		if (validationJson.has("ancestor")) {
			JsonElement ancestorElement = validationJson.get("ancestor");
			String ancestorString = ancestorElement.getAsString();
			try {
				ancestor = (ArrayList<String>) gson.fromJson(ancestorString,
						ArrayList.class);
			} catch (JsonSyntaxException | ClassCastException e) {
				ancestor = ancestorString;
			}

		}
	}

	/**
	 * 
	 * @return whether a quantity attribute exists to verify
	 */
	public boolean hasQuantity() {
		return quantity != -1;
	}

	/**
	 * 
	 * @return Whether a value attribute exists to verify
	 */
	public boolean hasValue() {
		return value != null;
	}

	/**
	 * 
	 * @return whether the composite attribute exists to verify
	 */
	public boolean hasComposite() {
		return composite != null;
	}

	/**
	 * 
	 * @return whether the ancestor attribute exists to verify
	 */
	public boolean hasAncestor() {
		return ancestor != null;
	}

	/**
	 * 
	 * @return whether the hierarchy attribute exists to verify
	 */
	public boolean hasHierarchy() {
		return hierarchy != null;
	}

	/**
	 * composite Getter
	 * 
	 * @return composite ValidationUnit
	 */
	public Object getComposite() {
		return composite;
	}

	/**
	 * ancestor Getter
	 * 
	 * @return ancestor JSONObject
	 */
	public Object getAncestor() {
		return ancestor;
	}

	/**
	 * attribute Getter
	 * 
	 * @return attribute string
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * hierarchy Getter
	 * 
	 * @return hierarchy Object
	 */
	public Object getHierarchy() {
		return hierarchy;
	}

	/**
	 * value Getter
	 * 
	 * @return value Object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * quantity Getter
	 * 
	 * @return quantity int
	 */
	public int getQuantity() {
		return quantity;
	}
}
