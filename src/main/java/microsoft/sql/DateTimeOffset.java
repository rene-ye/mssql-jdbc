/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */

package microsoft.sql;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;


/**
 * Represents the SQL Server DATETIMEOFFSET data type.
 *
 * The DateTimeOffset class represents a java.sql.Timestamp, including fractional seconds, plus an integer representing
 * the number of minutes offset from GMT.
 */
public final class DateTimeOffset implements java.io.Serializable, java.lang.Comparable<DateTimeOffset> {
    private static final long serialVersionUID = 541973748553014280L;
    OffsetDateTime odt;

    /**
     * Constructs a DateTimeOffset.
     *
     * This method does not check that its arguments represent a timestamp value that falls within the range of values
     * acceptable to SQL Server for the DATETIMEOFFSET data type. That is, it is possible to create a DateTimeOffset
     * instance representing a value outside the range from 1 January 1AD 00:00:00 UTC to 31 December 9999 00:00:00 UTC.
     */
    private DateTimeOffset(java.sql.Timestamp timestamp, int minutesOffset) {
        odt = OffsetDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneId.of("UTC")).plusMinutes(minutesOffset);
    }

    private DateTimeOffset(OffsetDateTime odt) {
        this.odt = odt;
    }

    public static DateTimeOffset valueOf(long nanonsSinceMidnight, int utcDaysIntoCE, int localMinutesOffset) {
        java.time.ZoneOffset zo = ZoneOffset.ofTotalSeconds(localMinutesOffset * 60);
        OffsetDateTime odt = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, zo);
        return new DateTimeOffset(odt.plusDays(utcDaysIntoCE).plusNanos(nanonsSinceMidnight));
    }

    /**
     * Converts a java.sql.Timestamp value with an integer offset to the equivalent DateTimeOffset value
     * 
     * @param timestamp
     *        A java.sql.Timestamp value
     * @param minutesOffset
     *        An integer offset in minutes
     * @return The DateTimeOffset value of the input timestamp and minutesOffset
     */
    public static DateTimeOffset valueOf(java.sql.Timestamp timestamp, int minutesOffset) {
        return new DateTimeOffset(timestamp, minutesOffset);
    }

    /**
     * Converts a java.sql.Timestamp value with a Calendar value to the equivalent DateTimeOffset value
     * 
     * @param timestamp
     *        A java.sql.Timestamp value
     * @param calendar
     *        A java.util.Calendar value
     * @return The DateTimeOffset value of the input timestamp and calendar
     */
    public static DateTimeOffset valueOf(java.sql.Timestamp timestamp, java.util.Calendar calendar) {
        java.util.TimeZone tz = calendar.getTimeZone();
        int minutesOffset = tz == null ? 0 : tz.getRawOffset() / 1000 / 60;
        return new DateTimeOffset(timestamp, minutesOffset);
    }

    private java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS xxx");

    /**
     * Formats a datetimeoffset as yyyy-mm-dd hh:mm:ss[.fffffffff] [+|-]hh:mm, where yyyy-mm-dd hh:mm:ss[.fffffffff]
     * indicates a timestamp that is offset from UTC by the number of minutes indicated by [+|-]hh:mm.
     *
     * @return a String object in yyyy-mm-dd hh:mm:ss[.fffffffff] [+|-]hh:mm format
     */
    @Override
    public String toString() {
        return fmt.format(odt);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DateTimeOffset)) {
            return false;
        } else {
            return ((DateTimeOffset) o).getOffsetDateTime().compareTo(odt) == 0;
        }
    }

    @Override
    public int hashCode() {
        return odt.hashCode();
    }

    /**
     * Returns this DateTimeOffset object's timestamp value.
     * <p>
     * The returned value represents an instant in time as the number of milliseconds since January 1, 1970, 00:00:00
     * GMT.
     *
     * @return this DateTimeOffset object's timestamp component
     */
    public java.sql.Timestamp getTimestamp() {
        return java.sql.Timestamp.valueOf(odt.atZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime());
    }

    /**
     * Returns OffsetDateTime equivalent to this DateTimeOffset object.
     *
     * @return OffsetDateTime equivalent to this DateTimeOffset object.
     */
    public java.time.OffsetDateTime getOffsetDateTime() {
        return odt;
    }

    /**
     * Returns this DateTimeOffset object's offset value.
     *
     * @return this DateTimeOffset object's minutes offset from GMT
     */
    public int getMinutesOffset() {
        // placeholder
        return odt.getMinute();
    }

    /**
     * Compares this DateTimeOffset object with another DateTimeOffset object to determine their relative order.
     * <p>
     * The ordering is based on the timestamp component only. The offset component is not compared. Two DateTimeOffset
     * objects are considered equivalent with respect to ordering as long as they represent the same moment in time,
     * regardless of the location of the event. This is how SQL Server orders DATETIMEOFFSET values.
     *
     * @return a negative integer, zero, or a positive integer as this DateTimeOffset is less than, equal to, or greater
     *         than the specified DateTimeOffset.
     */
    public int compareTo(DateTimeOffset other) {
        return other.getOffsetDateTime().compareTo(odt);
    }

    private static class SerializationProxy implements java.io.Serializable {
        private final OffsetDateTime odt;

        SerializationProxy(DateTimeOffset dateTimeOffset) {
            this.odt = dateTimeOffset.getOffsetDateTime();
        }

        private static final long serialVersionUID = 664661379547314226L;

        private Object readResolve() {
            return new DateTimeOffset(this.odt);
        }
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.InvalidObjectException {
        // For added security/robustness, the only way to rehydrate a serialized DateTimeOffset
        // is to use a SerializationProxy. Direct use of readObject() is not supported.
        throw new java.io.InvalidObjectException("");
    }
}
