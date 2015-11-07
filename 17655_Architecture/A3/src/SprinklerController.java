/******************************************************************************************************************
 * SprinklerController.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SprinklerController  {

	static SprinklerController sc;
	static Indicator sprinklerIndicator;
	static MessageManagerInterface mmInterfaceObject = null;	// Interface object to the message manager
	static MessageWindow mw;
	static SystemBMonitor Monitor = null;			// The environmental control system monitor
	static boolean isSprinklerOn = false;
	

	public static void main(String args[])
	{
		String MsgMgrIP;					// Message Manager IP address
		Message Msg = null;					// Message object
		MessageQueue messageQueue = null;				// Message Queue

		HeartBeater hb = null;          // The heart beater.

		int	Delay = 2500;					// The loop delay (2.5 seconds)
		boolean Done = false;				// Loop termination flag
		boolean isFireDetected = false;
		sc = new SprinklerController();

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

		if ( args.length != 0 )
		{
			// message manager is not on the local system

			Monitor = new SystemBMonitor( args[0] );

		} else {

			Monitor = new SystemBMonitor();

		} // if



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

		} 
		else 
		{
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
			hb = new HeartBeater("Sprinkler Controller #1", "A controller to control the room's sprinkler.");

			System.out.println("Registered with the message manager." );

			/* Now we create the Sprinkler control status and message panel
			 ** We put this panel about 1/3 the way down the terminal, aligned to the left
			 ** of the terminal. The status indicators are placed directly under this panel
			 */

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms
			//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
			//of a percentage of the screen height

			mw = new MessageWindow("Sprinkler Controller Status Console", WinPosX, WinPosY);

			// Put the status indicators under the panel...

			sprinklerIndicator = new Indicator ("SPRINKLER OFF", mw.GetX(), mw.GetY() + mw.Height());

			mw.WriteMessage("Registered with the message manager.");

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
					messageQueue = mmInterfaceObject.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 6 & -6.

				int qlen = messageQueue.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = messageQueue.GetMessage();

					if ( Msg.GetMessageId() == 400 )
					{
						String message = Msg.GetMessage();						

						if (message.equals("FIRE")) 
						{
							// Fire Detected
							isFireDetected = true;
							isSprinklerOn = false;
						}											
					}
					
					/*else if ( Msg.GetMessageId() == -7 )
					{
						String message = Msg.GetMessage();												
						
						if (message.equals("FIRE_ALARM_CLOSE")) {														
							isFireDetected = false;
							mw.WriteMessage("Fire turned off." );
							mw.WriteMessage("***Fire Detection Alarm Closed***" );							
							fireIndicator.SetLampColorAndMessage("FIRE ALARM OFF", 0);							
						}
					}*/

					else if ( Msg.GetMessageId() == -7 )
					{
						isFireDetected = false;
						sc = new SprinklerController();
						sc.TurnSprinklerOff();
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

						sprinklerIndicator.dispose();


					} // if

				} // for



				if (isFireDetected && !isSprinklerOn) 
				{
					// Turn on Sprinkler boolean variable so that it does come inside this loop as long as fire is turned on
					isSprinklerOn = true;

					mw.WriteMessage("***Fire Detected***" );

					// Pop up the console for Sprinkler Action

					System.out.print("Fire has been detected. Sprinkler will automatically turn on in 10 seconds. \n"
							+ "Press Y to turn it off or Press any other key to start it immediately...\n");

					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					long startTime = System.currentTimeMillis();
					Scanner scanner = null;
					
					try
					{


						while ((System.currentTimeMillis() - startTime) < 10 * 1000 && ! in.ready()) 
						{							
							;// Wait for either 10 seconds get over or user input
						}

						if (in.ready() && in.readLine().toLowerCase().equals("y")) // User responded
						{
							System.out.print("You entered Y. Going to turn off the sprinkler...\n");
							sc.TurnSprinklerOff();							
							System.out.print("Sprinkler turned off\n");

						} 
						else // User did not respond
						{							
							System.out.print("Going to turn on the sprinkler...\n");							
							mw.WriteMessage("Received sprinkler ON message.");
							sc.TurnSprinklerOn();
							Thread.sleep(2*1000);

							System.out.println("Sprinkler is turned on. Press Y/y to turn it off? Or press any other key to keep it running.");
							try
							{
								scanner = new Scanner(System.in);						 						      
								String input  = scanner.nextLine();
								
								if(input.toLowerCase().equals("y"))
								{
									sc.TurnSprinklerOff();
									System.out.println("Sprinkler is turned off.");						    	
								}
								else
								{
									System.out.println("Sorry. Wrong input. Sprinkler still ON.");		
								}
								
							}
							catch (Exception e)
							{
								System.out.println(e.getMessage());
							}
							finally
							{
								scanner.close();
							}							
						}

						/*Message msg = new Message(-7, "FIRE_ALARM_CLOSE");											
						mmInterfaceObject.SendMessage( msg );
						isFireDetected = false;*/
					}

					catch(Exception e)
					{
						System.out.println("Exception while reading user data...");
					}
					finally
					{
						if(scanner!=null)
							scanner.close();
					}


				}

				/*else if(isSprinklerOn == false)
				{
					sc.TurnSprinklerOff();
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

		} 
		else 
		{
			System.out.println("Unable to register with the message manager.\n\n" );
		} // if

	} // main

	public void TurnSprinklerOn()
	{
		sprinklerIndicator.SetLampColorAndMessage("SPRINKLER ON", 1);
		isSprinklerOn = true;
	}

	public void TurnSprinklerOff()
	{
		sprinklerIndicator.SetLampColorAndMessage("SPRINKLER OFF", 0);
		mw.WriteMessage("Received sprinkler OFF message from user.");
		// isSprinklerOn = false;
	}



}