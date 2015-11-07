package A1;

import java.io.PipedOutputStream;
import java.lang.*;

/**
 *  @brief: The filter for filtering the pressure data that are less than 10K.
 */
public class Filter_LessThan10KAltitude extends FilterFramework
{
    // Data-frame-related 
    public static final int DATA_ID_LENGTH_IN_BYTE = 4;
    public static final int DATA_VALUE_LENGTH_IN_BYTE = 8;
    public static final int DATA_FRAME_FIELD_NUM = 6;
    public static final int DATA_FRAME_LENGTH_IN_BYTE = (DATA_ID_LENGTH_IN_BYTE +
            DATA_VALUE_LENGTH_IN_BYTE) * DATA_FRAME_FIELD_NUM;
    public static final int DATA_ALTITUDE_FRAME_LENGTH_IN_BYTE = (DATA_ID_LENGTH_IN_BYTE +
            DATA_VALUE_LENGTH_IN_BYTE) * 2;

    // Measurement IDs.
    public static final int ID_TIME = 0;
    public static final int ID_VELOCITY = 1;
    public static final int ID_ALTITUDE = 2;
    public static final int ID_PRESSURE = 3;
    public static final int ID_TEMPERATURE = 4;
    public static final int ID_ATTITUDE = 5;

    // Constants: Filter-specific
    private static final double ALTITUDE_FILTERING_LOWER_LIMIT = 10000.0;


    // override A1.FilterFramework
    void validateNumberOfPorts(){

        if(InputReadPorts.size() > 1){
            throw new IllegalArgumentException("Filter_LessThan10kAltitude filter can only have a single input port");
        }

        if(OutputWritePorts.size() > 2){
            throw new IllegalArgumentException("Filter_LessThan10kAltitude filter cannot have more than 2 output ports");
        }
    }


    /**
     * @brief: Convert 4 bytes to an int.
     * @param: [in] data: The byte array.
     * @param: [in] startIndex: The starting index in data to convert from.
     *      The following bytes will be converted to an int:
     *      data[startIndex], data[startIndex+1], data[startIndex+2], data[startIndex+3]
     * @return: int: The result int.
     */
    public static int Convert4BytesToInt(byte[] data, int startIndex)
    {
        int result = 0;

        if (data.length - startIndex < 4)
        {
            // TODO: Throw exception.
            return result;
        }

        int i = 0;
        while (i < 3)
        {
            result = result | (data[startIndex+i] & 0xFF);
            // Move 8 bits left to make room for the next byte. We only do
            //  this to the first 3 bytes.
            result = result << 8;
            ++i;
        }

        result = result | (data[startIndex+i] & 0xFF);

        return result;
    }

    /**
     * @brief: Convert 8 bytes to a long.
     * @param: [in] data: The byte array.
     * @param: [in] startIndex: The starting index in data to convert from.
     *      The following bytes will be converted to a long:
     *      data[startIndex], data[startIndex+1], data[startIndex+2], data[startIndex+3],
     *      data[startIndex+4], data[startIndex+5], data[startIndex+6], data[startIndex+7]
     * @return: long: The result long.
     */
    public static long Convert8BytesToLong(byte[] data, int startIndex)
    {
        long result = 0;

        if (data.length - startIndex < 8)
        {
            // TODO: Throw exception.
            return result;
        }

        int i = 0;
        while (i < 7)
        {
            result = result | (data[startIndex+i] & 0xFF);
            // Move 8 bits left to make room for the next byte. We only do
            //  this to the first 7 bytes.
            result = result << 8;
            ++i;
        }

        result = result | (data[startIndex+i] & 0xFF);

        return result;
    }

    /**
     * @brief: Convert 8 bytes to a double.
     * @param: [in] data: The byte array.
     * @param: [in] startIndex: The starting index in data to convert from.
     *      The following bytes will be converted to a double:
     *      data[startIndex], data[startIndex+1], data[startIndex+2], data[startIndex+3],
     *      data[startIndex+4], data[startIndex+5], data[startIndex+6], data[startIndex+7]
     * @return: double: The result double.
     */
    public static double Convert8BytesToDouble(byte[] data, int startIndex)
    {
        double result = 0.0;

        if (data.length - startIndex < 8)
        {
            // TODO: Throw exception.
            return result;
        }

        long tmp = Convert8BytesToLong(data, startIndex);

        result = Double.longBitsToDouble(tmp);

        return result;
    }

    /**
     * @brief: Try to read one entire data frame from the input stream pipe.
     * @param: [in] frameLength: The length of a data frame.
     * @param: [out] frame: The data read from input stream as bytes.
     * @return: boolean: Whether an entire frame is read or not.
     *      true: An entire data frame has been read and returned.
     *      false: Only a partial of data frame is returned because there is
     *          no more data available. Or because invalid parameters.
     */
    private boolean readOneFrame(int frameLength, byte[] frame)
    {
        boolean oneFrameRead = false;    // Whether one frame is read or not.
        byte datum = 0;     // One byte of datum

        // Validate parameters
        if (frameLength <= 0 || frame == null || frame.length < frameLength)
        {
            return false;   // TODO: Should I throw exception here?
        }

        // Clear the frame
        // TODO: Is there a more elegant way to do so??
        for (int i = 0; i < frameLength; ++i)
        {
            frame[i] = 0;
        }

        // Start reading the byte stream from the pipe.
        try
        {
            for (int i = 0; i < frameLength; ++i)
            {
                datum = ReadFilterInputPort();
                frame[i] = datum;
            }

            // When we finish reading frameLength bytes, that means we have succeeded
            // in reading a whole frame.
            oneFrameRead = true;
        }
        catch (EndOfStreamException e)
        {
            // We don't have any more bytes to read. We have to return.
            ClosePorts();
            oneFrameRead = false;
        }

        return oneFrameRead;
    }

