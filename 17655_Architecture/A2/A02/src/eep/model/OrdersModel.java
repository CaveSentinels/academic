/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eep.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

/**
 *
 * @author walid
 */
public class OrdersModel {
    
    private static OrdersModel instance;
    
    public static OrdersModel getInstance(){
        if (instance == null){
            instance = new OrdersModel();
        }
        
        return instance;
    }
    
    
    private Connection getConnection(String SQLServerIP) throws Exception{
        String msgString = null;            // String for displaying non-error messages

        // Connect to the inventory database
        try
        {
            msgString = "\n>> Establishing Driver...";
            //Load J Connector for MySQL - explicit loads are not needed for 
            //connectors that are version 4 and better
            //Class.forName( "com.mysql.jdbc.Driver" );

            msgString = "\n>> Setting up URL...";

            //define the data source
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306/orderinfo";

            msgString = "\n>> Establishing connection with: " + sourceURL + "...";

            //create a connection to the db - note the default account is "remote"
            //and the password is "remote_pass" - you will have to set this
            //account up in your database
            
            return DriverManager.getConnection(sourceURL,"remote","remote_pass");

        } catch (Exception e) {

            throw new Exception(msgString + "\nProblem connecting to database:: " + e);
            

        } // end try-catch
        
    }
    
    

