/******************************************************************************************************************
* FireAlarmController.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* 
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
*
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;

import java.util.*;

public class FireAlarmController {
	
	static Indicator fireIndicator;
	
	public static void main(String args[])
	{
		String MsgMgrIP;					// Message Manager IP address
		Message Msg = null;					// Message object
		MessageQueue eq = null;				// Message Queue
		MessageManagerInterface mmInterfaceObject = null;	// Interface object to the message manager
		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag
		boolean isFireDetected = false;

		HeartBeater hb = null;          	// The heart beater.
		

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

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (mmInterfaceObject != null)
		{
			// Prepare the heart beater
			hb = new HeartBeater("Fire Alarm Controller #1", "A controller to control the fire alarm.");

			System.out.println("Registered with the message manager." );

			/* Now we create the temperature control status and message panel
			** We put this panel about 1/3 the way down the terminal, aligned to the left
			** of the terminal. The status indicators are placed directly under this panel
			*/

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Fire Alarm Controller Status Console", WinPosX, WinPosY);

			// Put the status indicators under the panel...

			fireIndicator = new Indicator ("FIRE Alarm OFF", mw.GetX(), mw.GetY() + mw.Height());
			
			mw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + mmInterfaceObject.GetMyId() );
				mw.WriteMessage("   Registration Time: " + mmInterfaceObject.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			while ( !Done )
			{

				try
				{
					eq = mmInterfaceObject.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 6 & -6.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();

					if ( Msg.GetMessageId() == 400 )
					{
						String message = Msg.GetMessage();
						// String[] messageArray = message.split("-");
						
						if (message.equals("FIRE")) {
							// Fire Detected							
							isFireDetected = true;
						}											
					}
					
					
					if ( Msg.GetMessageId() == -7 )
					{
						String message = Msg.GetMessage();												
						
						if (message.equals("FIRE_ALARM_CLOSE")) {														
							isFireDetected = false;
							mw.WriteMessage("Fire turned off." );
							mw.WriteMessage("***Fire Detection Alarm Closed***" );							
							fireIndicator.SetLampColorAndMessage("FIRE ALARM OFF", 0);							
						}
					}
						
						

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
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

				    	fireIndicator.dispose();
						

					} // if

				} // for
				
				
				
				if (isFireDetected) {
					// Fire Detected
					mw.WriteMessage("***Fire Detected***" );
					fireIndicator.SetLampColorAndMessage("FIRE ALARM ON", 3);
				}
				
				/*else 
				{
					mw.WriteMessage("Fire turned off." );
					fireIndicator.SetLampColorAndMessage("FIRE ALARM OFF", 0);
				}*/

				// Before we go to bed, send the heart beat message.
				hb.HeartBeat(mmInterfaceObject);

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main
}
