import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;

import java.util.ArrayList;

public class MaintenanceMonitor extends Thread {

    private static final int MSG_EQUIPMENT_HEARTBEAT = 860405;
    private static final int MSG_MAINTENANCE_END = 869999;

    private static final long EQUIPMENT_DISCONNECTION_THREASHOLD = 1000 * 4;    // 4 sec

    private MessageManagerInterface em = null;	// Interface object to the message manager
    private String MsgMgrIP = null;				// Message Manager IP address
    boolean Registered = true;					// Signifies that this class is registered with an message manager.
    MessageWindow mw = null;					// This is the message window

    ArrayList<EquipmentInfo> equipList;         // All the currently installed equipment.

    public MaintenanceMonitor()
    {
        // Create the equipment list.
        equipList = new ArrayList<EquipmentInfo>();

        // message manager is on the local system

        try
        {
            // Here we create an message manager interface object. This assumes
            // that the message manager is on the local machine

            em = new MessageManagerInterface();

        }

        catch (Exception e)
        {
            System.out.println("MaintenanceMonitor::Error instantiating message manager interface: " + e);
            Registered = false;

        } // catch

    } //Constructor

    public MaintenanceMonitor( String MsgIpAddress )
    {
        // message manager is not on the local system

        MsgMgrIP = MsgIpAddress;

        try
        {
            // Here we create an message manager interface object. This assumes
            // that the message manager is NOT on the local machine

            em = new MessageManagerInterface( MsgMgrIP );
        }

        catch (Exception e)
        {
            System.out.println("MaintenanceMonitor::Error instantiating message manager interface: " + e);
            Registered = false;

        } // catch

    } // Constructor

