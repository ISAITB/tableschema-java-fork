package io.frictionlessdata.tableschema.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class TableSchemaUtil {

    public static LocalDate parseDate(String value, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDate parsedValue = LocalDate.parse(value, formatter);
            String formattedParsedValue = parsedValue.format(formatter);
            if (formattedParsedValue.equals(value)) {
                return parsedValue;
            }
        } catch (DateTimeParseException e) {
            // Ignore.
        }
        return null;
    }

    public static ZonedDateTime parseDateTime(String value, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            ZonedDateTime parsedValue = ZonedDateTime.parse(value, formatter);
            String formattedParsedValue = parsedValue.format(formatter);
            if (formattedParsedValue.equals(value)) {
                return parsedValue;
            }
        } catch (DateTimeParseException e) {
            // Ignore.
        }
        return null;
    }

    public static LocalTime parseTime(String value, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalTime parsedValue = LocalTime.parse(value, formatter);
            String formattedParsedValue = parsedValue.format(formatter);
            if (formattedParsedValue.equals(value)) {
                return parsedValue;
            }
        } catch (DateTimeParseException e) {
            // Ignore.
        }
        return null;
    }

    public static String pythonDateFormatToJavaDateFormat(String pythonFormat) {
        if (pythonFormat == null) {
          return null;
        }
        return pythonFormat
                .replaceAll("%a", "EEE")
                .replaceAll("%A", "EEEE")
                .replaceAll("%w", "e") // TODO problem with Sunday (0 should be 7)
                .replaceAll("%d", "dd")
                .replaceAll("%b", "MMM")
                .replaceAll("%B", "MMMM")
                .replaceAll("%m", "MM")
                .replaceAll("%y", "yy")
                .replaceAll("%Y", "yyyy")
                .replaceAll("%H", "HH")
                .replaceAll("%I", "hh")
                .replaceAll("%p", "a")
                .replaceAll("%M", "mm")
                .replaceAll("%S", "ss")
                .replaceAll("%f", "SSSSSS")
                .replaceAll("%z", "Z")
                .replaceAll("%Z", "zzz")
                .replaceAll("%j", "D")
                .replaceAll("%U", "W")
                .replaceAll("%W", "W")
                .replaceAll("%%", "%")
                ;
    }

    public static Map<Integer, Integer> createSchemaHeaderMapping(String[] headers, String[] sortedHeaders) {
        if ((null == headers) || (null == sortedHeaders))
            return null;
        Map<Integer, Integer> mapping = new HashMap<>();

        for (int i = 0; i < sortedHeaders.length; i++) {
            for (int j = 0; j < headers.length; j++) {
                if (sortedHeaders[i].equals(headers[j])) {
                    mapping.put(i, j);
                }
            }
            // declared header not found in actual data - can happen with JSON Arrays
            // of JSON objects as they will not have keys for null values
            if (!mapping.containsKey(i)) {
                mapping.put(i, null);
            }
        }
        return mapping;
    }
}
