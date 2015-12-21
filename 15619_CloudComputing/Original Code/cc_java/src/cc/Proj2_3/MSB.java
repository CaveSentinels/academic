/*
 * MSB.java
 * 
 * This is a web-service used by the MSB to get targets' private
 * conversations from the databases. The conversations have been
 * encrypted, but I have heard rumors about the key being a part 
 * of the results retrieved from the database. 
 * 
 * 02/08/15 - I have replicated the database instances to make
 * the web service go faster.
 * 
 * To do (before 02/15/15): My team lead says that I can get a 
 * higher RPS by optimizing the retrieveDetails function. I 
 * stack overflowed "how to optimize retrieveDetails function", 
 * but could not find any helpful results. I need to get it done
 * before 02/15/15 or I will lose my job to that new junior systems
 * architect.
 * 
 * 02/15/15 - :'(
 *
 * 02/22/15 - Let me continue your legend.
 *
 * @author: Yaobin Wen
 * @email: yaobinw@andrew.cmu.edu
 */


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;	// Robin: My code
import java.util.Map;	// Robin: My code

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;


public class MSB extends Verticle {

	// Robin: My code - BEGIN

	private static final int _MAX_ID_POSSIBLE = 850000;	// The maximum ID I've ever observed.

	// The maximum size of the entire cache.
	private static final int _CACHE_MAX_SIZE = 1000;

	// Cache size for high frequency data. The frequency is based on statistical data analysis.
	private static final int _CACHE_HIGH_FREQ_MAX_SIZE = 86;

	// Cache size for large IDs.
	private static final int _CACHE_LARGE_ID_MAX_SIZE = 85;

	// Cache size of LRU.
	private static final int _CACHE_MAX_DYNAMIC_SIZE =
			_CACHE_MAX_SIZE - _CACHE_HIGH_FREQ_MAX_SIZE - _CACHE_LARGE_ID_MAX_SIZE;

	// Cache load factor
	private static final float _CACHE_LOAD_FACTOR = 0.75F;

	// Cache increment radius: The increase/descrease of cache will be (+-)Radius.
	private static final int _RANGE_RADIUS = 50;

	// The cache for large IDs.
	private HashMap<String, String> _cacheLargeIDs =
			new HashMap<String, String>(_CACHE_HIGH_FREQ_MAX_SIZE, _CACHE_LOAD_FACTOR);

	// High-freq cache: the data in this cache will never be swapped out.
	// The selection of IDs is based on historical statistical data to see
	// which IDs appeared the most frequently.
	private HashMap<String, String> _cacheHighFreq =
			new HashMap<String, String>(_CACHE_HIGH_FREQ_MAX_SIZE, _CACHE_LOAD_FACTOR);

	// LRU cache
	private LinkedHashMap< String, String > _cacheLRU =
			new LinkedHashMap<String, String>(_CACHE_MAX_DYNAMIC_SIZE, _CACHE_LOAD_FACTOR, true ) {
				public boolean removeEldestEntry(Map.Entry eldest) {
					return size() > _CACHE_MAX_SIZE;
				}
			};

	private void _constructHighFreqCache()
	{
		ArrayList<String> idListHF = _readIDList("data_pure_id_high_freq.txt");
		// Insert at most _CACHE_HIGH_FREQ_MAX_SIZE.
		for (int i = 0; i < _CACHE_HIGH_FREQ_MAX_SIZE; ++i)
		{
			try
			{
				int idNum = Integer.parseInt(idListHF.get(i));
				String result = sendRequest(generateURL(
								_instanceIndex(idNum),
								idListHF.get(i)
						)
				);
				_cacheHighFreq.put(idListHF.get(i), result);
			}
			catch(Exception e)
			{
				// TODO: Log the error
			}
		}
	}

	private void _constructLargeIDCache()
	{
		for (int i = 0; i < _CACHE_LARGE_ID_MAX_SIZE; ++i)
		{
			try
			{
				int targetID = _MAX_ID_POSSIBLE - 1000 * i;
				String targetIDStr = String.valueOf(targetID);
				String result = sendRequest(generateURL(
								_instanceIndex(targetID),
								targetIDStr
						)
				);
				_cacheLargeIDs.put(targetIDStr, result);
			}
			catch(Exception e)
			{
				// TODO: Log the error
			}
		}
	}

	// --------------------
	// All about data pattern
	private static final int _INSTANCE_DIVISION = _MAX_ID_POSSIBLE / 2;

	private int _instanceIndex(int targetID)
	{
		return ( targetID <= _INSTANCE_DIVISION ? 0 : 1 );
	}

	private ArrayList< String > _readIDList(String file)
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
		catch ( Exception e )
		{
			// TODO: Implement me!
		}

