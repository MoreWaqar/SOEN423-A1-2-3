package ServerImpl;

import Interface.commandsInterface;
import Model.Customer;
import Model.Item;
import Model.Purchase;
import Model.Waitlist;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.*;

public class QCCommandsImpl extends UnicastRemoteObject implements commandsInterface {

    private Map<String, Item> Stock;
    private static Map<String, Queue> WaitList;
    private Map <String, Customer> Customers;
    private ArrayList<String> foreignCustomers ;




    public QCCommandsImpl() throws RemoteException{
        super();
        try {
            this.Stock = new HashMap<>();
            WaitList = new HashMap<>();
            this.foreignCustomers = new ArrayList<String>();
            this.Customers = new HashMap<>();
            Stock.put("QC1012", new Item("1012", "Coke_Zero", "QC",28, 15));
            Stock.put("QC1023", new Item("1023", "Coke_Green", "QC",1, 8));
            Stock.put("QC1034", new Item("1034", "Coke", "QC",0, 16));
            Stock.put("QC1045", new Item("1045", "Diet_Pepsi", "BC",45, 6));
            Stock.put("QC1060", new Item("1060", "Pepsi", "QC",6, 25));
            Stock.put("QC1061", new Item("1061", "BC_Cola", "QC",0, 25));
            Stock.put("QC1085", new Item("1085", "AB_Cola", "QC",3, 34));
            Customers.put("QCU1001", new Customer());
            Customers.put("QCU1500", new Customer());


        }catch(Exception e){
            System.out.println(e);
        }
    }

