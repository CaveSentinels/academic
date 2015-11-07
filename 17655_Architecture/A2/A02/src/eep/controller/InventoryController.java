/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eep.controller;

import eep.AppRunner;
import eep.model.EEPInventoryModel;
import eep.model.LeafInventoryModel;
import eep.model.LoginModel;
import eep.view.HomeScreenView;
import eep.view.InventoryView;
import eep.view.LoginView;
import eep.view.OrdersView;
import eep.view.ShippingView;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
public class InventoryController {
        
    private InventoryView nventoryView;
    private String username;

    
    public InventoryController(String userName){   
        this.username = userName;
        this.nventoryView = new InventoryView(this, userName);
    }

    public void renderView(){
        nventoryView.setVisible(true);
    }        

    public void logOut() {
        nventoryView.setVisible(false); //to hide the log in frame        
        LoginController controller = new LoginController();
        controller.logLogout(username);   
    }

    
    
}
