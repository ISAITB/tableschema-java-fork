package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;

import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeField extends Field<LocalTime> {
    // An ISO8601 time string e.g. HH:mm:ss
    private static final String REGEX_TIME = "(2[0-3]|[01]?[0-9]):?([0-5]?[0-9]):?([0-5]?[0-9])";

    TimeField() {
        super();
    }

    public TimeField(String name) {
        super(name, FIELD_TYPE_TIME);
    }

    public TimeField(String name, String format, String title, String description,
                     URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_TIME, format, title, description, rdfType, constraints, options);
    }

    @Override
    public void setFormat(String format) {
        super.setFormat(TableSchemaUtil.pythonDateFormatToJavaDateFormat(format));
        super.setDefinedFormat(format);
    }

    @Override
    public LocalTime parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {

        if (format == null) {
            Pattern pattern = Pattern.compile(REGEX_TIME);
            Matcher matcher = pattern.matcher(value);
            if(matcher.matches()){
                //DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
                //DateTime dt = formatter.parseDateTime(value);
                return LocalTime.parse(value);
            }else{
                throw new TypeInferringException();
            }

        } else {
            return LocalTime.from(DateTimeFormatter.ofPattern(format).parse(value));
        }

    }

    @Override
    public String formatValueAsString(LocalTime value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (format == null) {
            return value.format(DateTimeFormatter.ISO_LOCAL_TIME);
        } else {
            return value.format(DateTimeFormatter.ofPattern(format));
        }
        //return value.toString(DateTimeFormat.forPattern("HH:mm:ss"));
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
                    constraints.put(CONSTRAINT_KEY_MINIMUM, LocalTime.from(DateTimeFormatter.ofPattern(getFormat()).parse((String)value)));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MAXIMUM, LocalTime.from(DateTimeFormatter.ofPattern(getFormat()).parse((String)value)));
                }
            }
        }
    }

}
