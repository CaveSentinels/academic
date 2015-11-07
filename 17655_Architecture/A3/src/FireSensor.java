/******************************************************************************************************************
* File:FireSensor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a fire sensor. It polls the message manager for messages corresponding to commands from
* the user and react to them simulating sensing door break.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*   void ReportFire(MessageManagerInterface ei)
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;

class FireSensor
{
	public static void main(String args[])
	{
		String MsgMgrIP;				// Message Manager IP address
		Message Msg = null;				// Message object
		MessageQueue messageQueue = null;			// Message Queue
		MessageManagerInterface mmInterfaceObject = null;// Interface object to the message manager
		int	Delay = 2500;				// The loop delay (2.5 seconds)
		boolean Done = false;			// Loop termination flag
		boolean shouldSetFire = false; // Flag to keep track of the fire sensor for user based command

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

				mmInterfaceObject = new MessageManagerInterface();
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

				mmInterfaceObject = new MessageManagerInterface( MsgMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

 		
		// Here we check to see if registration worked. If em is null then the
		// message manager interface was not properly created.
		if (mmInterfaceObject != null)
		{
			// Prepare the heart beater
			hb = new HeartBeater("Fire Sensor #1", "A sensor to detect if there is fire.");

			// We create a message window. Note that we place this panel about 1/2 across
			// and 1/3 down the screen

			float WinPosX = 0.5f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow messageWindow = new MessageWindow("Fire Sensor", WinPosX, WinPosY );

			messageWindow.WriteMessage("Registered with the message manager." );

	    	try
	    	{
	    		messageWindow.WriteMessage("   Participant id: " + mmInterfaceObject.GetMyId() );
	    		messageWindow.WriteMessage("   Registration Time: " + mmInterfaceObject.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
	    		messageWindow.WriteMessage("Error:: " + e);

			} // catch

	    	messageWindow.WriteMessage("Beginning Simulation... ");


			while ( !Done )
			{
				// Get the message queue

				try
				{
					messageQueue = mmInterfaceObject.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					messageWindow.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = -400, this means that user has
				// given command to post the sense the fire. Note that we 
				// get all the messages at once... there is a 2.5 second delay 
				// between samples,.. so the assumption is that there should only 
				// be a message at most. If there are more, it is the last message 
				// that will effect the output of the sensor.

				int qlen = messageQueue.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = messageQueue.GetMessage();

					if ( Msg.GetMessageId() == -400 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("SET_FIRE")) // Report Fire
						{
							shouldSetFire = true;

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
							mmInterfaceObject.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
				    		messageWindow.WriteMessage("Error unregistering: " + e);

				    	} // catch

						messageWindow.WriteMessage("\n\nSimulation Stopped. \n");

					} // if

				} // for
				
				
				// If user command received to set fire, report fire
				if (shouldSetFire) 
				{
					messageWindow.WriteMessage("Fire Fire!" );
					ReportFire(mmInterfaceObject);
					shouldSetFire = false;
				}

				// Before we go to bed, send the heart beat message.
				hb.HeartBeat(mmInterfaceObject);

				// Here we wait for a 2.5 seconds before we start the next sample
				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					messageWindow.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while

		} 
		else 
		{

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main



	/***************************************************************************
	* CONCRETE METHOD:: ReportFire
	* Purpose: This method reports the fire breakage to the 
	* specified message manager. This method assumes an message ID of 400.
	*
	* Arguments: MessageManagerInterface ei - this is the messagemanger interface
	*			 where the message will be posted.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void ReportFire(MessageManagerInterface messageManager)
	{
		// Here we create the message.

		Message msg = new Message( (int) 400, "FIRE");

		// Here we send the message to the message manager.

		try
		{
			messageManager.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Reporting Fire:: " + e );

		} // catch

	} // ReportFire

} // FireSensor