    /**
     * @brief: Write one data frame to the output stream pipe.
     * @param: [in] frameLength: The length of the frame to write.
     * @param: [in] frame: The data frame to be written to the output stream.
     * @param: [in] out: The out port to be written into.
     * @return: boolean: Whether the data frame is written successfully or not.
     *      true: The data frame is written successfully.
     *      false: The data frame is not written successfully because of
     *          invalid parameters.
     */
    private boolean writeOneFrame(int frameLength, byte[] frame, PipedOutputStream out)
    {
        // Validate parameters.
        if (frameLength > frame.length)
        {
            return false;
        }

        // Write frame to output stream.
        for (int i = 0; i < frameLength; ++i)
        {
            WriteFilterOutputPort(frame[i], out);
        }

        return true;
    }

    /**
     * @brief: The Thread execution method.
     * @param: N/A
     * @return: N/A
     */
    public void run()
    {
        // The buffer to store the current data frame.
        byte[] frame = new byte[DATA_FRAME_LENGTH_IN_BYTE];
        byte[] altitude_frame = new byte[DATA_ALTITUDE_FRAME_LENGTH_IN_BYTE];

        System.out.println(this.getName() + "::A1.Filter_LessThan10KAltitude Starts Reading...");

        while (true)
        {
            // Clear the buffer.
            for (int i = 0; i < DATA_FRAME_LENGTH_IN_BYTE; ++i)
            {
                frame[i] = (byte)0;
            }
            for (int i = 0; i < DATA_ALTITUDE_FRAME_LENGTH_IN_BYTE; ++i)
            {
                altitude_frame[i] = (byte)0;
            }

            // The altitude value of the current data frame.
            double altitude = 0.0;

            // If one entire frame is read from the input stream or not.
            boolean readSuccessfully = false;

            // Step 1: Read an entire data frame.
            readSuccessfully = readOneFrame(DATA_FRAME_LENGTH_IN_BYTE, frame);
            if (!readSuccessfully)
            {
                // We don't have any data to process. Just return.
                System.out.println(this.getName() + ": No more data to process. Exiting...");
                break;  // while(true)
            }

            // Step 2: Parse the data frame.
            int index = 0;    // The index of byte we are currently processing.
            int altitude_frame_index = 0;
            while (index < DATA_FRAME_LENGTH_IN_BYTE)
            {
                int id = Convert4BytesToInt(frame, index);

                if (id == ID_TIME)
                {
                    // Copy to the altitude_frame in case it is less than 10K.
                    for (int j = 0; j < DATA_ID_LENGTH_IN_BYTE + DATA_VALUE_LENGTH_IN_BYTE; ++j)
                        altitude_frame[altitude_frame_index+j] = frame[index+j];
                    altitude_frame_index += DATA_ID_LENGTH_IN_BYTE + DATA_VALUE_LENGTH_IN_BYTE;

                    index += DATA_ID_LENGTH_IN_BYTE + DATA_VALUE_LENGTH_IN_BYTE;
                }
                else if (id == ID_ALTITUDE)
                {
                    // Copy to the altitude_frame in case it is less than 10K.
                    for (int j = 0; j < DATA_ID_LENGTH_IN_BYTE + DATA_VALUE_LENGTH_IN_BYTE; ++j)
                        altitude_frame[altitude_frame_index+j] = frame[index+j];
                    altitude_frame_index += DATA_ID_LENGTH_IN_BYTE + DATA_VALUE_LENGTH_IN_BYTE;

                    // Move the index to the first byte for value.
                    index += DATA_ID_LENGTH_IN_BYTE;

                    altitude = Convert8BytesToDouble(frame, index);

                    // OK, we've found the value we want. Now break.
                    break;  // while (index < DATA_FRAME_LENGTH_IN_BYTE)
                }
                else
                {
                    // No, this is not the value we want to manipulate.
                    // Move the index to the next measurement.
                    index += DATA_ID_LENGTH_IN_BYTE + DATA_VALUE_LENGTH_IN_BYTE;
                }
            }

            // Step 3: Filter out the data frame with altitude < 10K
            if (Double.compare(altitude, ALTITUDE_FILTERING_LOWER_LIMIT) < 0)
            {
                // This frame should be filtered out. So we continue to the next frame
                // without writing this frame to the output stream pipe.
                writeOneFrame(DATA_ALTITUDE_FRAME_LENGTH_IN_BYTE,
                        altitude_frame,
                        OutputWritePorts.get(0)
                );
            }
            else
            {
                // Step 4: If a data frame is retained, write it to the output stream.
                writeOneFrame(DATA_FRAME_LENGTH_IN_BYTE,
                        frame,
                        OutputWritePorts.get(1)
                );
            }

        } // while

        System.out.println(this.getName() + "::A1.Filter_LessThan10KAltitude Finishes Reading...");

    } // run

}
