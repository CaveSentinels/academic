import TermioPackage.Termio;

import java.util.ArrayList;

public class MaintenanceConsole {

    public static void main(String args[])
    {
        Termio UserInput = new Termio();	// Termio IO Object
        boolean Done = false;				// Main loop flag
        String Option = null;				// Menu choice from user
        MaintenanceMonitor Monitor = null;			// The environmental control system monitor

        /////////////////////////////////////////////////////////////////////////////////
        // Get the IP address of the message manager
        /////////////////////////////////////////////////////////////////////////////////

        if ( args.length != 0 )
        {
            // message manager is not on the local system

            Monitor = new MaintenanceMonitor( args[0] );

        } else {

            Monitor = new MaintenanceMonitor();

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
                System.out.println( "Maintenance System (MS) Command Console: \n" );

                if (args.length != 0)
                    System.out.println( "Using message manger at: " + args[0] + "\n" );
                else
                    System.out.println( "Using local message manger \n" );

                System.out.println( "Select an Option: \n" );
                System.out.println( "1: List all installed equipment" );
                System.out.println( "X: Stop Maintenance System\n" );
                System.out.print( "\n>>>> " );
                Option = UserInput.KeyboardReadString();

                //////////// option 1 ////////////

                if ( Option.equals( "1" ) )
                {
                    // Here we get the list of equipment.
                    ArrayList<EquipmentInfo> equipments = Monitor.GetInstalledEquipmentList();
                    if (equipments.size() > 0)
                    {
                        System.out.println( "\nCurrently installed equipment:" );
                        for (int i = 0; i < equipments.size(); ++i)
                        {
                            EquipmentInfo equip = equipments.get(i);
                            String name = equip.GetName();
                            String description = equip.GetDescription();
                            System.out.println( "\n\t- Equipment #" + String.valueOf(i+1) + ": " + name + " (" + description + ")" );
                        }
                    }
                    else
                    {
                        System.out.println( "\nNo equipment installed." );
                    }

                } // if

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
                    System.out.println( "\nConsole Stopped... Exit monitor window to return to command prompt." );
                    Monitor.Halt();

                } // if

            } // while

        } else {

            System.out.println("\n\nUnable start the monitor.\n\n");

        } // if

    } // main
}
