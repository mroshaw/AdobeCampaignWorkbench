package com.myapp;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampaignFunctions {

    public static Calendar parseTime(String strTime) throws IllegalArgumentException {
        if (strTime == null || strTime.length() == 0)
            return null;
        try {
            Calendar cal = Calendar.getInstance();
            int iIndex = strTime.indexOf(':');
            int iHour = Integer.parseInt(strTime.substring(0, iIndex));
            strTime = strTime.substring(iIndex + 1);
            iIndex = strTime.indexOf(':');
            int iMin = Integer.parseInt(strTime.substring(0, iIndex));
            strTime = strTime.substring(iIndex + 1);
            int iSec = (int)Double.parseDouble(strTime);
            cal.set(0, 0, 1, iHour, iMin, iSec);
            return cal;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error formatting date: " + strTime, e);
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

            // Normalize fractional seconds to 3 digits (milliseconds)
            Pattern pattern = Pattern.compile("\\.(\\d{1,3})Z$");
            Matcher matcher = pattern.matcher(dateStr);
            if (matcher.find()) {
                String frac = matcher.group(1);
                if (frac.length() == 1) frac += "00";
                else if (frac.length() == 2) frac += "0";
                dateStr = matcher.replaceFirst("." + frac + "Z");
            }

            // Parse the date string
            OffsetDateTime odt = OffsetDateTime.parse(dateStr);
            ZonedDateTime zdt = odt.toZonedDateTime(); // convert to system default zone

            // Translate ACC format to Java pattern
            String javaFormat = translateACCFormat(formatStr);

            return zdt.format(DateTimeFormatter.ofPattern(javaFormat));

        } catch (Exception ex) {
            throw new IllegalArgumentException("Error formatting date: " + dateObj, ex);
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
        String javaPattern = accFormat
                .replace("%Bl", "MMMM")   // full month name
                .replace("%B", "MMMM")    // full month name (standard)
                .replace("%b", "MMM")     // short month name
                .replace("%D", "dd")      // day
                .replace("%d", "dd")      // day
                .replace("%m", "MM")      // month number
                .replace("%Y", "yyyy")    // 4-digit year
                .replace("%4Y", "yyyy")   // ACC alias
                .replace("%y", "yy");     // 2-digit year

        return javaPattern;
    }
}
