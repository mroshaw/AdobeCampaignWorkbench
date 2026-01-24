package com.campaignworkbench.campaignrenderer;

import org.mozilla.javascript.Context;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides implementations of bespoke Adobe Campaign functions, that can then be included in template, block, and
 * module code
 */
public class CampaignFunctions {

    private CampaignFunctions() {}

    /**
     * Parses an Adobe Campaign timestamp string into a Calendar.
     * Accepts a simplified ISO 8601 style string with optional time/timezone.
     *
     * @param timestamp as an ISO 8601 string
     * @return Object with corresponding instant (UTC based)
     */
    public static Object parseTimeStamp(String timestamp) {
        try {
            Instant instant = Instant.parse(timestamp); // parse ISO-8601
            // Convert to JS Date in Rhino
            Context cx = Context.getCurrentContext();
            return cx.newObject(cx.initStandardObjects(), "Date", new Object[]{instant.toEpochMilli()});
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Error parsing timestamp: " + timestamp, ex);
        }
    }

    /**
     * Format an Adobe Campaign-style date string using an ACC format string.
     * Supports fractional seconds and Z offsets in the input.
     *
     * @param dateObj   The date object (from JS XML, XMLList, or string)
     * @param formatStr The ACC format string (e.g., "%D %Bl %4Y %H:%m:%s")
     * @return Formatted date string
     */
    public static String formatDate(Object dateObj, String formatStr) {
        try {
            String dateStr = dateObj.toString().trim();

            // Normalize fractional seconds to 3 digits
            Pattern fracPattern = Pattern.compile("\\.(\\d{1,3})Z$");
            Matcher matcher = fracPattern.matcher(dateStr);
            if (matcher.find()) {
                String frac = matcher.group(1);
                while (frac.length() < 3) {
                    frac += "0"; // pad to 3 digits
                }
                // replace only the matched part with normalized fraction
                dateStr = matcher.replaceFirst("." + frac + "Z");
            }

            // Parse the date
            OffsetDateTime odt = OffsetDateTime.parse(dateStr);
            ZonedDateTime zdt = odt.toZonedDateTime(); // system default zone

            // Translate Adobe Campaign format to Java pattern
            String javaFormat = translateACCFormat(formatStr);
            return zdt.format(DateTimeFormatter.ofPattern(javaFormat));

        } catch (Exception ex) {
            throw new IllegalArgumentException("Error in formatDate: " + dateObj, ex);
        }
    }


    /**
     * Convert Adobe Campaign date format strings into Java DateTimeFormatter patterns.
     *
     * Preserves the correct handling of %Bl, %B, %b and other ACC quirks.
     *
     * @param accFormat ACC format string
     * @return Java date format pattern
     */
    private static String translateACCFormat(String accFormat) {
        if (accFormat == null) return "";

        // Replace ACC patterns, longer first
        // 2-digit year
        return accFormat
                .replace("%Bl", "MMMM")   // full month name
                .replace("%B", "MMMM")    // full month name (standard)
                .replace("%b", "MMM")     // short month name
                .replace("%D", "dd")      // day
                .replace("%d", "dd")      // day
                .replace("%m", "MM")      // month number
                .replace("%Y", "yyyy")    // 4-digit year
                .replace("%4Y", "yyyy")   // ACC alias
                .replace("%y", "yy");
    }
}
