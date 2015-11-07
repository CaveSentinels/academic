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
* This class converts the altitude from Feet to Meters and then write the date and converted value of the altitude
* along with respective IDs to filter's output port.
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.*;						// This class is used to interpret time words
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.
import java.nio.ByteBuffer;				// This class is used to convert the double value of altitude to bytes 

public class AltitudeConversion extends FilterFramework
{
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

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter
		
		byte[] bytesToBeWritten;		// This array holds the bytes that are to be written on the ouput port
		double feet;					// This is altitude in feet
		double meters;					// This is altitude in meters
		
		//int tempCount = 0;

		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::A1.AltitudeConversion Reading ");

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
				// ID 0 means that it is time data, so, next 8 bytes are going to be the time
				// data, just read them and write them out to the filter's output port along
				// with the ID (ID = 0)
				*****************************************************************************/
				
				if (id == 0) {
					ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
					bytesToBeWritten = buffer.putInt(id).array(); // Create the array from ByteBuffer
					
					// Write the bytes to the filter's output port
					for (i = 0; i < IdLength; i++) {
						WriteFilterOutputPort(bytesToBeWritten[i]);
						byteswritten++;
					}
					
					// Read the next 8 bytes from the input port and write them as it is on
					// the output port
					for (i = 0; i < MeasurementLength; i++) {
						databyte = ReadFilterInputPort();
						bytesread++;
						WriteFilterOutputPort(databyte);
						byteswritten++;
					}
				}	
				else {
					measurement = 0;

					for (i = 0; i < MeasurementLength; i++) {
						// Read the data byte from the input port
						databyte = ReadFilterInputPort();
						
						// Append the byte on to measurement...
						measurement = measurement | (databyte & 0xFF);
						
						if (i != MeasurementLength-1) {	
							// If this is not the last byte, then slide
							// the previously appended byte to the left by 
							// one byte	to make room for the next byte we 
							// append to the measurement
							measurement = measurement << 8;				
																		
						}
						// Increment the byte count
						bytesread++;									
					}
					
					/****************************************************************************
					// ID 2 means that it is altitude data, so, next 8 bytes are going to be the
					// altitude, read them, convert the value to meters and then finally convert
					// it to bytes and write it out to the filter's output along with the corresponding
					// ID.
					*****************************************************************************/
					
					if (id == 2) {
						// Write the ID of altitude to the output port
						ByteBuffer buffer = ByteBuffer.allocate(IdLength); // Create the ByteBuffer of legth 4
						bytesToBeWritten = buffer.putInt(id).array(); // Create the array from ByteBuffer
					
						// Write the bytes to the filter's output port
						for (i = 0; i < IdLength; i++) {
							WriteFilterOutputPort(bytesToBeWritten[i]);
							byteswritten++;
						}
						//tempCount++;
					
						// Convert the altitude to meters
						feet = Double.longBitsToDouble(measurement);
						meters = feet * 0.3048;
					
						// Convert the altitude to the Bytes and write it to the filter's output port
						buffer = ByteBuffer.allocate(MeasurementLength); // Create the ByteBuffer of legth 8
						bytesToBeWritten = buffer.putLong(Double.doubleToLongBits(meters)).array(); // Create the array from ByteBuffer
					
						// Write the bytes to the filter's output port
						for (i = 0; i < MeasurementLength; i++) {
							WriteFilterOutputPort(bytesToBeWritten[i]);
							byteswritten++;
						}
					}
				}

			} // try

			/*******************************************************************************
			*	The EndOfStreamExeception below is thrown when you reach end of the input
			*	stream (duh). At this point, the filter ports are closed and a message is
			*	written letting the user know what is going on.
			********************************************************************************/

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::A1.AltitudeConversion Exiting; bytes read: " + bytesread );
				//System.out.print( "\n Number of altitude values read in Altitude Filter" + tempCount );
				break;

			} // catch

		} // while

   } // run

} // SingFilter