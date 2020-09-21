package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;

import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TimeField extends Field<LocalTime> {

    protected static final String DEFAULT_FORMAT = "HH:mm:ss";

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
        if (format == null || "default".equals(format)) {
            format = DEFAULT_FORMAT;
        }
        LocalTime parsedValue = TableSchemaUtil.parseTime(value, format);
        if (parsedValue == null) {
            throw new TypeInferringException();
        }
        return parsedValue;
    }

    @Override
    public String formatValueAsString(LocalTime value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (format == null || "default".equals(format)) {
            return value.format(DateTimeFormatter.ofPattern(DEFAULT_FORMAT));
        } else {
            return value.format(DateTimeFormatter.ofPattern(format));
        }
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
                    constraints.put(CONSTRAINT_KEY_MINIMUM, parseValue((String)value, getFormat(), null));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MAXIMUM, parseValue((String)value, getFormat(), null));
                }
            }
        }
    }

}
