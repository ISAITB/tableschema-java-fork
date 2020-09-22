package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [According to spec](http://frictionlessdata.io/specs/table-schema/index.html#number), a number field
 * consists of "a non-empty finite-length sequence of decimal digits".
 */
public class IntegerField extends Field<BigInteger> {

    private boolean bareNumber = true;

    IntegerField() {
        super();
    }

    public IntegerField(String name) {
        super(name, FIELD_TYPE_INTEGER);
    }

    public IntegerField(String name, String format, String title, String description,
                        URI rdfType, Map constraints, Map options) {
        super(name, FIELD_TYPE_INTEGER, format, title, description, rdfType, constraints, options);
    }

    public boolean isBareNumber() {
        return bareNumber;
    }

    public void setBareNumber(boolean bareNumber) {
        this.bareNumber = bareNumber;
    }

    @Override
    public BigInteger parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return new BigInteger(value.trim());
    }

    @Override
    public String formatValueAsString(BigInteger value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    public Map<String, Object> getOptions() {
        if (options == null) {
            options = new HashMap<>();
            options.put(NumberField.NUMBER_OPTION_BARE_NUMBER, bareNumber);
        }
        return options;
    }

    @Override
    public void validate() {
        super.validate();
        if (constraints != null) {
            if (constraints.containsKey(CONSTRAINT_KEY_ENUM)) {
                // Items can be string or number
                List<?> values = (List<?>)constraints.get(CONSTRAINT_KEY_ENUM);
                List<Integer> valuesToUse = new ArrayList<>(values.size());
                for (Object value: values) {
                    if (value instanceof Integer) {
                        valuesToUse.add((Integer)value);
                    } else {
                        valuesToUse.add(Integer.valueOf(value.toString()));
                    }
                }
                constraints.put(CONSTRAINT_KEY_ENUM, valuesToUse);
            }
        }
    }

}
