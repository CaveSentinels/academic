/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eep.model;

import eep.view.HomeScreenView;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author walid
 */
public class LoginModel {
    
    private static LoginModel instance;
    
    public static LoginModel getInstance(){
        if (instance == null){
            instance = new LoginModel();
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

            throw new Exception("\nProblem connecting to database");
            

        } // end try-catch
        
    }
    
    
    
    public String[] login(String SQLServerIP, String userNameLocal, String passwordHash){
        String retrievedPassword = "";     // String for building SQL queries
        Connection DBConn = null;       // MySQL connection handle
        Statement s = null;                 // SQL statement pointer
        ResultSet res = null;               // SQL query result set pointer
        
        try {
            DBConn = getConnection(SQLServerIP);
            
        } catch (Exception ex) {
            return new String[] {ex.getMessage()};
        }
        
        try
        {                                           
            
            s = DBConn.createStatement();
            res = s.executeQuery( "Select pwd, Inventory, Orders, Shipping  from users WHERE UserName='"+userNameLocal+"'" );
            
            String inventory = "";
            String orders = "";
            String shipping = "";
            
            while (res.next())
                {
                    retrievedPassword = res.getString(1);
                    inventory = res.getString(2);
                    orders = res.getString(3);
                    shipping = res.getString(4);
                    
                }
            
            return new String[] {retrievedPassword, inventory, orders, shipping};                        
            
        }
        catch (Exception ex)
        {
            return new String[] {ex.getMessage()};
            
        }
    }

    public void logActivity(String userNameLocal, String activity) {
        Connection DBConn = null;       // MySQL connection handle
        Statement s = null;                 // SQL statement pointer
        
        try {
            DBConn = getConnection("localhost");
            
        } catch (Exception ex) {
            Logger.getLogger(LoginModel.class.getName()).log(Level.SEVERE, null, ex);
        }    
        try {
            //both Date and Time
            Date date = new Date(System.currentTimeMillis());
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                
            s = DBConn.createStatement();
            String q = String.format("INSERT INTO logs VALUES (\"%s\", \"%s\", \"%s\")", 
                                    userNameLocal, activity, df.format(date).toString());
            s.executeUpdate(q);
        } catch (SQLException ex) {
            Logger.getLogger(LoginModel.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        
        
    }
        
    
    
}
