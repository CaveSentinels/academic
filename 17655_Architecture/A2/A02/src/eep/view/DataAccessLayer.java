package eep.view;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sankalp
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataAccessLayer {
    
    String SQLstatement = null;     // String for building SQL queries
    int executeUpdateVal;           // Return value from execute indicating effected rows
    Connection DBConn = null;       // MySQL connection handle
    Statement s = null;             // SQL statement pointer
    
    
    public void CreateLog(String userName, String activity, String application, String time)
    {        
        try 
        {
            //define the data source
            String SQLServerIP = "localhost";
            String sourceURL = "jdbc:mysql://" + SQLServerIP + ":3306/inventory";
            DBConn = DriverManager.getConnection(sourceURL,"remote","remote_pass");
            
            
            s = DBConn.createStatement();
            SQLstatement = "INSERT INTO logs(UserName, Activity, Application, Time) VALUES('"+userName+"', '"+activity+"', '"+application+"', '"+time+"')";
            executeUpdateVal = s.executeUpdate(SQLstatement);
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
