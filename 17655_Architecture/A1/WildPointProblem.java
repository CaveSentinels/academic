package A1;

/******************************************************************************************************************
 * File:A1.SinkFilter.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class converts the temperature from Fahrenheit to Celcius and then write the date and converted value of the
 * temperature along with respective IDs to filter's output port.
 *
 *
 * Parameters: 	None
 *
 * Internal Methods: None
 *
 ******************************************************************************************************************/
import java.util.*; // This class is used to interpret time words
import java.text.SimpleDateFormat; // This class is used to format and write time in a string format.
import java.nio.ByteBuffer; // This class is used to convert the double value of temperature to bytes 

public class WildPointProblem extends FilterFramework {

    // override A1.FilterFramework
    void validateNumberOfPorts(){

        if(InputReadPorts.size() > 1){
            throw new IllegalArgumentException("Pressure filter can only have a single input port");
        }

        if(OutputWritePorts.size() > 2){
            throw new IllegalArgumentException("Pressure filter cannot have more than 2 output ports");
        }
    }

    public void run()
    {
        /************************************************************************************
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the terminal.
         *************************************************************************************/

        Calendar TimeStamp = Calendar.getInstance();
        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

        int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
        int IdLength = 4;				// This is the length of IDs in the byte stream

        byte databyte = 0;				// This is the data byte read from the stream
        int bytesread = 0;				// This is the number of bytes read from the stream
        int byteswritten = 0;			// Number of bytes written to the stream.

        long measurement;
        long currentDay = 0; 	// This is the word used to store all measurements - conversions are illustrated.
        int id;							// This is the measurement id
        int i;							// This is a loop counter

        byte[] bytesToBeWritten;
        int pressureCounter=0;
        double currentPressure=0;
        double previousPressure=0;
        double averagePressure=0;
        ArrayList <Double> invalidPressure = new ArrayList<Double>();
        ArrayList <Long> day = new ArrayList<Long>();
        byte m_array[]= new byte[MeasurementLength];
        byte day_arr[] = new byte[MeasurementLength];
        byte pres_arr[] = new byte[MeasurementLength];


        /*************************************************************
         *	First we announce to the world that we are alive...
         **************************************************************/

        System.out.print( "\n" + this.getName() + "::WildPoint Pressure Reading ");
        while (true)
        {
            try
            {
                /***************************************************************************
                 // We know that the first data coming to this filter is going to be an ID and
                 // that it is IdLength long. So we first decommutate the ID bytes.
                 ****************************************************************************/

                id = 0;

                for (i=0; i<IdLength; i++ )
                {
                    databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

                    id = id | (databyte & 0xFF);		// We append the byte on to ID...

                    if (i != IdLength-1)				// If this is not the last byte, then slide the
                    {									// previously appended byte to the left by one byte
                        id = id << 8;					// to make room for the next byte we append to the ID

                    } // if

                    bytesread++;						// Increment the byte count

                } // for

                /****************************************************************************
                 // next 8 bytes are going to be the value
                 //
                 //
                 *****************************************************************************/




                measurement = 0;

                for (i=0; i<MeasurementLength; i++ )
                {
                    databyte = ReadFilterInputPort();
                    measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...
                    m_array[i]=databyte;
                    if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
                    {												// previously appended byte to the left by one byte
                        measurement = measurement << 8;				// to make room for the next byte we append to the
                        // measurement
                    } // if

                    bytesread++;									// Increment the byte count

                } // if




                if(id == 0){
                    for(int index=0; index<8;index++)
                        day_arr[index] = m_array[index];
                    currentDay= measurement;
                }

                else if (id == 3){

                    for(int index=0; index<8;index++)
                        pres_arr[index] = m_array[index];
                    currentPressure = Double.longBitsToDouble(measurement);
                    // System.out.println(currentPressure);
                    pressureCounter++;
                    if(pressureCounter == 1)
                    {

                        if(currentPressure > 0 ){
                            if (OutputWritePorts.size() > 1){
                                //write to the valid file with currentday & current pressure
                                // System.out.println("Date" +day+ "validpressure1 " +currentPressure);
                                ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                                // Write the bytes to the filter's output port
                                for (i = 0; i < IdLength; i++) {
                                    WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(1));
                                    byteswritten++;}
                                for(i=0;i<MeasurementLength;i++){
                                    WriteFilterOutputPort(day_arr[i], OutputWritePorts.get(1));}
                                //2 loops
                                ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                                // Write the bytes to the filter's output port
                                for (i = 0; i < IdLength; i++) {
                                    WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(1));
                                    byteswritten++;}
                                for(i=0;i<MeasurementLength;i++){
                                    WriteFilterOutputPort(pres_arr[i], OutputWritePorts.get(1));
                                }
                            }
                            previousPressure=currentPressure;
                            //reverse 0 by 1

                        } else {
                            invalidPressure.add(currentPressure);
                            day.add(currentDay);
                            //call a method to write to invalid file
                            // System.out.println("Date" +day+ "invalidpressure1 " +currentPressure);
                            ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                            bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                            // Write the bytes to the filter's output port
                            for (i = 0; i < IdLength; i++) {
                                WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(0));
                                byteswritten++;}
                            for(i=0;i<MeasurementLength;i++){
                                WriteFilterOutputPort(day_arr[i], OutputWritePorts.get(0));
                            }
                            ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                            bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                            // Write the bytes to the filter's output port
                            for (i = 0; i < IdLength; i++) {
                                WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(0));
                                byteswritten++;}
                            //check if the port is there
                            for(i=0;i<MeasurementLength;i++){
                                WriteFilterOutputPort(pres_arr[i], OutputWritePorts.get(0));
                            }


                        }
                    } else {

                        if(currentPressure < 0) // invalid entry
                        {
                            //add to invalid list & file
                            invalidPressure.add(currentPressure);
                            day.add(currentDay);
                            // System.out.println("Date" +day+ "invalidpressure2 " +currentPressure);
                            ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                            bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                            // Write the bytes to the filter's output port
                            for (i = 0; i < IdLength; i++) {
                                WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(0));
                                byteswritten++;}
                            for(i=0;i<MeasurementLength;i++){
                                WriteFilterOutputPort(day_arr[i], OutputWritePorts.get(0));}
                            ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                            bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                            // Write the bytes to the filter's output port
                            for (i = 0; i < IdLength; i++) {
                                WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(0));
                                byteswritten++;}
                            for(i=0;i<MeasurementLength;i++){
                                WriteFilterOutputPort(pres_arr[i], OutputWritePorts.get(0));
                            }

                        }
                        else if (Math.abs(previousPressure-currentPressure)<10) // valid entry
                        {
                            currentPressure = Double.longBitsToDouble(measurement);
                            previousPressure=currentPressure;

                            //if the invalid list is empty, then write currentPressure & day directly to the output file
                            if(invalidPressure.isEmpty() && OutputWritePorts.size() > 1){
                                // System.out.println("Date" +day+ "validpressure2 " +currentPressure);
                                ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                                // Write the bytes to the filter's output port
                                for (i = 0; i < IdLength; i++) {
                                    WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(1));
                                    byteswritten++;}
                                for(i=0;i<MeasurementLength;i++){
                                    WriteFilterOutputPort(day_arr[i], OutputWritePorts.get(1));}
                                ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                                // Write the bytes to the filter's output port
                                for (i = 0; i < IdLength; i++) {
                                    WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(1));
                                    byteswritten++;}
                                for(i=0;i<MeasurementLength;i++){
                                    WriteFilterOutputPort(pres_arr[i], OutputWritePorts.get(1));
                                }
                            }
                            //if invalid list is not empty, write correct pressure(average) with each day in invalid days list & empty the arraylist
                            else{

                                averagePressure=(currentPressure+previousPressure)/2;

                                if (OutputWritePorts.size() > 1) {
                                    //convert double averagePressure to byte array so that it can be written to the output port
                                    byte[] pressure = ByteBuffer.allocate(MeasurementLength).putDouble(averagePressure).array();
                                    Iterator iterator = day.iterator();
                                    while (iterator.hasNext()) {
                                        byte[] daytobewritten = ByteBuffer.allocate(MeasurementLength).putLong((Long) iterator.next()).array();
                                        ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                        bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                                        // Write the bytes to the filter's output port
                                        for (i = 0; i < IdLength; i++) {
                                            WriteFilterOutputPort(bytesToBeWritten[i], OutputWritePorts.get(1));
                                            byteswritten++;
                                        }
                                        for (i = 0; i < MeasurementLength; i++) {
                                            WriteFilterOutputPort(daytobewritten[i], OutputWritePorts.get(1));
                                        }
                                        ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                        bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                                        // Write the bytes to the filter's output port
                                        for (i = 0; i < IdLength; i++) {
                                            WriteFilterOutputPort(bytesToBeWritten[i], OutputWritePorts.get(1));
                                            byteswritten++;
                                        }
                                        for (i = 0; i < MeasurementLength; i++) {
                                            WriteFilterOutputPort(pressure[i], OutputWritePorts.get(1));
                                        }
                                        //    System.out.println("day"+iterator.next()+"validpressure3 "+averagePressure);
                                    }
                                }
                                //empty the arraylist of pressure & day
                                previousPressure=currentPressure;
                                day.clear();
                                invalidPressure.clear();

                                if (OutputWritePorts.size() > 1) {
                                    //now write the current pressure
                                    ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                    bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                                    // Write the bytes to the filter's output port
                                    for (i = 0; i < IdLength; i++) {
                                        WriteFilterOutputPort(bytesToBeWritten[i], OutputWritePorts.get(1));
                                        byteswritten++;
                                    }
                                    for (i = 0; i < MeasurementLength; i++) {
                                        WriteFilterOutputPort(day_arr[i], OutputWritePorts.get(1));
                                    }
                                    //2 loops
                                    ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                                    bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                                    // Write the bytes to the filter's output port
                                    for (i = 0; i < IdLength; i++) {
                                        WriteFilterOutputPort(bytesToBeWritten[i], OutputWritePorts.get(1));
                                        byteswritten++;
                                    }
                                    for (i = 0; i < MeasurementLength; i++) {
                                        WriteFilterOutputPort(pres_arr[i], OutputWritePorts.get(1));
                                    }
                                }


                            }


                        }
                        else // if diff is greater than 10 = invalid
                        {
                            // save the current pressure to an invalid array list & to the invalid file with the current day value
                            invalidPressure.add(currentPressure);
                            day.add(currentDay);
                            // System.out.println("Date" +day+ "invalidpressure3 " +currentPressure);
                            ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                            bytesToBeWritten = buffer.putInt(0).array(); // Create the array from ByteBuffer

                            // Write the bytes to the filter's output port
                            for (i = 0; i < IdLength; i++) {
                                WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(0));
                                byteswritten++;}
                            for(i=0;i<MeasurementLength;i++){
                                WriteFilterOutputPort(day_arr[i], OutputWritePorts.get(0));}
                            ByteBuffer buffer1 = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
                            bytesToBeWritten = buffer1.putInt(id).array(); // Create the array from ByteBuffer

                            // Write the bytes to the filter's output port
                            for (i = 0; i < IdLength; i++) {
                                WriteFilterOutputPort(bytesToBeWritten[i],OutputWritePorts.get(0));
                                byteswritten++;}
                            for(i=0;i<MeasurementLength;i++){
                                WriteFilterOutputPort(pres_arr[i], OutputWritePorts.get(0));
                            }
                        }
                    }




                }

            }
            catch (EndOfStreamException e)
            {
                ClosePorts();
                System.out.print( "\n" + this.getName() + "::PressureExiting; bytes read: " + bytesread );
                //System.out.print( "\n Number of temperature values read in A1.TempConversion Filter" + tempCount );
                break;

            } // catch
        }
    }
}
