/******************************************************************************************************************
* File:IntrusionAlarmController.java
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

class IntrusionAlarmController
{
	public static void main(String args[])
	{
		String MsgMgrIP;					// Message Manager IP address
		Message Msg = null;					// Message object
		MessageQueue eq = null;				// Message Queue
		MessageManagerInterface em = null;	// Interface object to the message manager
		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag
		boolean isWindowBroken = false;
		boolean isDoorBroken = false;
		boolean isMotionDetected = false;
		boolean shouldUpdtaeWindowBreakIndicator = false;
		boolean shouldUpdtaeDoorBreakIndicator = false;
		boolean shouldUpdtaeMotionDetectorIndicator = false;

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

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (em != null)
		{
			// Prepare the heart beater
			hb = new HeartBeater("Intrusion Alarm Controller #1", "A controller to control the intrusion alarm.");

			System.out.println("Registered with the message manager." );

			/* Now we create the temperature control status and message panel
			** We put this panel about 1/3 the way down the terminal, aligned to the left
			** of the terminal. The status indicators are placed directly under this panel
			*/

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Intrusion Alarm Controller Status Console", WinPosX, WinPosY);

			// Put the status indicators under the panel...

			Indicator wai = new Indicator ("W Alarm", mw.GetX(), mw.GetY()+mw.Height());
			Indicator dai = new Indicator ("D Alarm", mw.GetX()+(wai.Width()*2), mw.GetY()+mw.Height());
			Indicator mai = new Indicator ("M Alarm", mw.GetX()+(wai.Width()*2)+(dai.Width()*2), mw.GetY()+mw.Height());

			mw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

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
					eq = em.GetMessageQueue();

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

					if ( Msg.GetMessageId() == 6 )
					{
						String message = Msg.GetMessage();
						String[] messageArray = message.split("-");
						
						if (Arrays.asList(messageArray).contains("WB")) {
							// Window Break Detected
							isWindowBroken = true;
							shouldUpdtaeWindowBreakIndicator = true;
						}
						
						if (Arrays.asList(messageArray).contains("DB")) {
							// Door Break Detected
							isDoorBroken = true;
							shouldUpdtaeDoorBreakIndicator = true;
						}
						
						if (Arrays.asList(messageArray).contains("MD")) {
							// Movement Detected
							isMotionDetected = true;
							shouldUpdtaeMotionDetectorIndicator = true;
						}
					}
					
					
					if ( Msg.GetMessageId() == -6 )
					{
						String message = Msg.GetMessage();
						
						if (message.equals("WB_ALARM_CLOSE")) {
							// Window Break Detected
							isWindowBroken = false;
							mw.WriteMessage("***Window Break Alarm Closed***" );
							shouldUpdtaeWindowBreakIndicator = true;
						}
						
						if (message.equals("DB_ALARM_CLOSE")) {
							// Door Break Detected
							isDoorBroken = false;
							mw.WriteMessage("***Door Break Alarm Closed***" );
							shouldUpdtaeDoorBreakIndicator = true;
						}
						
						if (message.equals("MD_ALARM_CLOSE")) {
							// Movement Detected
							isMotionDetected = false;
							mw.WriteMessage("***Motion Detection Alarm Closed***" );
							shouldUpdtaeMotionDetectorIndicator = true;
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
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						wai.dispose();
						dai.dispose();
						mai.dispose();

					} // if

				} // for
				
				
				if (isWindowBroken) {
					if (shouldUpdtaeWindowBreakIndicator) {
						// Window Break Detected
						mw.WriteMessage("***Window Broken***" );
						wai.SetLampColorAndMessage("Window Broken", 3);
						
					}
					
				}
				else {
					if (shouldUpdtaeWindowBreakIndicator) {
						wai.SetLampColorAndMessage("W Alarm", 0);
					}
					
				}
				
				if (isDoorBroken) {
					if (shouldUpdtaeDoorBreakIndicator) {
						// Door Break Detected
						mw.WriteMessage("***Door Broken***" );
						dai.SetLampColorAndMessage("Door Broken", 3);
					}
				}
				else {
					if (shouldUpdtaeDoorBreakIndicator) {
						dai.SetLampColorAndMessage("D Alarm", 0);
					}
				}
				
				if (isMotionDetected) {
					if (shouldUpdtaeMotionDetectorIndicator) {
						// Movement Detected
						mw.WriteMessage("***Motion Detected***" );
						mai.SetLampColorAndMessage("Motion Detected", 3);
					}
				}
				else {
					if (shouldUpdtaeMotionDetectorIndicator) {
						mai.SetLampColorAndMessage("M Alarm", 0);
					}
				}
				
				shouldUpdtaeWindowBreakIndicator = false;
				shouldUpdtaeDoorBreakIndicator = false;
				shouldUpdtaeMotionDetectorIndicator = false;

				// Before we go to bed, send the heart beat message.
				hb.HeartBeat(em);

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