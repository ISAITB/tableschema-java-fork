package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definition of a field in a data table. Doesn't hold values
 *
 * Spec: http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = AnyField.class, 
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StringField.class, name = Field.FIELD_TYPE_STRING),
    @JsonSubTypes.Type(value = IntegerField.class, name = Field.FIELD_TYPE_INTEGER),
    @JsonSubTypes.Type(value = NumberField.class, name = Field.FIELD_TYPE_NUMBER),
    @JsonSubTypes.Type(value = BooleanField.class, name = Field.FIELD_TYPE_BOOLEAN),
    @JsonSubTypes.Type(value = ObjectField.class, name = Field.FIELD_TYPE_OBJECT),
    @JsonSubTypes.Type(value = ArrayField.class, name = Field.FIELD_TYPE_ARRAY),
    @JsonSubTypes.Type(value = DateField.class, name = Field.FIELD_TYPE_DATE),
    @JsonSubTypes.Type(value = TimeField.class, name = Field.FIELD_TYPE_TIME),
    @JsonSubTypes.Type(value = DatetimeField.class, name = Field.FIELD_TYPE_DATETIME),
    @JsonSubTypes.Type(value = YearField.class, name = Field.FIELD_TYPE_YEAR),
    @JsonSubTypes.Type(value = YearmonthField.class, name = Field.FIELD_TYPE_YEARMONTH),
    @JsonSubTypes.Type(value = DurationField.class, name = Field.FIELD_TYPE_DURATION),
    @JsonSubTypes.Type(value = GeopointField.class, name = Field.FIELD_TYPE_GEOPOINT),
    @JsonSubTypes.Type(value = GeojsonField.class, name = Field.FIELD_TYPE_GEOJSON),
    @JsonSubTypes.Type(value = AnyField.class, name = Field.FIELD_TYPE_ANY)
    }
)
public abstract class Field<T> {
    public static final String FIELD_TYPE_STRING = "string";
    public static final String FIELD_TYPE_INTEGER = "integer";
    public static final String FIELD_TYPE_NUMBER = "number";
    public static final String FIELD_TYPE_BOOLEAN = "boolean";
    public static final String FIELD_TYPE_OBJECT = "object";
    public static final String FIELD_TYPE_ARRAY = "array";
    public static final String FIELD_TYPE_DATE = "date";
    public static final String FIELD_TYPE_TIME = "time";
    public static final String FIELD_TYPE_DATETIME = "datetime";
    public static final String FIELD_TYPE_YEAR = "year";
    public static final String FIELD_TYPE_YEARMONTH = "yearmonth";
    public static final String FIELD_TYPE_DURATION = "duration";
    public static final String FIELD_TYPE_GEOPOINT = "geopoint";
    public static final String FIELD_TYPE_GEOJSON = "geojson";
    public static final String FIELD_TYPE_ANY = "any";
    
    private List<String> wellKnownFieldTypes = Arrays.asList(
    	FIELD_TYPE_STRING,
    	FIELD_TYPE_INTEGER,
    	FIELD_TYPE_NUMBER,
    	FIELD_TYPE_BOOLEAN,
    	FIELD_TYPE_OBJECT,
    	FIELD_TYPE_ARRAY,
    	FIELD_TYPE_DATE,
    	FIELD_TYPE_TIME,
    	FIELD_TYPE_DATETIME,
    	FIELD_TYPE_YEAR,
    	FIELD_TYPE_YEARMONTH,
    	FIELD_TYPE_DURATION,
    	FIELD_TYPE_GEOPOINT,
    	FIELD_TYPE_GEOJSON,
    	FIELD_TYPE_ANY
    );
    
    public static final String FIELD_FORMAT_DEFAULT = "default";
    public static final String FIELD_FORMAT_ARRAY = "array";
    public static final String FIELD_FORMAT_OBJECT = "object";
    public static final String FIELD_FORMAT_TOPOJSON = "topojson";
    public static final String FIELD_FORMAT_URI = "uri";
    public static final String FIELD_FORMAT_EMAIL = "email";
    public static final String FIELD_FORMAT_BINARY = "binary";
    public static final String FIELD_FORMAT_UUID = "uuid";

