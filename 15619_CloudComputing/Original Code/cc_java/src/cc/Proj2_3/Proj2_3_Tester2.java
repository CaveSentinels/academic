package cc.Proj2_3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.logging.*;


public class Proj2_3_Tester2
{
    // To MSB
    private final int CACHE_MAX_SIZE = 1000;
    private final int RANGE_RADIUS = 50;
    private LRUCache2 _lruCache = new LRUCache2( CACHE_MAX_SIZE );

    private static final Logger log = Logger.getLogger( Proj2_3_Tester.class.getName() );

    public Proj2_3_Tester2()
    {
        try {
            FileHandler fileHandler = new FileHandler("Test.log");
            log.addHandler( fileHandler );
            log.setLevel(Level.INFO);
            fileHandler.setFormatter(new Formatter()
            {
                @Override
                public String format(LogRecord record)
                {
                    return record.getMessage() + "\n";
                }
            });
        }
        catch (Exception e) {
            // Empty
        }
    }

    // To MSB

    private String[] _databaseInstances = new String[2];

    private String generateRangeURL(Integer instanceID, Integer startRange, Integer endRange)
    {
        return "http://" + _databaseInstances[instanceID] + "/range?start_range="
                + Integer.toString(startRange) + "&end_range=" + Integer.toString(endRange);
    }

    private String generateURL(Integer instanceID, String key) {
        return "http://" + _databaseInstances[instanceID] + "/target?targetID=" + key;
    }

    private String sendRequest( String requestURL )
    {
        StringBuilder resultsStr = new StringBuilder();

        int startPos = requestURL.indexOf("start_range=");
        int endPos = requestURL.indexOf("&end_range=");
        int targetPos = requestURL.indexOf("targetID=");

        if ( startPos >= 0 && endPos >= 0 )
        {
            String startRange = requestURL.substring(startPos + 12, endPos);
            String endRange = requestURL.substring(endPos + 11);

            int startRangeNum = Integer.parseInt(startRange);
            int endRangeNum = Integer.parseInt(endRange);

            for (int i = startRangeNum; i <= endRangeNum; ++i)
            {
                resultsStr.append(i).append(";");
            }
        }
        else if ( targetPos >= 0 )
        {
            String targetID = requestURL.substring( targetPos + 9 );
            resultsStr.append( targetID );
        }
        else
        {
            // TODO: What to do?
        }

        return resultsStr.toString();
    }

    private ArrayList< String > readIDList(String file)
    {
        ArrayList< String > idList = new ArrayList<String>();

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] parts = line.split( "\t" );
                idList.add( parts[0] );
            }
            br.close();
        }
        catch ( FileNotFoundException e )
        {
            // TODO: Implement me!
        }
        catch ( IOException e )
        {
            // TODO: Implement me!
        }
        catch ( Exception e )
        {
            // TODO: Implement me!
        }

        return idList;
    }

    private String retrieveDetails( String targetID )
    {
        String result = null;

        result = _lruCache.access(targetID);

        if (result == null)
        {
            int idNum = Integer.parseInt(targetID);

            int startRange = idNum;
            int endRange = idNum + 2 * RANGE_RADIUS;
            if (idNum > RANGE_RADIUS)
            {
                startRange = idNum - RANGE_RADIUS ;
                endRange = idNum + RANGE_RADIUS;
            }

            String resultsString = sendRequest(generateRangeURL(
                            0, startRange, endRange)
            );

            String[] results = resultsString.split(";");

            for (int i = 0; i < results.length; ++i)
            {
                _lruCache.update(String.valueOf(idNum + i), results[i]);
            }

            result = _lruCache.access(targetID);

            log.log( Level.INFO, targetID + ": miss" );
        }
        else
        {
            log.log( Level.INFO, targetID + ":" );
        }

        return result;
    }

    public void Main_Proj2_3()
    {
        // Read all the IDs.
        ArrayList< String > idList = readIDList( "data_pure_id.txt" );

        // Iterate each ID and request for the details
        int idListLength = idList.size();
        String result = null;
        for ( int i = 0; i < idListLength; ++i )
        {
            result = retrieveDetails( idList.get(i) );
            if ( !result.equals(idList.get(i)) )
            {
                log.log(Level.SEVERE, "ID = " + idList.get(i) + "; Result = " + result);
            }
        }

        System.out.println( "ID List: " + String.valueOf( idList.size() ) );
    }
}
