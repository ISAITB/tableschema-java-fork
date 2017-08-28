# tableschema-java
[![Build Status](https://travis-ci.org/frictionlessdata/tableschema-java.svg?branch=master)](https://travis-ci.org/frictionlessdata/tableschema-java)
[![Coverage Status](https://coveralls.io/repos/github/frictionlessdata/tableschema-java/badge.svg?branch=master)](https://coveralls.io/github/frictionlessdata/tableschema-java?branch=master)
[![Gitter](https://img.shields.io/gitter/room/frictionlessdata/chat.svg)](https://gitter.im/frictionlessdata/chat)

A Java library for working with Table Schema.


## Usage

### Parse a CSV Without a Schema

Cast data from a CSV without a schema:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
Table table = new Table(url);

// Iterate through rows          
Iterator<String[]> iter = table.iterator();
while(iter.hasNext()){
    String[] row = iter.next();
    System.out.println(Arrays.toString(row));
}

// [1, foo]
// [2, bar]
// [3, baz]

// Read the entire CSV and output it as a List:
List<String[]> allData = table.read();
```

### Parse a CSV With a Schema

Cast data from a CSV with a schema:
```java
//TODO: Implement and document this.
```

### Infer a Schema

If you don't have a schema for a CSV, and want to generate one, you can infer a schema like so:

```java
URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
Table table = new Table(url);

JSONObject schema = table.inferSchema();
System.out.println(schema);

// {"fields":[{"name":"id","format":"","description":"","title":"","type":"integer","constraints":{}},{"name":"title","format":"","description":"","title":"","type":"string","constraints":{}}]}

```

### Build a Schema

You can also build a `Schema` instance from scratch or modify an existing one:

```java
Schema schema = new Schema();

Field nameField = new Field("name", Field.FIELD_TYPE_STRING);
schema.addField(nameField);

Field coordinatesField = new Field("coordinates", Field.FIELD_TYPE_GEOPOINT);
schema.addField(coordinatesField);

System.out.println(schema.getJson());

// {"fields":[{"name":"name","format":"default","description":"","type":"string","title":"","constraints":{}},{"name":"coordinates","format":"default","description":"","type":"geopoint","title":"","constraints":{}}]}
```

You can also build a `Schema` instance with `JSONObject` instances instead of `Field` instances:

```java
Schema schema = new Schema();

JSONObject nameFieldJsonObject = new JSONObject();
nameFieldJsonObject.put("name", "name");
nameFieldJsonObject.put("type", Field.FIELD_TYPE_STRING);
schema.addField(nameFieldJsonObject);

// An invalid Field definition, will be ignored.
JSONObject invalidFieldJsonObject = new JSONObject();
invalidFieldJsonObject.put("name", "id");
invalidFieldJsonObject.put("type", Field.FIELD_TYPE_INTEGER);
invalidFieldJsonObject.put("format", "invalid");
schema.addField(invalidFieldJsonObject);

JSONObject coordinatesFieldJsonObject = new JSONObject();
coordinatesFieldJsonObject.put("name", "coordinates");
coordinatesFieldJsonObject.put("type", Field.FIELD_TYPE_GEOPOINT);
coordinatesFieldJsonObject.put("format", Field.FIELD_FORMAT_ARRAY);
schema.addField(coordinatesFieldJsonObject);

System.out.println(schema.getJson());

// {"fields":[{"name":"name","type":"string"},{"name":"coordinates","format":"array","type":"geopoint"}]}
```

When using the `addField` method, the schema undergoes validation after every field addition.
If adding a field causes the schema to fail validation, then the field is automatically removed.

### Validate a Schema
To make sure a schema complies with [Table Schema specifications](https://specs.frictionlessdata.io/table-schema/), we can validate each custom schema against the official [Table Schema schema](https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/main/resources/schemas/table-schema.json):

```java
JSONObject schemaJsonObj = new JSONObject();
Field nameField = new Field("id", Field.FIELD_TYPE_INTEGER);
schemaJsonObj.put("fields", new JSONArray());
schemaJsonObj.getJSONArray("fields").put(nameField.getJson());

Schema schema = new Schema(schemaJsonObj);

boolean isValid = schema.validate();
System.out.println(isValid);

// true

Field invalidField = new Field("coordinates", "invalid");
schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());

isValid = schema.validate();
System.out.println(isValid);

// false
```

### Row Casting
To check if a given set of values complies with the schema, you can use `castRow`:

```java
Schema schema = new Schema();
        
// A String field.
Field stringField = new Field("stringField", Field.FIELD_TYPE_STRING);
schema.addField(stringField);

// An Integer field.
Field integerField = new Field("integerField", Field.FIELD_TYPE_INTEGER);
schema.addField(integerField);

// A Boolean field.
Field booleanField = new Field("booleanField", Field.FIELD_TYPE_BOOLEAN);
schema.addField(booleanField);

// Define a given set of values:
String[] row = new String[]{"John Doe", "25", "T"}

// Cast the row's values into their schema defined types: 
Object[] castRow = schema.castRow(row);
```

If a value in the given set of values cannot be cast to its expected type as defined by the schema, then an `InvalidCastException` is thrown.

### Field Casting
Data values can be cast to native Java objects with a Field instance. This allows formats and constraints to be defined for the field in the [field descriptor](https://specs.frictionlessdata.io/table-schema/#field-descriptors):

```java
Field intField = new Field("id", Field.FIELD_TYPE_INTEGER);
int intVal = intField.castValue("242");
System.out.print(intVal);

// 242

Field datetimeField = new Field("date", Field.FIELD_TYPE_DATETIME);
DateTime datetimeVal = datetimeField.castValue("2008-08-30T01:45:36.123Z");
System.out.print(datetimeVal.getYear());

// 2008

Field geopointField = new Field("coordinates", Field.FIELD_TYPE_GEOPOINT, Field.FIELD_FORMAT_ARRAY);
int[] geopointVal = geopointField.castValue("[12,21]");
System.out.print("lon: " + geopointVal[0] + ", lat: " + geopointVal[1]);

// lon: 12, lat: 21
```

Casting a value will check the value is of the expected type, is in the correct format, and complies with any constraints imposed in the descriptor.

Value that can't be cast will raise an `InvalidCastException`.

By default, casting a value that does not meet the constraints will raise a `ConstraintsException`.
Constraints can be ignored with by setting a boolean flag to false:

```java
// Define constraint limiting String length between 30 and 40 characters:
Map<String, Object> constraints = new HashMap();
constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 30);
constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 40);

// Cast a field and cast a value that violates the above constraint.
// Disable constrain enforcement by setting the enforceConstraints boolean flag to false.
Field field = new Field("name", Field.FIELD_TYPE_STRING, null, null, null, constraints);
field.castValue("This string length is greater than 45 characters.", false); // Setting false here ignores constraints during cast.

// ConstraintsException will not be thrown despite casting a value that does not meet the constraints.
```

You can call the `checkConstraintViolations` method to find out which constraints are being validated.
The method returns a map of violated constraints:

```java
Map<String, Object> constraints = new HashMap();
constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 5);
constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 15);

Field field = new Field("name", Field.FIELD_TYPE_INTEGER, null, null, null, constraints);

int constraintViolatingValue = 16;
Map<String, Object> violatedConstraints = field.checkConstraintViolations(constraintViolatingValue);

System.out.println(violatedConstraints);

// {maximum=15}
```
