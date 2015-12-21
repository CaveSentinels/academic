package cc.cmu.edu.minisite;

import java.sql.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

import io.undertow.io.Sender;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;

public class MiniSite {



    public MiniSite() throws Exception{
        // Empty
    }

    class FEConst {
        public final static String HTTP_REQUEST_PARAM_USER_ID = "id";
        public final static String HTTP_REQUEST_PARAM_PASSWORD = "pwd";

        public final static String DB_TABLE_NAME_USER = "user";
        public static final String DB_COL_NAME_USER_ID = "Userid";
        public static final String DB_COL_NAME_PASSWORD = "Password";
        public static final String DB_COL_NAME_NAME = "Name";

        public final static String DB_TABLE_NAME_FRIENDS = "friends";
        public final static String DB_COL_FAMILY_FRIENDS = "friends";

        public final static int DB_QUERY2_SELECT_STMT_LENGTH = 150; // The most possible length of a Query statement.

        public final static String DB_KEYWORD_SELECT = "SELECT";
        public final static String DB_KEYWORD_FROM = "FROM";
        public final static String DB_KEYWORD_WHERE = "WHERE";
        public final static String DB_KEYWORD_AND = "AND";

        public static final String UNAUTHORIZED = "Unauthorized";

        public final static String STEP1_DB_DNS = "dbstep1.ct12pppy4tqx.us-east-1.rds.amazonaws.com";
        public final static String STEP1_DB_PORT = "3306";
        public final static String STEP1_DB_NAME = "DBStep1";
        public final static String STEP1_DB_USER_NAME = "robin";
        public final static String STEP1_DB_PASSWORD = "canyoubreakit";

        public static final String STEP2_HBASE_CONFIG_ZOOKEEPER_QUORUM = "hbase.zookeeper.quorum";
        public static final String STEP2_HBASE_CONFIG_ZOOKEEPER_CLIENTPORT = "hbase.zookeeper.property.clientPort";
        public static final String STEP2_HBASE_NODES_PRIVATE_IP_ADDRESSES = "172.31.51.38, 172.31.58.144";
        // http://blog.cloudera.com/blog/2013/07/guide-to-using-apache-hbase-ports/
        public static final String STEP2_HBASE_ZOOKEEPER_CLIENT_PORT = "2181";
        public static final String STEP2_HBASE_VALUE_DELIMITER = ",";

        public static final String STEP3_DYNAMODB_USERID = "userid";
        public static final String STEP3_DYNAMODB_TABLE_NAME = "DBStep3";
        public static final String STEP3_DYNAMODB_URL = "url";
        public static final String STEP3_DYNAMODB_TIME = "time";

        public static final String JSON_NAME_TIME = "time";
        public static final String JSON_NAME_URL = "url";
        public static final String JSON_NAME_FRIENDS = "friends";
        public static final String JSON_NAME_NAME = "name";
        public static final String JSON_NAME_PHOTOS = "photos";
    }

    private static Connection connectMySQL(String dbServerDNS, String dbServerPort, String dbName, String dbUserName, String dbPassword) {
        Connection dbConn;

        try {
            //define the data source
            String sourceURL = "jdbc:mysql://" + dbServerDNS + ":" + dbServerPort + "/" + dbName;

            dbConn = DriverManager.getConnection(sourceURL, dbUserName, dbPassword);
        } catch (Exception e) {
            System.err.println(e.toString());
            dbConn = null;
        }

        return dbConn;
    }

    private static boolean disconnectMySQL(Connection dbConn) {
        boolean successful = true;

        try {
            dbConn.close();
        } catch (Exception e) {
            System.err.println(e.toString());
            successful = false;
        }

        return successful;
    }