    public void run()
    {
        Message Msg = null;				// Message object
        MessageQueue eq = null;			// Message Queue
        int MsgId = 0;					// User specified message ID
//        float CurrentTemperature = 0;	// Current temperature as reported by the temperature sensor
//        float CurrentHumidity= 0;		// Current relative humidity as reported by the humidity sensor
        int	Delay = 1000;				// The loop delay (1 second)
        boolean Done = false;			// Loop termination flag
        boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
        boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

        if (em != null)
        {
            // Now we create the ECS status and message panel
            // Note that we set up two indicators that are initially yellow. This is
            // because we do not know if the temperature/humidity is high/low.
            // This panel is placed in the upper left hand corner and the status
            // indicators are placed directly to the right, one on top of the other

            mw = new MessageWindow("Maintenance Monitoring Console", 0, 0);

            mw.WriteMessage( "Registered with the message manager." );

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
                // Here we get our message queue from the message manager

                try
                {
                    eq = em.GetMessageQueue();

                } // try

                catch( Exception e )
                {
                    mw.WriteMessage("Error getting message queue::" + e );

                } // catch

                // If there are messages in the queue, we read through them.
                // We are looking for MessageIDs = MSG_EQUIPMENT_HEARTBEAT
                // Message IDs of are equipment registration; message IDs of MSG_EQUIPMENT_HEARTBEAT
                // are equipment heartbeats.
                // Note that we get all the messages at once... there is a 1
                // second delay between samples,.. so the assumption is that there should
                // only be a message at most. If there are more, it is the last message
                // that will effect the status of the temperature and humidity controllers
                // as it would in reality.

                int qlen = eq.GetSize();

                for ( int i = 0; i < qlen; i++ )
                {
                    Msg = eq.GetMessage();

                    if ( Msg.GetMessageId() == MSG_EQUIPMENT_HEARTBEAT ) // Equipment heartbeat
                    {
                        // TODO: Remove the debugging message.
//                        mw.WriteMessage("[DEBUG] Heartbeat message received: " + Msg.GetMessage());

                        try
                        {
                            String HeartbeatMsg = Msg.GetMessage();

                            // Parse the heartbeat message to retrieve the equipment
                            // name and description.
                            String[] parts = HeartbeatMsg.split(",");   // seperate with ','
                            if (parts.length != 2)
                            {
                                // The heartbeat message is in wrong format.
                                throw new Exception("Wrong heartbeat message: \"" + HeartbeatMsg + "\"");
                            }
                            else
                            {
                                String Name = parts[0];
                                String Description = parts[1];

                                if (EquipmentExists(Name))
                                {
                                    EquipmentUpdateLastSeenTime(Name);
                                    // We do not write any message to the message window, otherwise
                                    // the user may not see the equipment disconnection messages.
                                }
                                else
                                {
                                    EquipmentNew(Name, Description);
                                    mw.WriteMessage("Equipment < " + Name + "> is installed to the system.");
                                }
                            }

                        } // try

                        catch( Exception e )
                        {
                            mw.WriteMessage("Error reading heartbeat message: " + e);

                        } // catch

                    } // if

                    // If the message ID == 99 then this is a signal that the simulation
                    // is to end. At this point, the loop termination flag is set to
                    // true and this process unregisters from the message manager.

                    if ( Msg.GetMessageId() == MSG_MAINTENANCE_END)
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

                    } // if

                } // for

                // Go through the equipment list to see if there is any equipment disconnected.
                long currTimeMillis = System.currentTimeMillis();
                // TODO: Remove the debug messages.
//                mw.WriteMessage("[DEBUG] Checking if any equipment disconnected. Time = " + String.valueOf(currTimeMillis));
                ArrayList<String> disconnectedEquipList = new ArrayList<String>();
                ArrayList<EquipmentInfo> updatedEquipList = new ArrayList<EquipmentInfo>();
//                mw.WriteMessage("[DEBUG] Equipment list size: " + String.valueOf(equipList.size()));
                for (int i = 0; i < equipList.size(); ++i) {
                    EquipmentInfo equip = equipList.get(i);
//                    mw.WriteMessage("[DEBUG] Equipment: " + equip.GetName() + " Last seen time: " + String.valueOf(equip.GetLastSeenTime()));
                    if (currTimeMillis - equip.GetLastSeenTime() > EQUIPMENT_DISCONNECTION_THREASHOLD) {
                        disconnectedEquipList.add(equip.GetName());
                    } else {
                        updatedEquipList.add(equip);
                    }
                }

                equipList = updatedEquipList;   // Maintain the updated list.

                // Write a message if some equipment disconnects.
                for (String name : disconnectedEquipList) {
                    mw.WriteMessage("WARNING!!! Equipment < " + name + " > is not responding.");
                }

                // This delay slows down the sample rate to Delay milliseconds

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
     * CONCRETE METHOD:: GetInstalledEquipmentList
     * Purpose: This method returns the list of installed equipment.
     *
     * Arguments: none
     *
     * Returns: An array of all the installed equipment.
     *
     * Exceptions: None
     *
     ***************************************************************************/

    public ArrayList<EquipmentInfo> GetInstalledEquipmentList()
    {
        return equipList;
    }


    /***************************************************************************
     * CONCRETE METHOD:: EquipmentExists
     * Purpose: This method returns if the equipment of the specified name already
     *  exists. If the equipment exists, it is not new and we only need to update
     *  its last seen time.
     *
     * Arguments: String: The name of the equipment.
     *
     * Returns: A boolean indicating if the equipment exists or not.
     *
     * Exceptions: None
     *
     ***************************************************************************/
    boolean EquipmentExists(String Name) {
        boolean exists = false;

        for (int i = 0; i < equipList.size(); ++i) {
            EquipmentInfo equip = equipList.get(i);
            if ( equip.GetName().equals(Name) ) {
                exists = true;
                break;
            }
        }
        return exists;
    }


    /***************************************************************************
     * CONCRETE METHOD:: EquipmentUpdateLastSeenTime
     * Purpose: This method updates the last seen time of the specified equipment.
     *
     * Arguments: String: The name of the equipment.
     *
     * Returns: none
     *
     * Exceptions: None
     *
     ***************************************************************************/

    void EquipmentUpdateLastSeenTime(String Name) {
        for (int i = 0; i < equipList.size(); ++i) {
            EquipmentInfo equip = equipList.get(i);
            if (equip.GetName().equals(Name)) {
                equip.Heartbeat();
                break;
            }
        }
    }


    /***************************************************************************
     * CONCRETE METHOD:: EquipmentNew
     * Purpose: This method creates a new entry for the newly arrived equipment.
     *
     * Arguments: String: The name of the equipment.
     *
     * Arguments: String: The description of the equipment.
     *
     * Returns: none
     *
     * Exceptions: None
     *
     ***************************************************************************/

    void EquipmentNew(String Name, String Description) {
        equipList.add(new EquipmentInfo(Name, Description));
    }


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

    } // SetTemperatureRange


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
        mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN MAINTENANCE SYSTEM***" );

        // Here we create the stop message.

        Message msg;

        msg = new Message( MSG_MAINTENANCE_END, "XXX" );

        // Here we send the message to the message manager.

        try
        {
            em.SendMessage( msg );

        } // try

        catch (Exception e)
        {
            System.out.println("Error sending halt message:: " + e);

        } // catch

    } // Halt

}
