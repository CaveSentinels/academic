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



public class MergeStreamFilter extends FilterFramework
{

    /************************************************************************************
     *	dateToFrame holds completed frames from each pipes and uses the date value as keys
     *  it uses TreeMap to have the frames sorted by date
     *  this acts as a buffer till at least a single frame is read from port to decide which
     *  frame is oldest
     *************************************************************************************/
    private Map<Long, LinkedHashMap<Integer, Long>> dateToFrame = new TreeMap<Long, LinkedHashMap<Integer, Long>>();


    /************************************************************************************
     *	dateToPort is used to help dateToFrame to map each reading to its source port
     *************************************************************************************/
    private Map<Long, PipedInputStream> dateToPort = new HashMap<Long, PipedInputStream>();


    /************************************************************************************
     *	This set holds the input ports that frames have been read from so far.
     *  Once we read frames from all ports, then we can align frames and flush them to output port
     *  Then, this set will be reset and we start reading more frames
     *************************************************************************************/
    private Set<PipedInputStream> readFromPorts = new HashSet<PipedInputStream>();


    private int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
    private int IdLength = 4;				// This is the length of IDs in the byte stream



    public void run()
    {
        /************************************************************************************
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the terminal.
         *************************************************************************************/

        int numOfInputPorts = InputReadPorts.size();



        byte databyte = 0;				// This is the data byte read from the stream
        int bytesread = 0;				// This is the number of bytes read from the stream



        int i;							// This is a loop counter

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
        Map<PipedInputStream, LinkedHashMap<Integer, Long>> currentFrame = new HashMap<PipedInputStream, LinkedHashMap<Integer, Long>>();



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

                            dateToFrame.put(date, currentFrame.get(currentInputPort));
                            dateToPort.put(date, currentInputPort);

                            readFromPorts.add(currentInputPort);    // Adding the current input port to the set of ports that we read from so far



                            // If the filter read at least a single frame from each input ports
                            if (readFromPorts.size() == numOfInputPorts){
                                flushFrames(false);
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
                // If there are still frames store in the buffer to be written
                if (!dateToFrame.isEmpty()){
                    flushFrames(true);
                }

                ClosePorts();
                System.out.print( "\n" + this.getName() + "::Merge Exiting; bytes read: " + bytesread );
                break;

            } // catch

        } // while

    } // run



    /***************************************************************************
     // forceFlush is used to request all frames in the dateToFram buffer to be written
     // to the output, without consedring there might be younger data coming from differnt
     // input streams.
     // It is used to flush the remaining data in the dateToFram when the ports are empty
     ****************************************************************************/
    private void flushFrames(boolean forceFlush) {

        byte[] bytesToBeWritten;		// This array holds the bytes that are to be written on the ouput port
        ByteBuffer byteBuffer;
        PipedInputStream portToOutput = null;


        /***************************************************************************
         // Iterate over sorted readings according to date
         // Key: datetime, Value: data frame
         // Get the oldest data frame and output the oldest readings from that specific port
         // When the oldest remaining reading is from another port, then we stop and loop again
         ****************************************************************************/
        Iterator itr = dateToFrame.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Long, LinkedHashMap<Integer, Long>> entry = (Map.Entry) itr.next();
            boolean writeFrame = forceFlush;
            long date_key = entry.getKey();

            // This is our first time and this is the oldest frame
            if (portToOutput == null) {
                portToOutput = dateToPort.get(date_key);
                writeFrame = true;
            }
            // If reading came from the same previous port, then we know there is no other frame is older
            else if (dateToPort.get(date_key) == portToOutput) {
                writeFrame = true;
            }
            // If no more readings from the previous port remains in the dateToPort buffer
            else if (dateToPort.containsValue(portToOutput)) {
                writeFrame = true;
            }

            // If true, then we flush the frame to the output port
            if (writeFrame) {
                for (Map.Entry<Integer, Long> reading : entry.getValue().entrySet()) {
                    // Write the ID of measurement to the output port
                    byteBuffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of length 4
                    bytesToBeWritten = byteBuffer.putInt(reading.getKey()).array(); // Create the array from ByteBuffer

                    for (int j = 0; j < IdLength; j++) {
                        WriteFilterOutputPort(bytesToBeWritten[j]);
                    }


                    // Write the measurement to the output port
                    byteBuffer = ByteBuffer.allocate(MeasurementLength); // Create the ByteBuffer of length 8
                    bytesToBeWritten = byteBuffer.putLong(reading.getValue()).array(); // Create the array from ByteBuffer

                    for (int j = 0; j < MeasurementLength; j++) {
                        WriteFilterOutputPort(bytesToBeWritten[j]);
                    }
                }

                //dateToFrame.remove()
                itr.remove();
                dateToPort.remove(date_key);

                if (!dateToPort.containsValue(portToOutput)) {
                    readFromPorts.remove(portToOutput);
                }

            }
        }
    }

} // A1.MergeFrameFilter