    private static String authenticateUser(Connection dbConn, String userID, String password) {
        String response = null;
        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder sb = new StringBuilder(FEConst.DB_QUERY2_SELECT_STMT_LENGTH);
        sb.append(FEConst.DB_KEYWORD_SELECT).append(' ').append(FEConst.DB_COL_NAME_NAME).append(' ')
                .append(FEConst.DB_KEYWORD_FROM).append(' ').append(FEConst.DB_TABLE_NAME_USER).append(' ')
                .append(FEConst.DB_KEYWORD_WHERE).append(' ')
                .append(FEConst.DB_COL_NAME_USER_ID).append('=').append('\"').append(userID).append('\"').append(' ')
                .append(FEConst.DB_KEYWORD_AND).append(' ')
                .append(FEConst.DB_COL_NAME_PASSWORD).append('=').append('\"').append(password).append('\"')
        ;

        try {
            stmt = dbConn.createStatement();
            rs = stmt.executeQuery(sb.toString());

            while (rs.next()) {
                response = rs.getString(FEConst.DB_COL_NAME_NAME);
            }
        } catch(SQLException e) {
            System.out.println(sb.toString());
            System.err.println(
                    "Description: " + e.getCause() + "\n" +
                    "Error Code: " + String.valueOf(e.getErrorCode()) + "\n" +
                    "SQL State: " + e.getSQLState());
        } finally {
            // Release the resources.
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(sb.toString());
                    System.err.println(e.toString());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.out.println(sb.toString());
                    System.err.println(e.toString());
                }
            }
        }

        return (response == null ? FEConst.UNAUTHORIZED : response);
    }

    private static String queryUserName(Connection dbConn, String userID) {
        String response = null;
        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder sb = new StringBuilder(FEConst.DB_QUERY2_SELECT_STMT_LENGTH);
        sb.append(FEConst.DB_KEYWORD_SELECT).append(' ').append(FEConst.DB_COL_NAME_NAME).append(' ')
                .append(FEConst.DB_KEYWORD_FROM).append(' ').append(FEConst.DB_TABLE_NAME_USER).append(' ')
                .append(FEConst.DB_KEYWORD_WHERE).append(' ')
                .append(FEConst.DB_COL_NAME_USER_ID).append('=').append('\"').append(userID).append('\"').append(' ')
        ;

        try {
            stmt = dbConn.createStatement();
            rs = stmt.executeQuery(sb.toString());

            while (rs.next()) {
                response = rs.getString(FEConst.DB_COL_NAME_NAME);
            }
        } catch(SQLException e) {
            System.out.println(sb.toString());
            System.err.println(
                    "Description: " + e.getCause() + "\n" +
                            "Error Code: " + String.valueOf(e.getErrorCode()) + "\n" +
                            "SQL State: " + e.getSQLState());
        } finally {
            // Release the resources.
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(sb.toString());
                    System.err.println(e.toString());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.out.println(sb.toString());
                    System.err.println(e.toString());
                }
            }
        }

        return (response == null ? "" : response);
    }

    private static String authenticateUser(String userID, String password) {
        String result = "";

        Connection dbConn = connectMySQL(
                FEConst.STEP1_DB_DNS,
                FEConst.STEP1_DB_PORT,
                FEConst.STEP1_DB_NAME,
                FEConst.STEP1_DB_USER_NAME,
                FEConst.STEP1_DB_PASSWORD);

        if (dbConn != null) {
            result = authenticateUser(dbConn, userID, password);
            disconnectMySQL(dbConn);
        }

        return result;
    }

    private static String fetchFriendName(String friendID) {
        String result = "";

        Connection dbConn = connectMySQL(
                FEConst.STEP1_DB_DNS,
                FEConst.STEP1_DB_PORT,
                FEConst.STEP1_DB_NAME,
                FEConst.STEP1_DB_USER_NAME,
                FEConst.STEP1_DB_PASSWORD);

        if (dbConn != null) {
            result = queryUserName(dbConn, friendID);
            disconnectMySQL(dbConn);
        }

        return result;
    }

    private static JSONObject handleStep1(final HttpServerExchange exchange) {
        JSONObject responseObj = new JSONObject();

        // Retrieve the user ID and password passed in.
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        Deque<String> userIDs = params.get(FEConst.HTTP_REQUEST_PARAM_USER_ID);
        Deque<String> passwords = params.get(FEConst.HTTP_REQUEST_PARAM_PASSWORD);
        String userID = userIDs.getFirst();
        String password = passwords.getFirst();

        // Query the user id + password
        String result = authenticateUser(userID, password);

        // Make the response object.
        responseObj.put(FEConst.JSON_NAME_NAME, result);

        return responseObj;
    }

    private static String[] fetchFriends(String userID) {
        // Query the user ID.
        HTable table;

        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        config.set(FEConst.STEP2_HBASE_CONFIG_ZOOKEEPER_QUORUM, FEConst.STEP2_HBASE_NODES_PRIVATE_IP_ADDRESSES);
        config.set(FEConst.STEP2_HBASE_CONFIG_ZOOKEEPER_CLIENTPORT, FEConst.STEP2_HBASE_ZOOKEEPER_CLIENT_PORT);

        String valueStr = "";
        try {
            table = new HTable(config, FEConst.DB_TABLE_NAME_FRIENDS);

            Get g = new Get(Bytes.toBytes(userID));

            Result r = table.get(g);

            byte[] value = r.getValue(Bytes.toBytes(FEConst.DB_COL_FAMILY_FRIENDS), null/*no qualifier*/);

            valueStr = Bytes.toString(value);
        } catch(Exception e) {
            System.err.println(e.toString());
        }

        // Parse the value string.
        String tmpValue = valueStr.substring(0, valueStr.length()-1);   // Remove the '\n'

        return tmpValue.split(FEConst.STEP2_HBASE_VALUE_DELIMITER);
    }

    private static JSONObject handleStep2(final HttpServerExchange exchange) {
        JSONObject responseObj = new JSONObject();

        // Retrieve the user ID and password passed in.
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        Deque<String> userIDs = params.get(FEConst.HTTP_REQUEST_PARAM_USER_ID);
        String userID = userIDs.getFirst();

        // Fetch all the friends' IDs.
        String [] friendIDs = fetchFriends(userID);

        // Make the response object.
        JSONArray friends = new JSONArray();
        for (int i = 0; i < friendIDs.length; ++i) {
            if (!friendIDs[i].isEmpty()) {
                JSONObject friend = new JSONObject();
                friend.put(FEConst.STEP3_DYNAMODB_USERID, friendIDs[i]);
                friends.add(friend);
            }
        }

        responseObj.put(FEConst.JSON_NAME_FRIENDS, friends);

        return responseObj;
    }

    private static ProfileCredentialsProvider credentials = new ProfileCredentialsProvider();

    private static Map<String, AttributeValue> fetchProfileInfo(String userID) {
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentials);
        GetItemRequest getItemRequest = new GetItemRequest();
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put(FEConst.STEP3_DYNAMODB_USERID, new AttributeValue().withN(userID));
        getItemRequest.withTableName(FEConst.STEP3_DYNAMODB_TABLE_NAME).withKey(key);
        GetItemResult getItemResult = dynamoDBClient.getItem(getItemRequest);

        return getItemResult.getItem();
    }

    private static JSONObject handleStep3(final HttpServerExchange exchange) {
        JSONObject responseObj = new JSONObject();

        // Retrieve the user ID and password passed in.
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        Deque<String> userIDs = params.get(FEConst.HTTP_REQUEST_PARAM_USER_ID);
        String userID = userIDs.getFirst();

        // Query the database for the image information.
        Map<String, AttributeValue> result = fetchProfileInfo(userID);

        String createTime = result.get(FEConst.STEP3_DYNAMODB_TIME).getS();
        String imageURL = result.get(FEConst.STEP3_DYNAMODB_URL).getS();

        responseObj.put(FEConst.JSON_NAME_TIME, createTime);
        responseObj.put(FEConst.JSON_NAME_URL, imageURL);

        return responseObj;
    }

    private static JSONObject handleStep4(final HttpServerExchange exchange) {
        JSONObject responseObj = new JSONObject();
        JSONArray photos = new JSONArray();

        // Retrieve the user ID and password passed in.
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        Deque<String> userIDs = params.get(FEConst.HTTP_REQUEST_PARAM_USER_ID);
        Deque<String> passwords = params.get(FEConst.HTTP_REQUEST_PARAM_PASSWORD);
        String userID = userIDs.getFirst();
        String password = passwords.getFirst();

        System.out.println("[INFO] UserID: " + userID + "; " + "Password: " + password);

        // Try to authenticate the user
        String userName = authenticateUser(userID, password);
        if (userName.equals(FEConst.UNAUTHORIZED)) {
            // Failed to be authenticated
            responseObj.put(FEConst.JSON_NAME_NAME, userName);
            responseObj.put(FEConst.JSON_NAME_PHOTOS, photos);
            return responseObj;
        }

        // Authentication succeeded.
        // Fetch all the friends
        ArrayList<String> friendNames = new ArrayList<String>();
        ArrayList<String> imageURLs = new ArrayList<String>();
        ArrayList<String> imageTimes = new ArrayList<String>();
        String [] friendIDs = fetchFriends(userID);
        for (String friendID : friendIDs) {

            // Get friend's name.
            String friendName = fetchFriendName(friendID);

            // Get friend's profile image and follow time.
            Map<String, AttributeValue> friendInfo = fetchProfileInfo(friendID);
            String imageURL = friendInfo.get(FEConst.STEP3_DYNAMODB_URL).getS();
            String createTime = friendInfo.get(FEConst.STEP3_DYNAMODB_TIME).getS();

            // Now sort according to the time and then name.
            int i = 0;
            while (i < friendNames.size()) {
                int compTime = createTime.compareTo(imageTimes.get(i));
                int compName = friendName.compareTo(friendNames.get(i));
                if ((compTime < 0) || (compTime == 0 && compName < 0)) {
                    // IF createTime is earlier than imageTimes.get(i) OR
                    // createTime is the same as imageTimes.get(i) but the friendName
                    // precedes friendNames.get(i)...
                    imageTimes.add(i, createTime);
                    imageURLs.add(i, imageURL);
                    friendNames.add(i, friendName);
                    break;
                }
                ++i;
            }
            // Just in case this person is the last one, we should not forget to
            // add him/her.
            if (i >= friendNames.size()) {
                imageTimes.add(createTime);
                imageURLs.add(imageURL);
                friendNames.add(friendName);
            }
        }

        // Make the response object.
        for (int i = 0; i < friendNames.size(); ++i) {
            JSONObject friendPhoto = new JSONObject();

            friendPhoto.put(FEConst.JSON_NAME_NAME, friendNames.get(i));
            friendPhoto.put(FEConst.JSON_NAME_URL, imageURLs.get(i));
            friendPhoto.put(FEConst.JSON_NAME_TIME, imageTimes.get(i));

            photos.add(friendPhoto);
        }

        responseObj.put(FEConst.JSON_NAME_NAME, userName);
        responseObj.put(FEConst.JSON_NAME_PHOTOS, photos);

        return responseObj;
    }

    // ------------------------------


    public static void main(String[] args) throws Exception{
        final MiniSite minisite = new MiniSite();
        final ObjectMapper mapper = new ObjectMapper();
        
        Undertow.builder()
        .addHttpListener(8080, "0.0.0.0")
                .setHandler(new HttpHandler() {

                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; encoding=UTF-8");
                        Sender sender = exchange.getResponseSender();

                        JSONObject response = new JSONObject();

                        // Determine which step it is.
                        String step = exchange.getRequestPath();
                        System.out.println("[INFO] Step to handle: " + step);
                        if (step.equals("/step1")) {
                            response = handleStep1(exchange);
                        } else if (step.equals("/step2")) {
                            response = handleStep2(exchange);
                        } else if (step.equals("/step3")) {
                            response = handleStep3(exchange);
                        } else if (step.equals("/step4")) {
                            response = handleStep4(exchange);
                        } else {
                            System.err.println("ERROR: Unrecognized step: " + step);
                            response.put("", "");
                        }

                        String content = "returnRes(" + mapper.writeValueAsString(response) + ")";
                        System.out.println("[INFO] Content to return: " + content);
                        sender.send(content);
                    }
                }).build().start();
    }
}

