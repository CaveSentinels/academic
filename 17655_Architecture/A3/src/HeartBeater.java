import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;

public class HeartBeater {

    private static final int MSG_EQUIPMENT_HEARTBEAT = 860405;
    private String heartBeatMsg;    // The heart beat message to send.

    // Constructor: Pass the name and description in.
    public HeartBeater(String name, String description) {
        heartBeatMsg = name + "," + description;
    }

    // Beat the heart.
    public void HeartBeat(MessageManagerInterface msgInterface) {
        // Here we create the message.

        Message msg = new Message( MSG_EQUIPMENT_HEARTBEAT, heartBeatMsg );

        // Here we send the message to the message manager.

        try
        {
            msgInterface.SendMessage( msg );
            //System.out.println( "Sent Temp Message" );

        } // try

        catch (Exception e)
        {
            System.out.println( "Error Posting Heartbeat Message:: " + e );

        } // catch
    }
}
