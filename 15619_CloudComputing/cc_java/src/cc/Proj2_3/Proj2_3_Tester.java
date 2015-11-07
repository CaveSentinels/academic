package cc.Proj2_3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Proj2_3_Tester
{
    // To MSB
    private final int CACHE_MAX_SIZE = 1000;
    private final int CACHE_MAX_STATIC_SIZE = 86;
    private final int RANGE_RADIUS = 50;
    private final int CACHE_MAX_DYNAMIC_SIZE = CACHE_MAX_SIZE - CACHE_MAX_STATIC_SIZE;
    private LRUCache _lruCache = new LRUCache( CACHE_MAX_DYNAMIC_SIZE, CACHE_MAX_STATIC_SIZE );
    private ArrayList< String > _idListHF = new ArrayList<String>();
    private boolean _staticCacheUpdated = false;

    private static final Logger log = Logger.getLogger( Proj2_3_Tester.class.getName() );
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
        try
        {
            Thread.sleep(45);
        }
        catch(Exception e)
        {
            // TODO: Implement me!
        }

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
                resultsStr.append(i);
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
        if ( !_staticCacheUpdated )
        {
            for ( int i = 0; i < _idListHF.size(); ++i )
            {
                String result = sendRequest( generateURL( 0, _idListHF.get(i) ) );
                _lruCache.updateStatic( _idListHF.get(i), result );
            }

            try {
                FileHandler fileHandler = new FileHandler("Test.log");
                log.addHandler( fileHandler );
                log.setLevel(Level.INFO);
            }
            catch (Exception e) {
                // Empty
            }

            _staticCacheUpdated = true;
        }

        String result = _lruCache.access( targetID );

        if ( result == null )
        {
            int idNum = Integer.parseInt( targetID );

            String resultsString = sendRequest(generateRangeURL(
                            0, idNum - RANGE_RADIUS, idNum + RANGE_RADIUS)
            );

            String[] results = resultsString.split( ";" );

            for ( int i = 0; i < results.length; ++i )
            {
                _lruCache.update( String.valueOf( idNum - RANGE_RADIUS + i ), results[i] );
            }

            result = _lruCache.access( targetID );

            _lruCache.miss();
        }
        else
        {
            _lruCache.hit();
        }

        log.log( Level.INFO, "Hits: " + _lruCache.hits() + " ; Misses: " + _lruCache.misses() );

        return result;
    }

    public void Main_Proj2_3()
    {
        // Read all the IDs.
        ArrayList< String > idList = readIDList( "data_pure_id.txt" );

        // Read all the high-frequency IDs.
        _idListHF = readIDList( "data_pure_id_high_freq.txt" );

        // Iterate each ID and request for the details
        int idListLength = idList.size();
        for ( int i = 0; i < idListLength; ++i )
        {
            retrieveDetails( idList.get(i) );
        }

        System.out.println( "ID List: " + String.valueOf( idList.size() ) );
        System.out.println( "Hit count: " + String.valueOf( _lruCache.hits() ) );
        System.out.println( "Miss count: " + String.valueOf( _lruCache.misses() ) );
    }
}
