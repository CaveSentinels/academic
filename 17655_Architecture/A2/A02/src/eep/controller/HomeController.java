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
import eep.view.LoginView;
import eep.view.OrdersView;
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
public class HomeController {
        
    private HomeScreenView homeView;
    private String username;

    
    public HomeController(String userName, String inventory, String orders, String shipping){   
        this.username = userName;
        this.homeView = new HomeScreenView(this, userName, inventory, orders, shipping);
    }

    public void renderView(){
        if(homeView.shipping.equals("1") && !homeView.orders.equals("1") && !homeView.inventory.equals("1")){
            shippingApp();
            
        } else if(homeView.orders.equals("1") && !homeView.shipping.equals("1") && !homeView.inventory.equals("1")){
            ordersApp();
            
        } else if(homeView.inventory.equals("1") && !homeView.shipping.equals("1") && !homeView.orders.equals("1")){
            inventoryApp();
            
        } else {
            homeView.setVisible(true);            
        }

    }        

    public void logOut() {
        homeView.setVisible(false); //to hide the log in frame        
        LoginController controller = new LoginController();
        controller.logLogout(username);
    }

    public void ordersApp() {
        homeView.setVisible(false); //to hide the log in frame        
        OrdersController controller = new OrdersController(username);
        controller.renderView();                   
    }
    
    public void shippingApp() {
        homeView.setVisible(false); //to hide the log in frame        
        ShippingController controller = new ShippingController(username);
        controller.renderView();                   
    }
    
    public void inventoryApp() {
        homeView.setVisible(false); //to hide the log in frame        
        InventoryController controller = new InventoryController(username);
        controller.renderView();                    
    }
    
    
}
