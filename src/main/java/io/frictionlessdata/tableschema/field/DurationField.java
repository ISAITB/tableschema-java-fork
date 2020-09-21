package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public class DurationField extends Field<Duration> {

    DurationField() {
        super();
    }

    public DurationField(String name) {
        super(name, FIELD_TYPE_DURATION);
    }

    public DurationField(String name, String format, String title, String description,
                         URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_DURATION, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Duration parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        try{
            return Duration.parse(value);
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    @Override
    public String formatValueAsString(Duration value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
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
                    constraints.put(CONSTRAINT_KEY_MINIMUM, Duration.parse((String)value));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MAXIMUM, Duration.parse((String)value));
                }
            }
        }
    }

}
