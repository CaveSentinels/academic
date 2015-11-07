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
public class LoginController {
        
    private LoginModel loginModel;
    private LoginView loginView;
    private String username;

    
    public LoginController(){        
        this.loginModel = LoginModel.getInstance();
        this.loginView = new LoginView(this);
    }

    public void renderView(){
        loginView.setVisible(true);
    }
    
    public String getUsername(){
        return username;
    }
    
    public void logLogin(String userNameLocal){
        loginModel.logActivity(userNameLocal, "Log in");
        
        username= userNameLocal;
        loginView.userNameLocal = userNameLocal;
        loginView.setVisible(false);        
    }
    
    public void logLogout(String userNameLocal){
        username= userNameLocal;
        loginView.setVisible(true);
        loginModel.logActivity(userNameLocal, "Log out");
    }
    
    
    public String login(String SQLServerIP, String userNameLocal, String password){
        String passwordHash = null;
        String msgString = "";            // String for displaying messages

        try {
                                    
            byte[] bytesOfMessage = password.getBytes("UTF-8");
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] byteData = md.digest(bytesOfMessage);
            
            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i=0;i<byteData.length;i++) {
                
                // used to make sure you always get the last 8 bits
                String hex=Integer.toHexString(0xff & byteData[i]);
                if(hex.length()==1) sb.append('0');
                sb.append(hex);
            }
            
            passwordHash = sb.toString();
            
            String[] res = loginModel.login(SQLServerIP, userNameLocal, passwordHash);
            
            if(passwordHash.equals(res[0]))
            {
                msgString = "Login Successful";                
                
                logLogin(userNameLocal);
                
//                Thread.sleep(1000);
                
                HomeController homeController = new HomeController(userNameLocal, res[1], res[2], res[3]);
                homeController.renderView();

                
            }
            else
            {
                msgString = "Login failed";
            }
            
        } catch (UnsupportedEncodingException ex) {
            return ex.getMessage();
        } catch (NoSuchAlgorithmException ex) {
            return ex.getMessage();
        }
        
        return msgString;
                
    }
    
}
