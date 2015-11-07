/******************************************************************************************************************
* File:ECSConsole.java
* Course: 17655
* Project: Assignment 3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 February 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description: This class is the console for the museum environmental control system. This process consists of two
* threads. The ECSMonitor object is a thread that is started that is responsible for the monitoring and control of
* the museum environmental systems. The main thread provides a text interface for the user to change the temperature
* and humidity ranges, as well as shut down the system.
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
import TermioPackage.*;
import MessagePackage.*;

public class SystemAConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		SystemAMonitor Monitor = null;			// The environmental control system monitor
		boolean isWindowAlarmRinging = false;
		boolean isDoorAlarmRinging = false;
		boolean isMotionDetectionAlarmRinging = false;
		boolean systemArmed = true;
		

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// message manager is not on the local system

			Monitor = new SystemAMonitor( args[0] );

		} else {

			Monitor = new SystemAMonitor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (Monitor.IsRegistered() )
		{
			Monitor.start(); // Here we start the monitoring and control thread

			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "\n\n\n\n" );
				System.out.println( "Security Monitor: \n" );

				if (args.length != 0)
					System.out.println( "Using message manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local message manger \n" );
				
					
					System.out.println( "1: Arm System" );
					System.out.println( "2: Disarm System" );
				
				
					if (!isWindowAlarmRinging) {
						System.out.println( "100: Break Window" );
					}
					else {
						System.out.println( "100: Close Window Break Alarm" );
					}
					
					
					if (!isDoorAlarmRinging) {
						System.out.println( "200: Break Door" );
					}
					else {
						System.out.println( "200: Close Door Break Alarm" );
					}
					
					
					if (!isMotionDetectionAlarmRinging) {
						System.out.println( "300: Start Motion" );
					}
					else {
						System.out.println( "300: Close Motion Detection Alarm" );
					}
				
				
				System.out.println( "X: Stop System\n" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();
				
				
				if ( Option.equals( "1" ) ) {
					if (systemArmed) {
						// System is already Armed
						System.out.println("System is already armed");
					}
					else {
						systemArmed = true;
						System.out.println("System is armed");
					}
				}
				
				
				if ( Option.equals( "2" ) ) {
					if (systemArmed) {
						// System is Armed
						systemArmed = false;
						System.out.println("System is disarmed");
					}
					else {
						System.out.println("System is already disarmed");
					}
				}


				if (systemArmed) {
					if ( Option.equals( "100" ) ) {
						if (!isWindowAlarmRinging) {
							// Trigger Window Break Sensor
							Monitor.BreakWindow();
							isWindowAlarmRinging = true;
						}
						else {
							Monitor.CloseWindowBreakAlarm();
							isWindowAlarmRinging = false;
						}
					} 
					

					if ( Option.equals( "200" ) ) {
						if (!isDoorAlarmRinging) {
							// Trigger Door Break Sensor
							Monitor.BreakDoor();
							isDoorAlarmRinging = true;
						}
						else {
							Monitor.CloseDoorBreakAlarm();
							isDoorAlarmRinging = false;
						}
					}
					
					
					if ( Option.equals( "300" ) ) {
						if (!isMotionDetectionAlarmRinging) {
							// Trigger Fire Sensor
							Monitor.DetectMovement();
							isMotionDetectionAlarmRinging = true;
						}
						else {
							Monitor.CloseMotionDetectorAlarm();
							isMotionDetectionAlarmRinging = false;
						}
					}
				}
				


				//////////// option X ////////////

				if ( Option.equalsIgnoreCase( "X" ) )
				{
					// Here the user is done, so we set the Done flag and halt
					// the environmental control system. The monitor provides a method
					// to do this. Its important to have processes release their queues
					// with the message manager. If these queues are not released these
					// become dead queues and they collect messages and will eventually
					// cause problems for the message manager.

					Monitor.Halt();
					Done = true;
					System.out.println( "\nConsole Stopped... Exit monitor mindow to return to command prompt." );
					Monitor.Halt();

				} // if

			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // ECSConsole

