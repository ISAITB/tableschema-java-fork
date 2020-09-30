package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DateField extends Field<LocalDate> {
    // ISO8601 format yyyy-MM-dd
    protected static final String DEFAULT_FORMAT = "yyyy-MM-dd";

    DateField() {
        super();
    }

    public DateField(String name) {
        super(name, FIELD_TYPE_DATE);
    }

    public DateField(String name, String format, String title, String description,
                     URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_DATE, format, title, description, rdfType, constraints, options);
    }

    @Override
    public void setFormat(String format) {
        super.setFormat(TableSchemaUtil.prepareDateFormat(format, isJavaBasedDateFormats()));
        super.setDefinedFormat(format);
    }

    @Override
    public LocalDate parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (format == null || "any".equals(format) || "default".equals(format)) {
            format = DEFAULT_FORMAT;
        }
        LocalDate parsedValue = TableSchemaUtil.parseDate(value, format);
        if (parsedValue == null) {
            throw new TypeInferringException();
        }
        return parsedValue;
    }

    @Override
    public String formatValueAsString(LocalDate value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (format == null || "any".equals(format) || "default".equals(format)) {
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
            if (constraints.containsKey(CONSTRAINT_KEY_ENUM)) {
                List<?> values = (List<?>)constraints.get(CONSTRAINT_KEY_ENUM);
                List<LocalDate> valuesToUse = new ArrayList<>(values.size());
                for (Object value: values) {
                    if (value instanceof LocalDate) {
                        valuesToUse.add((LocalDate)value);
                    } else {
                        valuesToUse.add(parseValue(value.toString(), getFormat(), null));
                    }
                }
                constraints.put(CONSTRAINT_KEY_ENUM, valuesToUse);
            }
        }
    }
}
