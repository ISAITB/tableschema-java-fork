package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateField extends Field<LocalDate> {
    // ISO8601 format yyyy-MM-dd
    private static final String REGEX_DATE = "([0-9]{4})-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])";

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
        super.setFormat(TableSchemaUtil.pythonDateFormatToJavaDateFormat(format));
        super.setDefinedFormat(format);
    }

    @Override
    public LocalDate parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        if (format == null) {
            Pattern pattern = Pattern.compile(REGEX_DATE);
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                TemporalAccessor dt = formatter.parse(value);
                return LocalDate.from(dt);

            } else {
                throw new TypeInferringException();
            }
        } else {
            return LocalDate.from(DateTimeFormatter.ofPattern(format).parse(value));
        }
    }

    @Override
    public String formatValueAsString(LocalDate value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (format == null) {
            return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
                    constraints.put(CONSTRAINT_KEY_MINIMUM, LocalDate.from(DateTimeFormatter.ofPattern(getFormat()).parse((String)value)));
                }
            }
            if (constraints.containsKey(CONSTRAINT_KEY_MAXIMUM)) {
                Object value = constraints.get(CONSTRAINT_KEY_MAXIMUM);
                if (value instanceof String) {
                    constraints.put(CONSTRAINT_KEY_MAXIMUM, LocalDate.from(DateTimeFormatter.ofPattern(getFormat()).parse((String)value)));
                }
            }
        }
    }
}