		return idList;
	}

	private String _retrieveDetails(String targetID)
	{
		int idNum = Integer.parseInt(targetID);

		String result = null;

		// First, try to find from the static cache.
		result = _cacheHighFreq.get(targetID);

		// If the static cache does not have it, try the LargeID cache.
		if (result == null)
		{
			result = _cacheLargeIDs.get(targetID);
		}

		// If the LargeID cache does not have it, then try with the LRU cache.
		if (result == null)
		{
			result = _cacheLRU.get(targetID);
		}

		// If the LRU cache does not have it, then we have to load more.
		if (result == null)
		{
			try
			{
				int startRange = idNum;
				int endRange = idNum + 2 * _RANGE_RADIUS;

				if (idNum > _RANGE_RADIUS)
				{
					startRange = idNum - _RANGE_RADIUS;
					endRange = idNum + _RANGE_RADIUS;
				}

				String resultsString = sendRequest(generateRangeURL(
								_instanceIndex(idNum), startRange, endRange)
				);

				String[] results = resultsString.split(";");

				for (int i = 0; i < results.length; ++i)
				{
					_cacheLRU.put(String.valueOf(startRange + i), results[i]);
				}

				result = _cacheLRU.get(targetID);
			}
			catch(Exception e)
			{
				// TODO: Log the error
			}
		}

		return result;
	}

	// Robin: My code - END

	private String[] databaseInstances = new String[2];
	/* 
	 * init -initializes the variables which store the 
	 *	     DNS of your database instances
	 */
	private void init() {
		/* Add the DNS of your database instances here */
		databaseInstances[0] = "ec2-52-0-46-29.compute-1.amazonaws.com";	// Robin: My DCI#1
		databaseInstances[1] = "ec2-52-1-133-244.compute-1.amazonaws.com";	// Robin: My DCI#2
	}

	/*
	 * checkBackend - verifies that the DCI are running before starting this server
	 */
	private boolean checkBackend() {
		try{
			if(sendRequest(generateURL(0,"1")) == null ||
					sendRequest(generateURL(1,"1")) == null)
				return true;
		} catch (Exception ex) {
			System.out.println("Exception is " + ex);
			return true;
		}

		return false;
	}

	/*
	 * sendRequest
	 * Input: URL
	 * Action: Send a HTTP GET request for that URL and get the response
	 * Returns: The response
	 */
	private String sendRequest(String requestUrl) throws Exception {

		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");

		BufferedReader in = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), "UTF-8"));

		String responseCode = Integer.toString(connection.getResponseCode());
		if(responseCode.startsWith("2")){
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			connection.disconnect();
			return response.toString();
		} else {
			System.out.println("Unable to connect to "+requestUrl+
					". Please check whether the instance is up and also the security group settings");
			connection.disconnect();
			return null;
		}
	}
	/*
	 * generateURL
	 * Input: Instance ID of the Data Center
	 * 		  targetID
	 * Returns: URL which can be used to retrieve the target's details
	 * 			from the data center instance
	 * Additional info: the target's details are cached on backend instance
	 */
	private String generateURL(Integer instanceID, String key) {
		return "http://" + databaseInstances[instanceID] + "/target?targetID=" + key;
	}

	/*
	 * generateRangeURL
	 * Input: 	Instance ID of the Data Center
	 * 		  	startRange - starting range (targetID)
	 *			endRange - ending range (targetID)
	 * Returns: URL which can be used to retrieve the details of all
	 * 			targets in the range from the data center instance
	 * Additional info: the details of the last 10,000 targets are cached
	 * 					in the database instance
	 * 				
	 */
	private String generateRangeURL(Integer instanceID, Integer startRange, Integer endRange) {
		return "http://" + databaseInstances[instanceID] + "/range?start_range="
				+ Integer.toString(startRange) + "&end_range=" + Integer.toString(endRange);
	}

	/* 
	 * retrieveDetails - you have to modify this function to achieve a higher RPS value
	 * Input: the targetID
	 * Returns: The result from querying the database instance
	 */
	private String retrieveDetails(String targetID) {
		try{
			// return sendRequest(generateURL(0, targetID));	// Robin: Old code
			return _retrieveDetails(targetID);	// Robin: My code
		} catch (Exception ex){
			System.out.println(ex);
			return null;
		}
	}

	/* 
	 * processRequest - calls the retrieveDetails function with the targetID
	 */
	private void processRequest(String targetID, HttpServerRequest req) {
		String result = retrieveDetails(targetID);
		if(result != null)
			req.response().end(result);
		else
			req.response().end("No resopnse received");
	}

	/*
	 * start - starts the server
	 */
	public void start() {
		init();
		if(!checkBackend()){

			// Robin: My code - BEGIN
			_constructHighFreqCache();
			_constructLargeIDCache();
			// Robin: My code - END

			vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
				public void handle(HttpServerRequest req) {
					String query_type = req.path();
					req.response().headers().set("Content-Type", "text/plain");

					if(query_type.equals("/target")){
						String key = req.params().get("targetID");
						processRequest(key,req);
					}
					else {
						String key = "1";
						processRequest(key,req);
					}
				}
			}).listen(80);
		} else {
			System.out.println("Please make sure that both your DCI are up and running");
			System.exit(0);
		}
	}
}