    public static final String CONSTRAINT_KEY_REQUIRED = "required";
    public static final String CONSTRAINT_KEY_UNIQUE = "unique";
    public static final String CONSTRAINT_KEY_MIN_LENGTH = "minLength";
    public static final String CONSTRAINT_KEY_MAX_LENGTH = "maxLength";
    public static final String CONSTRAINT_KEY_MINIMUM = "minimum";
    public static final String CONSTRAINT_KEY_MAXIMUM = "maximum";
    public static final String CONSTRAINT_KEY_PATTERN = "pattern";
    public static final String CONSTRAINT_KEY_ENUM = "enum";
    
    public static final String JSON_KEY_NAME = "name";
    public static final String JSON_KEY_TYPE = "type";
    public static final String JSON_KEY_FORMAT = "format";
    public static final String JSON_KEY_TITLE = "title";
    public static final String JSON_KEY_RDFTYPE = "rdfType";
    public static final String JSON_KEY_DESCRIPTION = "description";
    public static final String JSON_KEY_CONSTRAINTS = "constraints";
    /**
     * The field descriptor MUST contain a `name` property.
     * `name` SHOULD NOT be considered case sensitive in determining uniqueness.
     * However, since it should correspond to the name of the field in the data file it
     * may be important to preserve case.
     * http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
     */
    private String name = "";

    /**
     * A field's `type` property is a string indicating the type of this field.
     * http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
     */
    String type = "";


    /**
     * A field's `format` property is a string, indicating a format for the field type.
     * http://frictionlessdata.io/specs/table-schema/index.html#field-descriptors
     */
    private String format = FIELD_FORMAT_DEFAULT;
    private String definedFormat = FIELD_FORMAT_DEFAULT;

    /**
     * A human readable label or title for the field
     */
    private String title = null;

    /**
     * A description for this field e.g. "The recipient of the funds"
     */
    private String description = null;

    /**
     * A richer, "semantic", description of the "type" of data in a given column MAY be
     * provided using a rdfType property on a field descriptor.
     *
     * The value of of the rdfType property MUST be the URI of a RDF Class, that is an
     * instance or subclass of RDF Schema Class object
     * http://frictionlessdata.io/specs/table-schema/index.html#rich-types
     */
    private URI rdfType = null;

    Map<String, Object> constraints = null;

    Map<String, Object> options = null;

    private Set<Object> existingValues = null;
    private Set<String> missingValues = null;
    private boolean javaBasedDateFormats = false;

    /**
     * Constructor for our reflection-based instantiation only
     */
    Field(){    }

    Field(String name, String type){
        this.name = name;
        this.type = type;
    }

    public Field(
            String name,
            String type,
            String format,
            String title,
            String description,
            URI rdfType,
            Map constraints,
            Map options){
        this.name = name;
        this.type = type;
        setFormat(format);
        this.title = title;
        this.rdfType = rdfType;
        this.description = description;
        this.constraints = constraints;
        this.options = options;
    }

    public static Field fromJson (String json) {
        return JsonUtil.getInstance().deserialize(json, Field.class);
    }


    public abstract T parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException;

