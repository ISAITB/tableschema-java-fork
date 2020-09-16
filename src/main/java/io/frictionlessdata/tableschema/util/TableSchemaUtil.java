package io.frictionlessdata.tableschema.util;

import java.util.HashMap;
import java.util.Map;

public class TableSchemaUtil {

    public static String pythonDateFormatToJavaDateFormat(String pythonFormat) {
        if (pythonFormat == null) {
          return null;
        }
        return pythonFormat
                .replaceAll("%a", "E")
                .replaceAll("%A", "EEEE")
                .replaceAll("%w", "u") // TODO problem with Sunday (0 should be 7)
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
                .replaceAll("%f", "") // TODO microseconds versus milliseconds
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