    public String addItem(String managerID, String itemID, String itemName,int qty, int price) throws java.rmi.RemoteException{
        try {
            qty = emptyWaitlist(itemID, qty);
            System.out.println(qty);
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty()+qty);
            String logMessage = "\na("+ (returnTimeStamp()) + ") "+"addItem Executed on existing item by " + managerID + " | Modifications successfully made to Server QC | " +
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n\n";
            writeLog(logMessage);
            return (logMessage);
        }catch(Exception e) {
            Stock.put(itemID, new Item(itemID.substring(2, 6), itemName, itemID.substring(0, 2), qty, price));
            this.Stock.get(itemID);
            String logMessage = "\na("+ (returnTimeStamp()) + ") "+"addItem Executed on newly added item by " + managerID + " | Modifications successfully made to Server QC | " +
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n\n";
            try{
                writeLog(logMessage);
            }catch (Exception en) {}
            return (logMessage);
        }
    }

    public int emptyWaitlist(String itemId,int qty){
        itemId = "QC" + itemId.substring(2,6);
        try{
            while(qty > 0 && !this.WaitList.get(itemId).isEmpty()){
                String satisfiedCustomer = (String) this.WaitList.get(itemId).poll();
                String logMessage = "\n("+ (returnTimeStamp()) + ") "+"purchaseItem Executed on waitlist item by " + satisfiedCustomer
                        + " | Modifications made to Server QC |\n " + "Updated Values \n ID | Qty \n" + itemId
                        + " | " + --qty + "\n";
                writeLog(logMessage);
                System.out.println(logMessage);
            }
            return qty;
        }catch (Exception e){

        }
        return qty;
    }


    public String removeItem(String managerID, String itemID, int qty) throws java.rmi.RemoteException{
        try{
            int currentQuantity = Stock.get(itemID).getItemQty();
            if(qty == -1){
                Stock.remove(itemID);
                String returnMessage = "("+ (returnTimeStamp()) + ") "+"removeItem Executed on existing item by " + managerID
                        + " | Modifications made to Server QC | Item Deleted "+"\n\n";
                writeLog(returnMessage);
                return returnMessage;
            }
            if(currentQuantity<qty){
                String returnMessage = "("+ (returnTimeStamp()) + ") "+"removeItem Executed on existing item by " + managerID
                        + " | Modifications not made to Server QC | Desired Removal " + qty
                        + " higher than current quantity: " + currentQuantity +"\n\n";
                writeLog(returnMessage);
                return returnMessage;
            }
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty()-qty);
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"removeItem Executed on existing item by " + managerID
                    + " | Modifications successfully made to Server QC |"+
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n\n";
            writeLog(returnMessage);
            return returnMessage;
        }catch(Exception e){
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"removeItem Executed on by " + managerID
                    + " | Modifications made to Server QC | Desired Item was not or is no longer in store stock \n\n";
            return returnMessage;
        }
    }

    public String listItemAvailability(String managerID) throws java.rmi.RemoteException{
        System.out.println(validateManager(managerID));
        if (validateManager(managerID)){
            String items = "ID | Item Name | Qty \n";
            for (String i : this.Stock.keySet()){
                items = items.concat(this.Stock.get(i).getItemID() + " | " + this.Stock.get(i).getItemName() + " | "
                        + this.Stock.get(i).getItemQty() + "\n\n");
            }
            return "("+ (returnTimeStamp()) + ") "+items;

        }else{
            try{
                return new String("Invalid Access Request");
            }catch(Exception e){

            }
        }
        return("Invalid Access Request");
    }

    public String purchaseItem(String customerID, String itemID, String dateOfPurchase) throws java.rmi.RemoteException{
        String logID = itemID;
        itemID = itemID.substring(2,6);
        try{
            String locallyAvailable = purchaseLocalItem(customerID,itemID);
                if(!locallyAvailable.startsWith("410")){
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"purchaseItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server QC |\n " + "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(logID).getItemID()
                            + " | " + this.Stock.get(logID).getItemName() + " | "
                            + this.Stock.get(logID).getItemQty() + "\n";
                    writeLog(logMessage);
                    return locallyAvailable;
                }
            else{
                String ONItem = sendUDP(2001, customerID, itemID,"purchaseItem",0);
                if(!ONItem.startsWith("410")) {
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"purchaseItem Executed on in-stock out-of-server item by " + customerID
                            + " | Modifications made to Server ON |\n ";
                    writeLog(logMessage);
                    return ONItem;
                }
                String BCItem = sendUDP(2002, customerID, itemID,"purchaseItem",0);
                    if(!BCItem.startsWith("410")){
                        String logMessage = "("+ (returnTimeStamp()) + ") "+"purchaseItem Executed on in-stock out-of-server item by " + customerID
                                + " | Modifications made to Server BC |\n ";
                        writeLog(logMessage);
                        return BCItem;
                    }
                    if(BCItem.startsWith("41010") || ONItem.startsWith("41010")|| locallyAvailable.startsWith("41010")){
                        return "You have already used your one purchase at this foreign store per company policy";
                    }
            }
            writeLog("Purchase request by " + customerID +". There is no stock for this item in any of our stores. Item ID: "+ itemID  + " \n" + "Customer added to waitlist");
            addToWaitList(customerID,logID);
            return("There is no stock for this item in any of our stores. Customer added to waitlist");
        }catch(Exception e){
            System.out.println("400");
        }
        try{
            writeLog("("+ (returnTimeStamp()) + ") "+"Purchase request by " + customerID +". There is no stock for this item in any of our stores. Item ID: "+ itemID  + " \n");
            writeLog("("+ (returnTimeStamp()) + ") "+"Customer" + customerID + " added to waitlist for Item ID: " + itemID);
            addToWaitList(customerID,logID);
        }catch(Exception e){
                e.printStackTrace();
            }
        return "There is no stock for this item in any of our stores. Customer added to waitlist";
    }

    public String purchaseLocalItem(String customerID, String itemID){
        itemID = "QC"+itemID;

        if(enoughStock(itemID)) {
            if(!dealWithBudget(customerID, itemID)){
                return "Customer does not have enough balance for this purchase";
            }
            if(!firstShop(customerID))
                return "41010";
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty() - 1);
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"Sale successful. Updated Stock for this item \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID()
                    + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
            return returnMessage;
        }else{
            return("410");
        }
    }

    public boolean firstShop(String customerId){
        String search = "";
        if(!customerId.substring(0,2).equals("QC")){
            for (int i=0; i<foreignCustomers.size();i++){
                if(foreignCustomers.get(i).equals(customerId))
                    search = foreignCustomers.get(i);
            }
            if(search.equals("")){
                foreignCustomers.add(customerId);
                return true;
            }else return false;

        }
        return true;

    }

    public String returnItem(String customerID, String itemID, String dateOfReturn) throws java.rmi.RemoteException{
        try{
            if (returnPossible(dateOfReturn)){
                if(itemID.substring(0,2).equals("QC")){
                    String QCItem = sendUDP(2003, customerID, itemID,"returnItem",0);
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server QC |\n "+QCItem + "\n";
                    System.out.println(QCItem);
                    writeLog(logMessage);
                    return QCItem;
                }else if(itemID.substring(0,2).equals("ON")){
                    String ONItem = sendUDP(2001, customerID, itemID,"returnItem",0);
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server ON |\n " +ONItem + "\n";
                    writeLog(logMessage);
                    return ONItem;
                }else if(itemID.substring(0,2).equals("BC")){
                    String BCItem = sendUDP(2002, customerID, itemID,"returnItem",0);
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server BC |\n " + BCItem;
                    writeLog(logMessage);
                    return BCItem;
                }
            }else{
                return "("+ (returnTimeStamp()) + ") "+"Return no longer possible. This article is not covered under the return policy";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new String("400");
    }

    public String returnLocalStock(String itemID){
        try{
            itemID = "QC"+itemID.substring(2,6);
            int qty = 1;
            qty = emptyWaitlist(itemID, qty);
            if(qty == 0){
                return "Return Successful, the item was assigned to a client on the waitlist";
            }
            this.Stock.get(itemID).setItemQty(this.Stock.get(itemID).getItemQty()+qty);
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"Return successful. Updated Stock for this item \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID()
                    + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
            System.out.println(returnMessage);
            return returnMessage;
        }catch (Exception e){
            e.printStackTrace();
            this.Stock.put(itemID,new Item(itemID.substring(2,6), "No Longer Sold Return", "QC",1, 100000));
        }
        String returnMessage = "("+ (returnTimeStamp()) + ") "+"Return successful. Item stocked, but no longer in regular sale items Price adjusted until manager input \n ID | Item Name | Qty \n"
                + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                + this.Stock.get(itemID).getItemQty() + "\n";
        return returnMessage;
    }

    public void checkWaitlist(){

    }

    public void addToWaitList(String customerID, String itemID){
        itemID = "QC" + itemID.substring(2,6);
            try {
                WaitList.get(itemID).add(customerID);
            }
            catch(Exception e){
                Queue queue = new LinkedList();
                queue.add(customerID);
                WaitList.put(itemID,queue);
            }
    }

    public String findItem(String customerID,  String itemName) throws java.rmi.RemoteException{
        try{
            String itemID = getItemIDbyName(itemName);
            String localItem = sendUDP(2003, customerID, itemName,"findItem",0);
            String ONItem = sendUDP(2001, customerID, itemName,"findItem",0);
            String BCItem = sendUDP(2002, customerID, itemName,"findItem",0);
            String returnMessage = localItem+ONItem+BCItem;

            String logMessage =  "("+ (returnTimeStamp()) + ") "+ "findItem executed by " + customerID
                    + " | Modifications not made to Servers | Logged Response :  \n" + returnMessage;
            writeLog(logMessage);
           return returnMessage;
        }catch(Exception e){

        }
        return new String("No Stock");
    }

    public String findLocalItem(String itemName){
        String itemID = getItemIDbyName(itemName);
        String localItem;
        try {
            itemID = "QC" + itemID;
            localItem = itemID + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
        }catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
            return "No Stock of this item at the QC Store \n";
        }
        return localItem;
    }

    private String getItemIDbyName(String itemName){
        for (String i : this.Stock.keySet()){
            String itemID = "";
            if (this.Stock.get(i).getItemName().equals(itemName)){
                return this.Stock.get(i).getItemID();
            }

        }
        return "404014";
    }

    public boolean dealWithBudget(String customerId, String itemID){
        try{
            if(customerId.substring(0,2).equals("QC")){
                String QCItem = sendUDP(2003, customerId, itemID, "getBudget",0);
                int budget = Integer.parseInt(QCItem.trim());
                int cost = this.Stock.get(itemID).getPrice();
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId));
                    return true;
                }
            }else if(customerId.substring(0,2).equals("BC")){
                String BCItem = sendUDP(2002, customerId, itemID, "getBudget",0);
                int budget = Integer.parseInt(BCItem.trim());
                int cost = this.Stock.get(itemID).getPrice();
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId));
                    return true;
                }
            }else if(customerId.substring(0,2).equals("ON")){
                String ONItem = sendUDP(2001, customerId, itemID, "getBudget",0);
                int budget = Integer.parseInt(ONItem.trim());
                int cost = this.Stock.get(itemID).getPrice();
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId));
                    return true;
                }

            }
        }catch(Exception e){
            this.Customers.put(itemID, new Customer());
            return false;
        }
        return false;
    }

    public String dealWithCosts(String customerId){
        if(customerId.substring(0,2).equals("QC")){
            String QCItem = sendUDP(2003, customerId, "itemID", "setBudget",0);
            return QCItem;
        }if(customerId.substring(0,2).equals("ON")){
            String ONItem = sendUDP(2001, customerId, "itemID", "setBudget",0);
            return ONItem;
        }if(customerId.substring(0,2).equals("BC")){
            String BCItem = sendUDP(2002, customerId, "itemID", "setBudget",0);
            return BCItem;
        }
        return "200";
    }

    public int getLocalBudget(String customerId){
        try{
            int budget = this.Customers.get(customerId).getBudget();
            return budget;
        }catch (Exception e){
            this.Customers.put(customerId,new Customer());
            return this.Customers.get(customerId).getBudget();
        }
    }

    public String setLocalBudget(String customerId, String cost){
        int newBudget = this.Customers.get(customerId).getBudget()-Integer.parseInt(cost.trim());
        this.Customers.get(customerId).setBudget(newBudget);
        return Integer.toString(newBudget);
    }


    public static boolean validateManager(String username){
        System.out.println(username);
        boolean valid = username.substring(2,3).equals("M") ? true: false;
        return valid;

    }

    public boolean enoughStock(String key){
        key = "QC" + key.substring(2,6);
        try{
        if(this.Stock.get(key).getItemQty() > 0){
            return true;
        }
        }catch (Exception e){
            return false;
        }
        return false;
    }





    public static boolean returnPossible(String formattedDate){
        int day = Integer.parseInt(formattedDate.substring(0,2));
        int month = Integer.parseInt(formattedDate.substring(2,4));
        int year = Integer.parseInt(formattedDate.substring(4,8));
        LocalDate oldTime = LocalDate.of(year, month, day);
        LocalDate maxReturnDate  = LocalDate.now().minusDays(30);
        boolean isPossible = oldTime.isAfter(maxReturnDate);
        return isPossible;
    }

    public static void writeLog(String message) throws IOException {
        String filePath = "C:\\Users\\Waqar's PC\\IdeaProjects\\SOEN423-A1\\src\\ServerLogs\\QCServer.txt";
        try {
            File myObj = new File(filePath);
            System.out.println(myObj.exists());
            if (!myObj.exists()) {
                myObj.createNewFile();
                System.out.println("File created");
                PrintWriter pw = new PrintWriter(new FileWriter(myObj));
                pw.write(message);
                pw.close();
            } else {
                System.out.println("File already exists.");
                PrintWriter pw = new PrintWriter(new FileWriter(myObj, true));
                pw.append(message);
                pw.close();
            }
        }catch(Exception e){}


    }

    public static String returnTimeStamp(){
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm");
        String returnTime = formattedTime.format(currentTime);
        return returnTime;
    }




    private static String sendUDP(int port, String username, String itemId, String action, int cost) {
        DatagramSocket socket = null;
        String UDPMessage = action+"-"+username+"-"+itemId+"-"+cost;
        String result="";
        try {
            result ="";
            socket = new DatagramSocket();
            byte[] messageToSend = UDPMessage.getBytes();
            InetAddress hostName = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(messageToSend, UDPMessage.length(), hostName, port);
            socket.send(request);

            byte[] bf = new byte[256];
            DatagramPacket reply = new DatagramPacket(bf, bf.length);
            socket.receive(reply);
            result = new String(reply.getData());
           // String[] parts = result.split("-");
           // result = parts[0];
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (socket != null)
                socket.close();
        }
        return result;

    }

}