    public abstract String formatValueAsString(T value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException;

    public String formatValueAsString(T value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return formatValueAsString( value, format, options);
    }

    public Object formatValueForJson(T value) throws InvalidCastException, ConstraintsException {
        return value;
    }

    /**
     * Given a value, try to parse the format. Some Field types don't have variant
     * formats and will always return `default`
     *
     * @param value sample value encoded as string
     * @param options format options
     * @return inferred format encoded as a string
     */
    public abstract String parseFormat(String value, Map<String, Object> options);

    
    /**
     * Use the Field definition to cast (=parse) a value into the Field type. Constraints enforcing
     * can be switched on or off.
     * @param value the value string to cast
     * @param enforceConstraints whether to enforce Field constraints
     * @param options casting options
     * @return result of the cast operation
     * @throws InvalidCastException if the content of `value` cannot be cast to the destination type
     * @throws ConstraintsException thrown if `enforceConstraints` was set to `true`and constraints were violated
     */
    public T castValue(String value, boolean enforceConstraints, Map<String, Object> options) throws InvalidCastException, ConstraintsException{
        if(this.type.isEmpty()){
            throw new InvalidCastException("Property 'type' must not be empty");
        } else if (missingValues.contains(value)) {
            return null;
        } else {
            try{
                T castValue = parseValue(value, format, options);
            
                // Check for constraint violations
                if(enforceConstraints && this.constraints != null){
                    Map<String, Object> violatedConstraints = checkConstraintViolations(castValue);
                    if(!violatedConstraints.isEmpty()){
                        throw new ConstraintsException("Violated "+ violatedConstraints.size()+" constraints");
                    }
                }
                
                return castValue;
                
            }catch(ConstraintsException ce){
                throw ce;
                
            }catch(Exception e){
                throw new InvalidCastException(e);
            }
        } 
    }

    /**
     * Use the Field definition to cast a value into the Field type.
     * Enforces constraints by default.
     * @param value the value string to cast
     * @return result of the cast operation
     * @throws InvalidCastException if the content of `value` cannot be cast to the destination type
     * @throws ConstraintsException thrown if `enforceConstraints` was set to `true`and constraints were violated
     */
    public T castValue(String value) throws InvalidCastException, ConstraintsException{
        return castValue(value, true, options);
    }

    
    /**
     * Returns a Map with all the constraints that have been violated.
     * @param value either a JSONArray/JSONObject or a string containing JSON
     * @return Map containing all the contraints violations
     */
    public Map<String, Object> checkConstraintViolations(Object value){
       
        Map<String, Object> violatedConstraints = new HashMap<>();
        
        // Indicates whether this field is allowed to be null. If required is true, then null is disallowed. 
        if(this.constraints.containsKey(CONSTRAINT_KEY_REQUIRED)){
            if((boolean) this.constraints.get(CONSTRAINT_KEY_REQUIRED) && value == null){
                violatedConstraints.put(CONSTRAINT_KEY_REQUIRED, true);
            }
        }
        
        // All values for that field MUST be unique within the data file in which it is found.
        // Can't check UNIQUE constraint when operating with only one value.
        // TODO: Implement a method that takes List<Object> value as argument.
        /*
        if(this.constraints.containsKey(CONSTRAINT_KEY_UNIQUE)){
    
        }*/
        
        // An integer that specifies the minimum length of a value.
        if(this.constraints.containsKey(CONSTRAINT_KEY_MIN_LENGTH)){
            int minLength = (int)this.constraints.get(CONSTRAINT_KEY_MIN_LENGTH);
 
            if(value instanceof String){
                if(((String)value).length() < minLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MIN_LENGTH, minLength);
                }
                
            }else if (value instanceof JsonNode){
                if(((JsonNode)value).size() < minLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MIN_LENGTH, minLength);
                }
                 
            }
        }
        
