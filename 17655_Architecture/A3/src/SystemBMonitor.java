/******************************************************************************************************************
* File:ECSMonitor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class monitors the environmental control systems that control museum temperature and humidity. In addition to
* monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
* ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	static private void Heater(MessageManagerInterface ei, boolean ON )
*	static private void Chiller(MessageManagerInterface ei, boolean ON )
*	static private void Humidifier(MessageManagerInterface ei, boolean ON )
*	static private void Dehumidifier(MessageManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;

import java.util.*;

class SystemBMonitor extends Thread
{
	private MessageManagerInterface mmInterfaceObject = null;	// Interface object to the message manager
	private String MsgMgrIP = null;				// Message Manager IP address
	boolean Registered = true;					// Signifies that this class is registered with an message manager.
	MessageWindow mw = null;					// This is the message window
	Indicator ti;								// Temperature indicator
	Indicator hi;								// Humidity indicator
	boolean isWindowBreakDetected = false;
	boolean isDoorBreakDetected = false;
	boolean isMovementDetected = false;
	boolean isFireDetected = false;
	
	boolean hasMessageBeenPosted = true;
	Indicator wai;
	Indicator dai;
	Indicator mai;
	
	
	public SystemBMonitor()
	{
		// message manager is on the local system

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is on the local machine

			mmInterfaceObject = new MessageManagerInterface();

		}

		catch (Exception e)
		{
			System.out.println("SystemAMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public SystemBMonitor( String MsgIpAddress )
	{
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine

			mmInterfaceObject = new MessageManagerInterface( MsgMgrIP );
		}

		catch (Exception e)
		{
			System.out.println("SystemAMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Message Msg = null;				// Message object
		MessageQueue messageQueue = null;			// Message Queue
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		

		if (mmInterfaceObject != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("System B Monitoring Console", 0, 0);
			wai = new Indicator ("Window", mw.GetX(), mw.GetY()+mw.Height());
			dai = new Indicator ("Door", mw.GetX()+(wai.Width()*2), mw.GetY()+mw.Height());
			mai = new Indicator ("Motion", mw.GetX()+(wai.Width()*2)+(dai.Width()*2), mw.GetY()+mw.Height());

			mw.WriteMessage( "Registered with the message manager." );

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
				// Here we get our message queue from the message manager

				try
				{
					messageQueue = mmInterfaceObject.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 100, 200, 300. Message ID of 100 is the 
				// window sensor reading; message ID of 200 is the door sensor reading;
				// message ID of 300 is the motion detector reading. 
				// Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature, humidity and intrusion alarm controllers
				// as it would in reality.

				int qlen = messageQueue.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = messageQueue.GetMessage();

					if (Msg.GetMessageId() == 100) {
						// Window breakage detected
						isWindowBreakDetected = true;
						mw.WriteMessage("Window Broken");
						hasMessageBeenPosted = false;
						wai.SetLampColorAndMessage("Window Broken", 3);
					} 
					
					if (Msg.GetMessageId() == 200) {
						// Door breakage detected
						isDoorBreakDetected = true;
						mw.WriteMessage("Door Broken");
						hasMessageBeenPosted = false;
						dai.SetLampColorAndMessage("Door Broken", 3);
					} 
					
					if (Msg.GetMessageId() == 300) {
						// Motion detected
						isMovementDetected = true;
						mw.WriteMessage("Movement Detected");
						hasMessageBeenPosted = false;
						mai.SetLampColorAndMessage("Motion Detected", 3);
					} 
					
					if (Msg.GetMessageId() == 400) {
						// Fire detected
						isFireDetected = true;
						mw.WriteMessage("Fire Detected");												
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
				    	
				    	wai.dispose();
						dai.dispose();
						mai.dispose();



					} // if

				} // for

				
				if (!hasMessageBeenPosted) {
					// Post a message for controller
					BlowAlarm(isWindowBreakDetected, isDoorBreakDetected, isMovementDetected, isFireDetected);
					hasMessageBeenPosted = true;
				}
				
				
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

	/***************************************************************************
	* CONCRETE METHOD:: IsRegistered
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} 


	/***************************************************************************
	* CONCRETE METHOD:: Halt
	* Purpose: This method posts an message that stops the environmental control
	*		   system.
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: Posting to message manager exception
	*
	***************************************************************************/

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop message.

		Message msg;

		msg = new Message( (int) 99, "XXX" );

		// Here we send the message to the message manager.

		try
		{
			mmInterfaceObject.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	

	/***************************************************************************
	* CONCRETE METHOD:: BlowAlarm
	* Purpose: This method posts messages that will signal all the
	*		   controllers to turn on/off the devices
	*
	*
	* Returns: none
	*
	* Exceptions: Posting to message manager exception
	*
	***************************************************************************/

	private void BlowAlarm(boolean windowBreakAlarm, boolean doorBreakAlarm, boolean motionDetectionAlarm, boolean fireAlarm) {
		// Here we create the message.
		StringBuilder message = new StringBuilder();
		
		if (windowBreakAlarm) {
			message.append("WB");
		}
		
		if (doorBreakAlarm) {
			if (windowBreakAlarm) {
				// WB-DB
				message.append("-DB");
			}
			else {
				message.append("DB");
			}
		}
		
		if (motionDetectionAlarm) {
			if (windowBreakAlarm || doorBreakAlarm) {
				// WB-DB-MD || WB-MD || DB-MD
				message.append("-MD");
			}
			else {
				message.append("MD");
			}
		}			

		Message msg = new Message( (int) 6, message.toString());
		
		
		


		// Here we send the message to the message manager.

		try
		{
			mmInterfaceObject.SendMessage( msg );
			if(fireAlarm)
			{
				Message msgFire = new Message( (int) 7, "FIRE");
				mmInterfaceObject.SendMessage( msgFire );
			}

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending alarm control message::  " + e);

		} // catch

	} // BlowAlarm
	
	
	
	private void CloseAlarm(int ID, String m) {
		// Here we create the message.

		Message msg = new Message( ID, m );
		
		try {
			// Here we send the message to the message manager.
			mmInterfaceObject.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error Confirming Message:: " + e);

		} // catch
	} // CloseAlarm 
	
	
	public void BreakWindow() {
		mw.WriteMessage( "***Window Break Message Sent***");
		SendMessageToSensor(-100, "BREAK");

	}
	
	public void BreakDoor() {
		mw.WriteMessage( "***Door Break Message Sent***");
		SendMessageToSensor(-200, "BREAK");
	}
	
	public void DetectMovement() {
		mw.WriteMessage( "***Movement Detection Message Sent***");
		SendMessageToSensor(-300, "MOVEMENT");
	}
	
	public void CloseWindowBreakAlarm() {
		mw.WriteMessage( "***Close Window Break Alarm***");
		CloseAlarm(-6, "WB_ALARM_CLOSE");
		isWindowBreakDetected = false;
		wai.SetLampColorAndMessage("Window", 0);
	}
	
	public void CloseDoorBreakAlarm() {
		mw.WriteMessage( "***Close Door Break Alarm***");
		CloseAlarm(-6, "DB_ALARM_CLOSE");
		isDoorBreakDetected = false;
		dai.SetLampColorAndMessage("Door", 0);
	}
	
	public void CloseMotionDetectorAlarm() {
		mw.WriteMessage( "***Close Motion Detector Alarm***");
		CloseAlarm(-6, "MD_ALARM_CLOSE");
		isMovementDetected = false;
		mai.SetLampColorAndMessage("Motion", 0);
	}
	
	public void DetectFire() {
		mw.WriteMessage( "***Fire Detection Message Sent***");
		SendMessageToSensor(-400, "SET_FIRE");
	}
	
	public void CloseFireAlarm() {
		mw.WriteMessage( "***Close Fire Alarm***");
		CloseAlarm(-7, "FIRE_ALARM_CLOSE");
		isFireDetected = false;
	}
	
	public void CloseSprinkler() {
		mw.WriteMessage( "***Close Sprinkler***");
		CloseAlarm(-7, "SPRINKLER_ALARM_CLOSE");
		isFireDetected = false;
	}
	
	
	
	
	private void SendMessageToSensor(int ID, String m) {
		// Here we create the message.

		Message msg = new Message( ID, m );
		
		try {
			// Here we send the message to the message manager.
			mmInterfaceObject.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error Confirming Message:: " + e);

		} // catch
	}

			
	
	
	

} // SystemAMonitor
 