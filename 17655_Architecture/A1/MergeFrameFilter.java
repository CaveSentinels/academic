package A1;

import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.util.*;

/******************************************************************************************************************
* File:A1.MergeFrameFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/



public class MergeFrameFilter extends FilterFramework
{
    class ObjectComparator implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    }


    public void run()
    {
        /************************************************************************************
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the terminal.
         *************************************************************************************/

        int numOfInputPorts = InputReadPorts.size();

        int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
        int IdLength = 4;				// This is the length of IDs in the byte stream

        byte databyte = 0;				// This is the data byte read from the stream
        int bytesread = 0;				// This is the number of bytes read from the stream


        // These will hold the byte count, id and measurement values for each input pipe separately
        Map<PipedInputStream, Integer> byteCounter = new HashMap<PipedInputStream, Integer>();
        Map<PipedInputStream, Integer> id = new HashMap<PipedInputStream, Integer>();
        Map<PipedInputStream, Long> measurement = new HashMap<PipedInputStream, Long>();

        /************************************************************************************
         *	currentFrame is used to hold the current frame of data read from each input pipe.
         * 	The frame is assumed to be complete when reaching ID=0, which belongs to the next frame)
         *	When the whole frame is read, it is pushed to toWriteFrame.
         *  We are using TreeMap to make sure readings sorted according to input port
         *************************************************************************************/
        Map<PipedInputStream, HashMap<Integer, Long>> currentFrame = new TreeMap<PipedInputStream, HashMap<Integer, Long>>(new ObjectComparator());

        /************************************************************************************
         *	toWriteFrame holds completed frames from each pipes and uses the date value as keys
         *  It stores the frames till all frames from all input ports for a specific date are read
         *  Then it will push the values to the output sink
         *************************************************************************************/
        Map<Long, Map<PipedInputStream, HashMap<Integer, Long>>> toWriteFrame = new TreeMap<Long, Map<PipedInputStream, HashMap<Integer, Long>>>(new ObjectComparator());

        /*************************************************************
         *	First we announce to the world that we are alive...
         **************************************************************/

        System.out.print( "\n" + this.getName() + "::Merge Reading ");

        while (true)
        {
            try
            {


                databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...
                bytesread++;						// Increment the byte count

                if (!byteCounter.containsKey(currentInputPort)) {
                    byteCounter.put(currentInputPort, 0);
                    id.put(currentInputPort, 0);
                    measurement.put(currentInputPort, 0L);
                }

                /***************************************************************************
                 // We know that the first data coming to this filter is going to be an ID and
                 // that it is IdLength long. So we first decommutate the ID bytes.
                 ****************************************************************************/

                if (byteCounter.get(currentInputPort) < IdLength){
                    id.put(currentInputPort, id.get(currentInputPort) | (databyte & 0xFF));

                    if (byteCounter.get(currentInputPort) != IdLength-1){
                        id.put(currentInputPort, id.get(currentInputPort) << 8);
                    }

                    byteCounter.put(currentInputPort, byteCounter.get(currentInputPort) + 1);
                }


                /****************************************************************************
                 // Here we read measurements. All measurement data is read as a stream of bytes
                 // and stored as a long value. This permits us to do bitwise manipulation that
                 // is neccesary to convert the byte stream into data words. Note that bitwise
                 // manipulation is not permitted on any kind of floating point types in Java.
                 // If the id = 0 then this is a time value and is therefore a long value - no
                 // problem. However, if the id is something other than 0, then the bits in the
                 // long value is really of type double and we need to convert the value using
                 // Double.longBitsToDouble(long val) to do the conversion which is illustrated.
                 // below.
                 *****************************************************************************/

                else if (byteCounter.get(currentInputPort) < (IdLength + MeasurementLength)){
                    measurement.put(currentInputPort, measurement.get(currentInputPort) | (databyte & 0xFF));

                    if (byteCounter.get(currentInputPort) != (IdLength + MeasurementLength-1)){
                        measurement.put(currentInputPort, measurement.get(currentInputPort) << 8);
                    }

                    byteCounter.put(currentInputPort, byteCounter.get(currentInputPort) + 1);
                }


                // If ID and Measurement have been read fully
                if (byteCounter.get(currentInputPort) == (IdLength + MeasurementLength)) {

                    // If we are reading the date, then we are starting a new frame
                    if (id.get(currentInputPort) == 0){

                        // If this is not the first frame, then we need to push the frame from 'currentFrame' to 'toWriteFrame'
                        if (currentFrame.containsKey(currentInputPort)){

                            // The date of the previous frame
                            long date = currentFrame.get(currentInputPort).get(0);

                            if (!toWriteFrame.containsKey(date)){
                                LinkedHashMap<PipedInputStream, HashMap<Integer, Long>> frame = new LinkedHashMap<PipedInputStream, HashMap<Integer, Long>>();
                                frame.put(currentInputPort, currentFrame.get(currentInputPort));

                                toWriteFrame.put(date, frame);

                            } else {
                                toWriteFrame.get(date).put(currentInputPort, currentFrame.get(currentInputPort));
                            }


                            // If all ports have read the frame related to this date, then we write to the output port
                            if (toWriteFrame.get(date).size() == numOfInputPorts){

                                flushStream(date, toWriteFrame);

                                toWriteFrame.remove(date);
                            }
                        }

                        LinkedHashMap<Integer, Long> reading = new LinkedHashMap<Integer, Long>();
                        reading.put(id.get(currentInputPort), measurement.get(currentInputPort));

                        currentFrame.put(currentInputPort, reading);
                    }

                    else {
                        currentFrame.get(currentInputPort).put(id.get(currentInputPort), measurement.get(currentInputPort));


                    }

                    // Reset the byte counter, id and measurement for this input port
                    byteCounter.put(currentInputPort, 0);
                    id.put(currentInputPort, 0);
                    measurement.put(currentInputPort,0L);
                }



            } // try

            /*******************************************************************************
             *	The EndOfStreamExeception below is thrown when you reach end of the input
             *	stream (duh). At this point, the filter ports are closed and a message is
             *	written letting the user know what is going on.
             ********************************************************************************/

            catch (EndOfStreamException e)
            {
                // Flushing remaining frames
                for (int i = 0; i < numOfInputPorts; i++){

                    currentInputPort = InputReadPorts.get(i);
                    if (currentFrame.containsKey(currentInputPort)){
                        long date = currentFrame.get(currentInputPort).get(0);

                        if (!toWriteFrame.containsKey(date)){
                            LinkedHashMap<PipedInputStream, HashMap<Integer, Long>> frame = new LinkedHashMap<PipedInputStream, HashMap<Integer, Long>>();
                            frame.put(currentInputPort, currentFrame.get(currentInputPort));

                            toWriteFrame.put(date, frame);

                        } else {
                            toWriteFrame.get(date).put(currentInputPort, currentFrame.get(currentInputPort));
                        }
                    }
                }

                if(toWriteFrame.size() > 0){
                    for (long date : toWriteFrame.keySet()){
                        flushStream(date, toWriteFrame);
                    }
                }

                ClosePorts();
                System.out.print( "\n" + this.getName() + "::Merge Exiting; bytes read: " + bytesread );
                break;

            } // catch

        } // while

    } // run


    void flushStream(long date, Map<Long, Map<PipedInputStream, HashMap<Integer, Long>>> toWriteFrame){

        int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
        int IdLength = 4;				// This is the length of IDs in the byte stream

        byte[] bytesToBeWritten;		// This array holds the bytes that are to be written on the ouput port
        ByteBuffer byteBuffer;

        int i;							// This is a loop counter


        // First, we write the ID of date to the output port
        byteBuffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of length 4
        bytesToBeWritten = byteBuffer.putInt(0).array(); // Create the array from ByteBuffer

        for (i = 0; i < IdLength; i++) {
            WriteFilterOutputPort(bytesToBeWritten[i]);
        }

        // Second, we write the measurement of date to the output port
        byteBuffer = ByteBuffer.allocate(MeasurementLength); // Create the ByteBuffer of length 8
        bytesToBeWritten = byteBuffer.putLong(date).array(); // Create the array from ByteBuffer

        for (i = 0; i < MeasurementLength; i++) {
            WriteFilterOutputPort(bytesToBeWritten[i]);
        }


        // Then we write the remaining readings to the output port. Notice the loop starts from 1
        // to escape the

        /***************************************************************************
         // Now we write the remaining readings to the output port.
         // We loop from 1 till 5 looking for measurement IDs in the readings
         // We start from and not from zero to escape the date which is written already
         ****************************************************************************/

        for (i=1; i <=5; i++ ){

            for(Map.Entry<PipedInputStream, HashMap<Integer, Long>> entry: toWriteFrame.get(date).entrySet()){
                if (entry.getValue().containsKey(i)){


                    // Write the ID of measurement to the output port
                    byteBuffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of length 4
                    bytesToBeWritten = byteBuffer.putInt(i).array(); // Create the array from ByteBuffer

                    for (int j = 0; j < IdLength; j++) {
                        WriteFilterOutputPort(bytesToBeWritten[j]);
                    }



                    // Write the measurement to the output port
                    byteBuffer = ByteBuffer.allocate(MeasurementLength); // Create the ByteBuffer of length 8
                    bytesToBeWritten = byteBuffer.putLong(entry.getValue().get(i)).array(); // Create the array from ByteBuffer

                    for (int j = 0; j < MeasurementLength; j++) {
                        WriteFilterOutputPort(bytesToBeWritten[j]);
                    }

                }
            }
        }

    }

} // A1.MergeFrameFilter