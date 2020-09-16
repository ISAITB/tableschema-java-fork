package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearField extends Field<Year> {
    // yyyy
    private static final String REGEX_YEAR = "([0-9]{4})";

    YearField() {
        super();
    }

    public YearField(String name) {
        super(name, FIELD_TYPE_YEAR);
    }

    public YearField(String name, String format, String title, String description,
                     URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_YEAR, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Year parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        Pattern pattern = Pattern.compile(REGEX_YEAR);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            return Year.parse(value);

        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public Object formatValueForJson(Year value) throws InvalidCastException, ConstraintsException {
        return value.getValue();
    }

    @Override
    public String formatValueAsString(Year value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return value.toString();
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    public void validate() {
        super.validate();
        if (constraints != null) {
            if (constraints.containsKey(CONSTRAINT_KEY_MINIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MINIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MINIMUM, Year.from(DateTimeFormatter.ofPattern("yyyy").parse((String)value)));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MAXIMUM, Year.from(DateTimeFormatter.ofPattern("yyyy").parse((String)value)));
                }
            }
        }
    }
}
