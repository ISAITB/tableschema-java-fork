package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearmonthField extends Field<YearMonth> {
    // yyyy-MM
    private static final String REGEX_YEARMONTH = "([0-9]{4})-(1[0-2]|0[1-9])";

    YearmonthField() {
        super();
    }

    public YearmonthField(String name) {
        super(name, FIELD_TYPE_YEARMONTH);
    }

    public YearmonthField(String name, String format, String title, String description,
                          URI rdfType, Map constraints, Map options) {
        super(name, FIELD_TYPE_YEARMONTH, format, title, description, rdfType, constraints, options);
    }

    @Override
    public YearMonth parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        Pattern pattern = Pattern.compile(REGEX_YEARMONTH);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            TemporalAccessor dt = formatter.parse(value);

            return YearMonth.from(dt);
        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public String formatValueAsString(YearMonth value, String format, Map<String, Object> options)
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
                    constraints.put(CONSTRAINT_KEY_MINIMUM, YearMonth.from(DateTimeFormatter.ofPattern("yyyy-MM").parse((String)value)));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MAXIMUM, YearMonth.from(DateTimeFormatter.ofPattern("yyyy-MM").parse((String)value)));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_ENUM)) {
                List<?> values = (List<?>)constraints.get(CONSTRAINT_KEY_ENUM);
                List<YearMonth> valuesToUse = new ArrayList<>(values.size());
                for (Object value: values) {
                    if (value instanceof YearMonth) {
                        valuesToUse.add((YearMonth) value);
                    } else {
                        valuesToUse.add(parseValue(value.toString(), getFormat(), null));
                    }
                }
                constraints.put(CONSTRAINT_KEY_ENUM, valuesToUse);
            }
        }
    }
}
