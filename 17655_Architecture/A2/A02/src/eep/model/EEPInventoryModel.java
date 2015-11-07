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

/**
 *
 * @author walid
 */
public class EEPInventoryModel {
    
    private static EEPInventoryModel instance;
    
    public static EEPInventoryModel getInstance(){
        if (instance == null){
            instance = new EEPInventoryModel();
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
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306/inventory";

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
    
    
    public String getTrees(String SQLServerIP) {
        return this.getProducts(SQLServerIP, "trees");
    }

    public String getSeeds(String SQLServerIP) {
        return this.getProducts(SQLServerIP, "seeds");
    }

    public String getShrubs(String SQLServerIP) {
        return this.getProducts(SQLServerIP, "shrubs");
    }
    

    
    
    
}