    public String getProducts(String SQLServerIP, String tableName) {
        // Database parameters
        String msgString = "";            // String for displaying non-error messages
        Connection DBConn = null;
        ResultSet res = null;               // SQL query result set pointer
        Statement s = null;                 // SQL statement pointer
        
        try {
            DBConn = getConnection(SQLServerIP);
            
        } catch (Exception ex) {
            return ex.getMessage();
        }
        
                      
        try
        {
            s = DBConn.createStatement();
            res = s.executeQuery( "Select * from " + tableName );

            //Display the data in the textarea            

            while (res.next())
            {
                msgString += res.getString(1) + " : " + res.getString(2) +
                        " : $"+ res.getString(4) + " : " + res.getString(3)
                        + " units in stock\n";

            } // while

        } catch (Exception e) {

            return "\nProblem getting " + tableName + " inventory:: " + e;

        } // end try-catch
        
        return msgString;
    }
    
    
    public String submitOrder(String SQLServerIP, String firstName, String lastName, String phoneNumber,
                                String customerAddress, String sTotalCost, String lines){
        int beginIndex;                 // String parsing index
        int endIndex;                   // String paring index
        Connection DBConn = null;       // MySQL connection handle
        float fCost;                    // Total order cost
        String description;             // Tree, seed, or shrub description
        String errString = null;        // String for displaying errors
        int executeUpdateVal;           // Return value from execute indicating effected rows
        String msgString = null;        // String for displaying non-error messages
        String orderTableName = null;   // This is the name of the table that lists the items
        String sPerUnitCost = null;     // String representation of per unit cost
        String orderItem = null;        // Order line item from jTextArea2
        Float perUnitCost;              // Cost per tree, seed, or shrub unit
        String productID = null;        // Product id of tree, seed, or shrub
        Statement s = null;             // SQL statement pointer
        String SQLstatement = null;     // String for building SQL queries

        try {
            DBConn = getConnection(SQLServerIP);
            
        } catch (Exception ex) {
            return ex.getMessage();
        }
        
        
     
      
        Calendar rightNow = Calendar.getInstance();

        int TheHour = rightNow.get(rightNow.HOUR_OF_DAY);
        int TheMinute = rightNow.get(rightNow.MINUTE);
        int TheSecond = rightNow.get(rightNow.SECOND);
        int TheDay = rightNow.get(rightNow.DAY_OF_WEEK);
        int TheMonth = rightNow.get(rightNow.MONTH);
        int TheYear = rightNow.get(rightNow.YEAR);
        orderTableName = "order" + String.valueOf(rightNow.getTimeInMillis());

        String dateTimeStamp = TheMonth + "/" + TheDay + "/" + TheYear + " "
                + TheHour + ":" + TheMinute  + ":" + TheSecond;
        
        beginIndex = 0;
        beginIndex = sTotalCost.indexOf("$",beginIndex)+1;
        sTotalCost = sTotalCost.substring(beginIndex, sTotalCost.length());
        fCost = Float.parseFloat(sTotalCost);

        try
        {
            s = DBConn.createStatement();

            SQLstatement = ( "CREATE TABLE " + orderTableName +
                        "(item_id int unsigned not null auto_increment primary key, " +
                        "product_id varchar(20), description varchar(80), " +
                        "item_price float(7,2) );");

            executeUpdateVal = s.executeUpdate(SQLstatement);

        } catch (Exception e) {

            return "\nProblem creating order table " + orderTableName +":: " + e;
            

        } // try


        try
        {
            SQLstatement = ( "INSERT INTO orders (order_date, " + "first_name, " +
                "last_name, address, phone, total_cost, shipped, " +
                "ordertable) VALUES ( '" + dateTimeStamp + "', " +
                "'" + firstName + "', " + "'" + lastName + "', " +
                "'" + customerAddress + "', " + "'" + phoneNumber + "', " +
                fCost + ", " + false + ", '" + orderTableName +"' );");

            executeUpdateVal = s.executeUpdate(SQLstatement);

        } catch (Exception e1) {

            errString =  "\nProblem with inserting into table orders:: " + e1;

            try
            {
                SQLstatement = ( "DROP TABLE " + orderTableName + ";" );
                executeUpdateVal = s.executeUpdate(SQLstatement);

            } catch (Exception e2) {

                errString +=  "\nProblem deleting unused order table:: " +
                        orderTableName + ":: " + e2;
                
                return errString;

            } // try

        } // try



        // Now, if there is no connect or SQL execution errors at this point, 
        // then we have an order added to the orderinfo::orders table, and a 
        // new ordersXXXX table created. Here we insert the list of items in
        // jTextArea2 into the ordersXXXX table.

        // Now we create a table that contains the itemized list
        // of stuff that is associated with the order

        String[] items = lines.split("\\n");
        msgString = "";
        
        for (int i = 0; i < items.length; i++ )
        {
            orderItem = items[i];
            msgString += "\nitem #:" + i + ": " + items[i];

            // Check just to make sure that a blank line was not stuck in
            // there... just in case.

            if (orderItem.length() > 0 )
            {
                // Parse out the product id
                beginIndex = 0;
                endIndex = orderItem.indexOf(" : ",beginIndex);
                productID = orderItem.substring(beginIndex,endIndex);

                // Parse out the description text
                beginIndex = endIndex + 3; //skip over " : "
                endIndex = orderItem.indexOf(" : ",beginIndex);
                description = orderItem.substring(beginIndex,endIndex);

                // Parse out the item cost
                beginIndex = endIndex + 4; //skip over " : $"
                //endIndex = orderItem.indexOf(" : ",orderItem.length());
                //sPerUnitCost = orderItem.substring(beginIndex,endIndex);
                sPerUnitCost = orderItem.substring(beginIndex,orderItem.length());
                perUnitCost = Float.parseFloat(sPerUnitCost);

                SQLstatement = ( "INSERT INTO " + orderTableName +
                    " (product_id, description, item_price) " +
                    "VALUES ( '" + productID + "', " + "'" +
                    description + "', " + perUnitCost + " );");
                try
                {
                    executeUpdateVal = s.executeUpdate(SQLstatement);
                    msgString =  "\nORDER SUBMITTED FOR: " + firstName + " " + lastName;

                    

                } catch (Exception e) {

                    msgString +=  "\nProblem with inserting into table " + orderTableName +
                        ":: " + e;

                } // try

            } // line length check

        } //for each line of text in order table
        
        return msgString;
                
    }
    

    
    
    
}
