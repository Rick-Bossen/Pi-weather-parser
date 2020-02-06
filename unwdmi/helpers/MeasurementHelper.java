package unwdmi.helpers;

import unwdmi.Type;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class represents a measurement helper.
 * It's purpose is to read an XML file and convert it to a byte array.
 * Missing or incorrect values in the XML file are corrected using extrapolation.
 *
 * @author Rick
 * @author Martijn
 */
public class MeasurementHelper {

    private ByteBuffer measurement;

    private int index = 0;
    private float[] stationAverages = new float[80];
    private boolean[] averagesIsSet = new boolean[80];
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    private String currentDate;

    /**
     * Start new {@link StringBuilder} and loop through lines to gather values.
     *
     * @param lines Lines to decode.
     * @return Decoded XML string in sql value format. e.g. (value1, value2)
     */
    public byte[] parseXML(List<String> lines) {
        if (measurement == null){
            measurement = ByteBuffer.allocate(35);
        }else{
            measurement.clear();
        }
        for (String line : lines) {
            char[] chars = line.toCharArray();
            if (chars[2] == '<') {
                parseLine(line.toCharArray());
            }
        }
        return measurement.array();
    }

    /**
     * Converts a binary {@link String} consisting of 1's and 0's to a byte array.
     *
     * @param s {@link String} to be converted.
     * @return Binary array of the given {@link String}.
     */
    private byte[] decodeBinary(String s) {
        if (s.length() % 8 != 0) throw new IllegalArgumentException(
                "Binary data length must be multiple of 8");
        byte[] data = new byte[s.length() / 8];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '1') {
                data[i >> 3] |= 0x80 >> (i & 0x7);
            } else if (c != '0') {
                throw new IllegalArgumentException("Invalid char in binary string");
            }
        }
        return data;
    }

    /**
     * Parse the line based on the chars withing.
     * Corrects wrong or missing values.
     *
     * @param chars Chars of the line.
     */
    private void parseLine(char[] chars) {
        StringBuilder tagBuilder = new StringBuilder(6);
        StringBuilder valueBuilder = new StringBuilder(10);
        boolean tagEnd = false;
        boolean hasDecimals = false;
        int usedDecimals = 0;
        for (int i = 3; i < chars.length; i++) {
            if (!tagEnd) {
                // <tag> = > = value afterwards
                if (chars[i] == '>') {
                    tagEnd = true;
                } else {
                    tagBuilder.append(chars[i]);
                }
            } else {
                // </tag> = < = value end
                if (chars[i] == '<') {
                    break;
                }
                // Remember the amount of decimals after the . for later formatting
                if (chars[i] == '.') {
                    hasDecimals = true;
                    continue;
                }
                if (hasDecimals) {
                    usedDecimals++;
                }
                valueBuilder.append(chars[i]);
            }
        }

        String tag = tagBuilder.toString();
        Type type = Type.valueOf(tag);

        // Since values are kept as integers make sure it is the right length.
        int e;
        if ((e = type.getExponent()) > 0 && valueBuilder.length() > 0) {
            valueBuilder.append("0".repeat(Math.max(0, (e - usedDecimals))));
        }

        // Save current station id for further key usages.
        StringBuilder value = new StringBuilder(valueBuilder.toString());
        if (type.equals(Type.STN)) {
            measurement.putInt(Integer.parseInt(value.toString()));
            index++;
            if (index >= 10) {
                index = 0;
            }
        } else if (type.equals(Type.DATE)) {
            currentDate = value.toString();
        } else if (type.equals(Type.TIME)) {
            String time = currentDate + "_" + value.toString();
            try {
                Date date = format.parse(time);
                int timestamp = (int) (date.getTime()  / 1000); // generate unix timestamp
                measurement.put(ByteBuffer.allocate(4).putInt(timestamp).array());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        } else {

            switch (type.getType()) {
                case Type.BIT:
                    while (value.length() < type.getByteLength() * 8) {    // add padding
                        value.append("0");
                    }
                    measurement.put(decodeBinary(value.toString()));
                    break;
                case Type.NUMBER:
                    // Index of the enum value
                    int o = type.ordinal();
                    int currentIndex = (index * 8) + o;
                    boolean hasPrediction = false;
                    if ((value.toString().isBlank() || type.equals(Type.TEMP))
                            && type.shouldCorrect() && averagesIsSet[index]) {

                        int intValue = Integer.parseInt(value.toString());
                        if (type.equals(Type.TEMP)) {
                            float margin = Math.abs(stationAverages[currentIndex]) * .2f;
                            float prediction = stationAverages[currentIndex] * (4f/5f) + intValue * (1f/5f);

                            if(prediction > (stationAverages[currentIndex] + margin)){
                                prediction = stationAverages[currentIndex] + margin;
                            }else if (prediction < (stationAverages[index] - margin)){
                                prediction = stationAverages[currentIndex] - margin;
                            }
                            stationAverages[currentIndex] = prediction;
                            value = new StringBuilder(String.valueOf((int) prediction));
                        } else {
                            stationAverages[currentIndex] = stationAverages[currentIndex] * (31f/30f);
                            value = new StringBuilder(String.valueOf((int) stationAverages[currentIndex]));
                        }
                        hasPrediction = true;
                    }

                    if (!value.toString().isBlank()) {
                        int intValue = Integer.parseInt(value.toString());
                        if (type.shouldCorrect()){
                            if (!averagesIsSet[currentIndex]){
                                stationAverages[currentIndex] = intValue;
                            }else{
                                if (hasPrediction){
                                    stationAverages[currentIndex] = intValue;
                                }else{
                                    stationAverages[currentIndex] = stationAverages[currentIndex] * (4f/5f) + intValue * (1f/5f);
                                }

                                averagesIsSet[currentIndex] = true;
                            }
                        }
                        for (int i = 0; i < type.getByteLength(); i ++){
                            measurement.put((byte) (intValue >> ((type.getByteLength() - 1 + i) * 8)));
                        }

                    } else {
                        // Fill data field with zeros.
                        measurement.put(new byte[type.getByteLength()]);
                    }
                    break;
            }
        }
    }
}