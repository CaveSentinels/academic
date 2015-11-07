/******************************************************************************************************************
* File:WindowBreakSensor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a window break sensor. It polls the message manager for messages corresponding to commands from
* the user and react to them simulating sensing window break.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*   void ReportWindowBreakage(MessageManagerInterface ei)
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;

class WindowBreakSensor
{
	public static void main(String args[])
	{
		String MsgMgrIP;				// Message Manager IP address
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		MessageManagerInterface em = null;// Interface object to the message manager
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		boolean Done = false;			// Loop termination flag
		boolean shouldBreakWindow = false; // Flag to keep track of the window sensor for user based command

        HeartBeater hb = null;          // The heart beater.

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// message manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is on the local machine

				em = new MessageManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {

			// message manager is not on the local system

			MsgMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is NOT on the local machine

				em = new MessageManagerInterface( MsgMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If em is null then the
		// message manager interface was not properly created.


		if (em != null)
		{
            // Prepare the heart beater
            hb = new HeartBeater("Window Break Sensor #1", "A sensor to detect if anyone breaks in through a window.");

			// We create a message window. Note that we place this panel about 1/2 across
			// and 1/3 down the screen

			float WinPosX = 0.5f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Window Break Sensor", WinPosX, WinPosY );

			mw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				mw.WriteMessage("Error:: " + e);

			} // catch

			mw.WriteMessage("Beginning Simulation... ");


			while ( !Done )
			{
				// Get the message queue

				try
				{
					eq = em.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = -100, this means that user has
				// given command to post the sense the window breakage. Note that we 
				// get all the messages at once... there is a 2.5 second delay 
				// between samples,.. so the assumption is that there should only 
				// be a message at most. If there are more, it is the last message 
				// that will effect the output of the sensor.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == -100 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("BREAK")) // Report Window Break
						{
							shouldBreakWindow = true;

						} // if

					} // if

					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

					if ( Msg.GetMessageId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage("\n\nSimulation Stopped. \n");

					} // if

				} // for
				
				// If user command received to break window, report window breakage
				if (shouldBreakWindow) {
					mw.WriteMessage("Window Broken" );
					ReportWindowBreakage(em);
					shouldBreakWindow = false;
				}

                // Before we go to bed, send the heart beat message.
                hb.HeartBeat(em);

				// Here we wait for a 2.5 seconds before we start the next sample

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main



	/***************************************************************************
	* CONCRETE METHOD:: ReportWindowBreakage
	* Purpose: This method reports the window breakage to the 
	* specified message manager. This method assumes an message ID of 100.
	*
	* Arguments: MessageManagerInterface ei - this is the messagemanger interface
	*			 where the message will be posted.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void ReportWindowBreakage(MessageManagerInterface ei)
	{
		// Here we create the message.

		Message msg = new Message( (int) 100, "WINDOW_BREAK");

		// Here we send the message to the message manager.

		try
		{
			ei.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Reporting Window Breakage:: " + e );

		} // catch

	} // ReportWindowBreakage

} // WindowBreakSensor
