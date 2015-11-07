/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eep.controller;

import eep.AppRunner;
import eep.model.EEPInventoryModel;
import eep.model.LeafInventoryModel;
import eep.model.OrdersModel;
import eep.view.OrdersView;


/**
 *
 * @author walid
 */
public class OrdersController {
    
    private OrdersView ordersView;
    private final EEPInventoryModel eepInventoryModel;
    private LeafInventoryModel leafInventoryModel;
    private OrdersModel ordersModel;
    private String username;

    
    public OrdersController(String userName){
        this.username = userName;
        
        EEPInventoryModel eepInventoryModel = EEPInventoryModel.getInstance();
        LeafInventoryModel leafInventoryModel = LeafInventoryModel.getInstance();
        OrdersModel ordersModel = OrdersModel.getInstance();
            
        this.eepInventoryModel = eepInventoryModel;
        this.leafInventoryModel = leafInventoryModel;
        this.ordersModel = ordersModel;
        
        this.ordersView = new OrdersView(this, userName);
    }
    
    public void renderView(){
        ordersView.setVisible(true);
    }
    

    public String getTrees(String SQLServerIP) {        
        return eepInventoryModel.getTrees(SQLServerIP);        
    }

    public String getSeeds(String SQLServerIP) {
        return eepInventoryModel.getSeeds(SQLServerIP);        
    }

    public String getShrubs(String SQLServerIP) {
        return eepInventoryModel.getShrubs(SQLServerIP);        
    }

    public String getCultureBoxes(String SQLServerIP) {
        return leafInventoryModel.getCultureBoxes(SQLServerIP);        
    }

    public String getReferenceMaterials(String SQLServerIP) {
        return leafInventoryModel.getReferenceMaterials(SQLServerIP);        
    }

    public String getProcessing(String SQLServerIP) {
        return leafInventoryModel.getProcessing(SQLServerIP);        
    }

    public String getGenomics(String SQLServerIP) {
        return leafInventoryModel.getGenomics(SQLServerIP);        
    }
    
    
    public String submitOrder(String SQLServerIP, String firstName, String lastName, String phoneNumber,
                                String customerAddress, String sTotalCost, String lines){
        
        // Check to make sure there is a first name, last name, address and phone
        if (!((lines.length()>0) 
                && (firstName.length()>0)
                && (lastName.length()>0)
                && (phoneNumber.length()>0)))
        {
            return "\nMissing customer information!!!\n";

        } else {
            return ordersModel.submitOrder(SQLServerIP, firstName, lastName, phoneNumber,
                                        customerAddress, sTotalCost, lines);
        }
        
    }

    public void logOut() {
        ordersView.setVisible(false); //to hide the log in frame        
        LoginController controller = new LoginController();
        controller.logLogout(username);   
    }
}
