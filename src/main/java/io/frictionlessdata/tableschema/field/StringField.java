package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import org.apache.commons.validator.routines.EmailValidator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of a string-holding Field.
 *
 * Well-known formats:
 * - default: any valid string.
 * - email: A valid email address.
 * - uri: A valid URI.
 * - binary: A base64 encoded string representing binary data.
 * - uuid: A string that is a uuid.
 *
 * Spec: http://frictionlessdata.io/specs/table-schema/index.html#string
 */
public class StringField extends Field<String> {
    private final static String REGEX_UUID
            = "^\\{?[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}?$";
    private final static Pattern PATTERN_UUID = Pattern.compile(REGEX_UUID);

    StringField() {
        super();
    }

    public StringField(String name) {
        super(name, FIELD_TYPE_STRING);
    }

    public StringField(String name, String format, String title, String description,
                       URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_STRING, format, title, description, rdfType, constraints, options);
    }

    @Override
    public String parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value;
    }

    @Override
    public boolean valueHasValidFormat(String value) {
        String format = getFormat();
        if (format == null) {
            format = FIELD_FORMAT_DEFAULT;
        }
        if ((format.equals(FIELD_FORMAT_EMAIL) && !validEmail(value)) ||
                (format.equals(FIELD_FORMAT_UUID) && !validUuid(value)) ||
                (format.equals(FIELD_FORMAT_URI) && !validUri(value))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String formatValueAsString(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value;
    }


    /**
     * Given a value, try to parse the format.
     *
     * We don't try to identify Base64-encoded binaries, as the overlap with
     * default is way too large. We can still understand schemas with `binary` fields
     * but will not infer the format correctly.
     * @param value sample value encoded as string
     * @param options format options
     * @return inferred format encoded as a string
     */
    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        if (value != null) {
            if (validUuid(value)) {
                return FIELD_FORMAT_UUID;
            } else if (validEmail(value)) {
                return FIELD_FORMAT_EMAIL;
            } else if (validUri(value)) {
                return FIELD_FORMAT_URI;
            }
        }
        return FIELD_FORMAT_DEFAULT;
    }

    private boolean validUuid(String value) {
        Matcher uuidMatcher = PATTERN_UUID.matcher(value);
        return uuidMatcher.matches();
    }

    private boolean validEmail(String value) {
        return EmailValidator.getInstance().isValid(value);
    }

    private boolean validUri(String value) {
        try {
            URI uri = new URI(value);
            return uri.getAuthority() != null &&
                    uri.getScheme() != null &&
                    uri.getHost() != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

}