        // An integer that specifies the maximum length of a value.
        if(this.constraints.containsKey(CONSTRAINT_KEY_MAX_LENGTH)){
            int maxLength = (int)this.constraints.get(CONSTRAINT_KEY_MAX_LENGTH);
            
            if(value instanceof String){
                if(((String)value).length() > maxLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MAX_LENGTH, maxLength);
                }
                
            }else if (value instanceof JsonNode){
                if(((JsonNode)value).size() > maxLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MAX_LENGTH, maxLength);
                }
                 
            }else if (value instanceof ArrayNode){
                if(((ArrayNode)value).size() > maxLength){
                    violatedConstraints.put(CONSTRAINT_KEY_MAX_LENGTH, maxLength);
                }                
            }  
        }
        
        /*
         * Specifies a minimum value for a field.
         * This is different to minLength which checks the number of items in the value.
         * A minimum value constraint checks whether a field value is greater than or equal to the specified value.
         * The range checking depends on the type of the field.
         * E.g. an integer field may have a minimum value of 100; a date field might have a minimum date.
         * If a minimum value constraint is specified then the field descriptor MUST contain a type key.
         **/
        if(this.constraints.containsKey(CONSTRAINT_KEY_MINIMUM)){
            if (value instanceof Number) {
                if (this instanceof IntegerField) {
                    int minInt = Integer.parseInt(this.constraints.get(CONSTRAINT_KEY_MINIMUM).toString());
                    if(((Number)value).longValue() < minInt){
                        violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minInt);
                    }
                } else {
                    double minDouble = Double.parseDouble(this.constraints.get(CONSTRAINT_KEY_MINIMUM).toString());
                    if(((Number)value).doubleValue() < minDouble){
                        violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDouble);
                    }
                }
            } else if(value instanceof LocalTime){
                LocalTime minTime = (LocalTime)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((LocalTime)value).isBefore(minTime)){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minTime);
                }

            }else if(value instanceof ZonedDateTime){
                ZonedDateTime minTime = (ZonedDateTime)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((ZonedDateTime)value).isBefore(minTime)){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minTime);
                }

            }else if(value instanceof LocalDate){
                LocalDate minDate = (LocalDate)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((LocalDate)value).isBefore(minDate)){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDate);
                }

            }else if(value instanceof Year){
                Year minDate = (Year)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((Year)value).isBefore(minDate)){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDate);
                }

            }else if(value instanceof YearMonth){
                YearMonth minDate = (YearMonth)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((YearMonth)value).isBefore(minDate)){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDate);
                }

            }else if(value instanceof Duration){
                Duration minDuration = (Duration)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
                if(((Duration)value).compareTo(minDuration) < 0){
                    violatedConstraints.put(CONSTRAINT_KEY_MINIMUM, minDuration);
                }
            } 
        }
        
        // As for minimum, but specifies a maximum value for a field.
        if(this.constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)){
            if (value instanceof Number) {
                if (this instanceof IntegerField) {
                    int maxInt = Integer.parseInt(this.constraints.get(CONSTRAINT_KEY_MAXIMUM).toString());
                    if(((Number)value).longValue() > maxInt){
                        violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxInt);
                    }
                } else {
                    double maxDouble = Double.parseDouble(this.constraints.get(CONSTRAINT_KEY_MAXIMUM).toString());
                    if(((Number)value).doubleValue() > maxDouble){
                        violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDouble);
                    }
                }
            } else if(value instanceof LocalTime){
                LocalTime maxTime = (LocalTime)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);

                if(((LocalTime)value).isAfter(maxTime)){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxTime);
                }

            }else if(value instanceof ZonedDateTime){
                ZonedDateTime maxTime = (ZonedDateTime)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);

                if(((ZonedDateTime)value).isAfter(maxTime)){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxTime);
                }

            }else if(value instanceof LocalDate){
                LocalDate maxDate = (LocalDate)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);

                if(((LocalDate)value).isAfter(maxDate)){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDate);
                }

            }else if(value instanceof Year){
                Year maxDate = (Year)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);

                if(((Year)value).isAfter(maxDate)){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDate);
                }

            }else if(value instanceof YearMonth){
                YearMonth maxDate = (YearMonth)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);

                if(((YearMonth)value).isAfter(maxDate)){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDate);
                }

            }else if(value instanceof Duration){
                Duration maxDuration = (Duration)this.constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if(((Duration)value).compareTo(maxDuration) > 0){
                    violatedConstraints.put(CONSTRAINT_KEY_MAXIMUM, maxDuration);
                }
            } 
        }
        
        // A regular expression that can be used to test field values. If the regular expression matches then the value is valid.
        if(this.constraints.containsKey(CONSTRAINT_KEY_PATTERN)){
            String regexPatternString = (String)this.constraints.get(CONSTRAINT_KEY_PATTERN);
            
            // Constraint only applies to a String value.
            if(value instanceof String){       
                Pattern pattern = Pattern.compile(regexPatternString);
                Matcher matcher = pattern.matcher((String)value);
                
                if(!matcher.matches()){
                    violatedConstraints.put(CONSTRAINT_KEY_PATTERN, regexPatternString);
                }
            
            }else if (value != null) {
                // If the value is not a String, then just interpret as a constraint violation.
                violatedConstraints.put(CONSTRAINT_KEY_PATTERN, regexPatternString);
            }
        }
        
        // The value of the field must exactly match a value in the enum array.
        if(this.constraints.containsKey(CONSTRAINT_KEY_ENUM)){
            if (value != null) {
                // A null value would never be checked against a set of enum values.

                boolean violatesEnumConstraint = true;

                if(value instanceof String){
                    List<String> stringList = (List<String>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<String> iter = stringList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().equalsIgnoreCase((String)value)){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }else if(value instanceof JsonNode){
                    List<JsonNode> jsonObjList = (List<JsonNode>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<JsonNode> iter = jsonObjList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().equals((JsonNode)value)){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }else if(value instanceof Number){
                    if (this instanceof IntegerField) {
                        List<Integer> intList = (List<Integer>)this.constraints.get(CONSTRAINT_KEY_ENUM);
                        Iterator<Integer> iter = intList.iterator();
                        while (iter.hasNext()){
                            if(iter.next() == ((Number)value).intValue()){
                                violatesEnumConstraint = false;
                                break;
                            }
                        }
                    } else {
                        List<Double> intList = (List<Double>)this.constraints.get(CONSTRAINT_KEY_ENUM);
                        Iterator<Double> iter = intList.iterator();
                        while (iter.hasNext()){
                            if(iter.next() == ((Number)value).doubleValue()){
                                violatesEnumConstraint = false;
                                break;
                            }
                        }
                    }

                }else if(value instanceof LocalTime){
                    List<LocalTime> timeList = (List<LocalTime>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<LocalTime> iter = timeList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().compareTo((LocalTime)value) == 0){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }
                }else if(value instanceof ZonedDateTime){
                    List<ZonedDateTime> timeList = (List<ZonedDateTime>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<ZonedDateTime> iter = timeList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().compareTo((ZonedDateTime)value) == 0){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }else if(value instanceof LocalDate){
                    List<LocalDate> dateList = (List<LocalDate>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<LocalDate> iter = dateList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().compareTo((LocalDate)value) == 0){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }else if(value instanceof Year){
                    List<Year> yearList = (List<Year>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<Year> iter = yearList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().compareTo((Year)value) == 0){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }else if(value instanceof YearMonth){
                    List<YearMonth> yearMonthList = (List<YearMonth>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<YearMonth> iter = yearMonthList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().compareTo((YearMonth)value) == 0){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }else if(value instanceof Duration){
                    List<Duration> durationList = (List<Duration>)this.constraints.get(CONSTRAINT_KEY_ENUM);

                    Iterator<Duration> iter = durationList.iterator();
                    while(iter.hasNext()){
                        if(iter.next().compareTo((Duration)value) == 0){
                            violatesEnumConstraint = false;
                            break;
                        }
                    }

                }

                if(violatesEnumConstraint){
                    violatedConstraints.put(CONSTRAINT_KEY_ENUM, this.constraints.get(CONSTRAINT_KEY_ENUM));
                }
            }
        }

        if (this.constraints.containsKey(CONSTRAINT_KEY_UNIQUE) && value != null) {
            boolean unique = Boolean.parseBoolean(this.constraints.get(CONSTRAINT_KEY_UNIQUE).toString());
            if (unique) {
                if (existingValues == null) {
                    existingValues = new HashSet<>();
                }
                if (existingValues.contains(value)) {
                    violatedConstraints.put(CONSTRAINT_KEY_UNIQUE, unique);
                } else {
                    existingValues.add(value);
                }
            }
        }
        
        return violatedConstraints;
    }

    public static Field forType(String type, String name) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(JSON_KEY_TYPE, type);
        fieldMap.put(JSON_KEY_NAME, name);
        return JsonUtil.getInstance().convertValue(fieldMap, Field.class);
    }

    /**
     * Get the JSON representation of the Field.
     * @return String-serialized JSON Object containing the properties of this field
     */
    @JsonIgnore
    public String getJson(){
        return JsonUtil.getInstance().serialize(this);
    }


    @JsonIgnore
    public String getCastMethodName() {
        return "cast" + (this.type.substring(0, 1).toUpperCase() + this.type.substring(1));
    }

    public String getName(){
        return this.name;
    }
    
    public String getType(){
    	if(Objects.nonNull(this.type) && !isWellKnownType(this.type)) {
    		return FIELD_TYPE_ANY;
    	} else return this.type;
    }

	public String getFormat(){
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
        setDefinedFormat(format);
    }

    public String getDefinedFormat(){
        return this.definedFormat;
    }

    public void setDefinedFormat(String definedFormat) {
        this.definedFormat = definedFormat;
    }

    public String getTitle(){
        return this.title;
    }
    
    public String getDescription(){
        return this.description;
    }
    
    public Map<String, Object> getConstraints(){
        return this.constraints;
    }

    public URI getRdfType() {
        return rdfType;
    }

    public void setRdfType(URI rdfType) {
        this.rdfType = rdfType;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    /**
     * Similar to {@link #equals(Object)}, but disregards the `format` property
     * to allow for Schemas that are similar except that Fields have no
     * defined format. Also treats null and empty string the same for `name` and
     * `type`.
     *
     * @param other the Field to compare against
     * @return true if the other Field is equals ignoring the format, false otherwise
     */
    public boolean similar(Field other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if ((!StringUtils.isEmpty(name)) && (!StringUtils.isEmpty(other.name))){
            if (!name.equals(other.name))
                return false;
        }
        if ((!StringUtils.isEmpty(type)) && (!StringUtils.isEmpty(other.type))){
            if (!type.equals(other.type))
                return false;
        }
        return Objects.equals(constraints, other.constraints);
    }
    

    private boolean isWellKnownType(String typeName) {
		return wellKnownFieldTypes.contains(typeName);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return name.equalsIgnoreCase(field.name) &&
                type.equals(field.type) &&
                Objects.equals(format, field.format) &&
                Objects.equals(constraints, field.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, format, constraints);
    }

    @Override
    public String toString() {
        Class clazz = this.getClass();
        return clazz.getName().replace(clazz.getPackage().getName(), "")
                +" {" +
                "name='" + name + '\'' +
                ", format='" + format + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public Set<String> getMissingValues() {
        return missingValues;
    }

    public void setMissingValues(Set<String> missingValues) {
        this.missingValues = missingValues;
    }

    public boolean isJavaBasedDateFormats() {
        return javaBasedDateFormats;
    }

    public void setJavaBasedDateFormats(boolean javaBasedDateFormats) {
        this.javaBasedDateFormats = javaBasedDateFormats;
    }

    public void validate() {
        // Do nothing by default
    }

    public boolean valueHasValidFormat(String value) {
        return true;
    }

    public static String formatValueAsString(Object value, Field<?> field) {
        String valueAsString = null;
        if (field instanceof DateField) {
            valueAsString = ((DateField)field).formatValueAsString((LocalDate)value, field.getFormat(), field.getOptions());
        } else if (field instanceof DatetimeField) {
            valueAsString = ((DatetimeField)field).formatValueAsString((ZonedDateTime)value, field.getFormat(), field.getOptions());
        } else if (field instanceof TimeField) {
            valueAsString = ((TimeField)field).formatValueAsString((LocalTime)value, field.getFormat(), field.getOptions());
        } else if (value != null) {
            valueAsString = value.toString();
        }
        return valueAsString;
    }
}
