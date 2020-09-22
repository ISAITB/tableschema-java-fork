package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;

import java.net.URI;
import java.util.*;

public class BooleanField extends Field<Boolean> {

    private static final String BOOLEAN_OPTION_TRUE_VALUES = "trueValues";
    private static final String BOOLEAN_OPTION_FALSE_VALUES = "falseValues";

    @JsonIgnore
    private static final List<String> DEFAULT_TRUE_VALUES = Arrays.asList("true", "True", "TRUE", "1");

    @JsonIgnore
    private static final List<String> DEFAULT_FALSE_VALUES = Arrays.asList("false", "False", "FALSE", "0");

    private List<String> trueValues = DEFAULT_TRUE_VALUES;
    private List<String> falseValues = DEFAULT_FALSE_VALUES;


    BooleanField() {
        super();
    }

    public BooleanField(String name) {
        super(name, FIELD_TYPE_BOOLEAN);
    }

    public BooleanField(String name, String format, String title, String description,
                        URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_BOOLEAN, format, title, description, rdfType, constraints, options);
    }

    public void setTrueValues(List<String> newValues) {
        trueValues = newValues;
    }

    public void setFalseValues(List<String> newValues) {
        falseValues = newValues;
    }

    @Override
    public Boolean parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        List<String> trueValuesToUse = trueValues;
        List<String> falseValuesToUse = falseValues;
        if (null != options) {
            if (options.containsKey(BOOLEAN_OPTION_TRUE_VALUES)) {
                trueValuesToUse = (List<String>)options.get(BOOLEAN_OPTION_TRUE_VALUES);
            }
            if (options.containsKey(BOOLEAN_OPTION_FALSE_VALUES)) {
                falseValuesToUse = (List<String>)options.get(BOOLEAN_OPTION_FALSE_VALUES);
            }
        }

        if (trueValuesToUse.contains(value)){
            return true;

        }else if (falseValuesToUse.contains(value)){
            return false;

        }else{
            throw new InvalidCastException("Value "+value+" not in '"+BOOLEAN_OPTION_TRUE_VALUES+"' or '"+BOOLEAN_OPTION_FALSE_VALUES+"'");
        }
    }

    @Override
    public String formatValueAsString(Boolean value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return (value) ? _getActualTrueValues().get(0) : _getActualFalseValues().get(0);
    }

    @Override
    public String formatValueAsString(Boolean value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        String trueValue = _getActualTrueValues().get(0);
        String falseValue = _getActualFalseValues().get(0);
        if (null != options) {
            if (options.containsKey(BOOLEAN_OPTION_TRUE_VALUES)) {
                trueValue = new ArrayList<String>((Collection) options.get(BOOLEAN_OPTION_TRUE_VALUES)).iterator().next();
            }
            if (options.containsKey(BOOLEAN_OPTION_FALSE_VALUES)) {
                falseValue = new ArrayList<String>((Collection) options.get(BOOLEAN_OPTION_FALSE_VALUES)).iterator().next();
            }
        }
        return (value) ? trueValue : falseValue;
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    public static Field fromJson (String json) {
    	return Field.fromJson(json);
    }

    public List<String> getTrueValues() {
        return trueValues;
    }

    public List<String> getFalseValues() {
        return falseValues;
    }

    @JsonIgnore
    private List<String> _getActualTrueValues() {
        if ((null == trueValues) || (trueValues.isEmpty()))
            return DEFAULT_TRUE_VALUES;
        return trueValues;
    }

    @JsonIgnore
    private List<String> _getActualFalseValues() {
        if ((null == falseValues) || (falseValues.isEmpty()))
            return DEFAULT_FALSE_VALUES;
        return falseValues;
    }

    @Override
    public Map<String, Object> getOptions() {
        if (options == null) {
            options = new HashMap<>();
            options.put(BOOLEAN_OPTION_FALSE_VALUES, falseValues);
            options.put(BOOLEAN_OPTION_TRUE_VALUES, trueValues);
        }
        return options;
    }

}
