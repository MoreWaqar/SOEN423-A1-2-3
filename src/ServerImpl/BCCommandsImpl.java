package ServerImpl;

import Interface.commandsInterface;
import Model.Customer;
import Model.Item;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BCCommandsImpl extends UnicastRemoteObject implements commandsInterface {

    private Map<String, Item> Stock;
    private static Map<String, Queue> WaitList;
    private Map <String, Customer> Customers;
    private ArrayList<String> foreignCustomers ;




    public BCCommandsImpl() throws RemoteException{
        super();
        try {
            this.Stock = new HashMap<>();
            WaitList = new HashMap<>();
            this.Customers = new HashMap<>();
            this.foreignCustomers = new ArrayList<String>();
            Stock.put("BC1012", new Item("1012", "Coke_Zero", "BC",12, 15));
            Stock.put("BC1023", new Item("1023", "Coke_Green", "BC",23, 8));
            Stock.put("BC1034", new Item("1034", "Coke", "BC",11, 16));
            Stock.put("BC1045", new Item("1045", "Diet_Pepsi", "BC",19, 6));
            Stock.put("BC1060", new Item("1060", "Pepsi", "BC",98, 25));
            Stock.put("BC1061", new Item("1061", "BC_Cola", "BC",501, 25));
            Stock.put("BC1085", new Item("1085", "AB_Cola", "BC",0, 34));
            Stock.put("BC1100", new Item("1100", "RICH", "BC",2, 3400));
            Stock.put("BC1200", new Item("1100", "RICH", "BC",2, 501));
            Customers.put("BCU1001", new Customer());
            Customers.put("BCU1500", new Customer());


        }catch(Exception e){
            System.out.println(e);
        }
    }

    public String addItem(String managerID, String itemID, String itemName,int qty, int price) throws java.rmi.RemoteException{
        try {
            qty = emptyWaitlist(itemID, qty);
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty()+qty);
            String logMessage = "\naddItem Executed on existing item by " + managerID + " | Modifications successfully made to Server BC | " +
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n\n";
            writeLog(logMessage);
            return (logMessage);
        }catch(Exception e) {
            Stock.put(itemID, new Item(itemID.substring(2, 6), itemName, itemID.substring(0, 2), qty, price));
            this.Stock.get(itemID);
            String logMessage = "\naddItem Executed on newly added item by " + managerID + " | Modifications successfully made to Server BC | " +
                    "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n\n";
            return (logMessage);
        }
    }

    public int emptyWaitlist(String itemId,int qty){
        itemId = "BC" + itemId.substring(2,6);
        try{
            while(qty > 0 && !this.WaitList.get(itemId).isEmpty()){
                String satisfiedCustomer = (String) this.WaitList.get(itemId).poll();
                String logMessage = "\npurchaseItem Executed on waitlist item by " + satisfiedCustomer
                        + " | Modifications made to Server BC |\n " + "Updated Values \n ID | Qty \n" + itemId
                        +" | "
                        + --qty + "\n";
                writeLog(logMessage);
                System.out.println(logMessage);

            }
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
                String returnMessage = "("+ (returnTimeStamp()) + ") "+"\nremoveItem Executed on existing item by " + managerID
                        + " | Modifications not made to Server BC | Desired Removal " + qty
                        + " higher than current quantity: " + currentQuantity +"\n\n";
                writeLog(returnMessage);
                return returnMessage;
            }
            Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty()-qty);
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"\nremoveItem Executed on existing item by " + managerID
                    + " | Modifications successfully made to Server BC |"+
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
        if (validateManager(managerID)){
            String items = "ID | Item Name | Qty \n";
            for (String i : this.Stock.keySet()){
                items = items.concat(this.Stock.get(i).getItemID() + " | " + this.Stock.get(i).getItemName() + " | "
                        + this.Stock.get(i).getItemQty() + "\n\n");
            }
            return items;

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
                        + " | Modifications made to Server BC |\n " + "Updated Values \n ID | Item Name | Qty \n" + this.Stock.get(logID).getItemID()
                        + " | " + this.Stock.get(logID).getItemName() + " | "
                        + this.Stock.get(logID).getItemQty() + "\n";
                writeLog(logMessage);
                return locallyAvailable;
            }
            else{
                String ONItem = sendUDP(2001, customerID, itemID, "purchaseItem",0);
                if(!ONItem.startsWith("410"))  {
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"purchaseItem Executed on in-stock out-of-server item by " + customerID
                            + " | Modifications made to Server ON |\n ";
                    writeLog(logMessage);
                    return ONItem;
                }
                String QCItem = sendUDP(2003, customerID, itemID, "purchaseItem",0);
                if(!QCItem.startsWith("410")){
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"purchaseItem Executed on in-stock out-of-server item by " + customerID
                            + " | Modifications made to Server QC |\n ";
                    writeLog(logMessage);
                    return QCItem;
                }
                if(QCItem.startsWith("41010") || ONItem.startsWith("41010")|| locallyAvailable.startsWith("41010")){
                    return "You have already used your one purchase at this foreign store per company policy";
                }


            }
            writeLog("Purchase request by " + customerID +". There is no stock for this item in any of our stores. Item ID: "+ itemID + " \n" + "Customer added to waitlist");
            addToWaitList(customerID,logID);
            return("There is no stock for this item in any of our stores. Customer added to waitlist");
        }catch(Exception e){
            System.out.println("400");
        }
        try{
            writeLog("Purchase request by " + customerID +". There is no stock for this item in any of our stores. Item ID: "+ itemID + " \n");
            writeLog("Customer" + customerID + " added to waitlist for Item ID: " + itemID);
            addToWaitList(customerID,logID);

        }catch(Exception e){
            e.printStackTrace();
        }
        return "There is no stock for this item in any of our stores. Customer added to waitlist";
    }

    public String purchaseLocalItem(String customerID, String itemID){
        itemID = "BC"+itemID;
        try {
            if (enoughStock(itemID)) {
                boolean flag = !dealWithBudget(customerID, itemID);
                System.out.println("flag: " + flag);
                if(flag){
                    return "Customer does not have enough balance for this purchase";
                }
                if(!firstShop(customerID))
                    return "41010";
                Stock.get(itemID).setItemQty(Stock.get(itemID).getItemQty() - 1);
                String returnMessage = "("+ (returnTimeStamp()) + ") "+"Sale successful. Updated Stock for this item \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID()
                        + " | " + this.Stock.get(itemID).getItemName() + " | "
                        + this.Stock.get(itemID).getItemQty() + "\n";
                return returnMessage;
            } else {
                return ("410");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return("410");
    }

    public boolean firstShop(String customerId){
        String search = "";
        System.out.println(customerId);
        if(!customerId.substring(0,2).equals("BC")){
            for (int i=0; i<foreignCustomers.size();i++){
                if(foreignCustomers.get(i).equals(customerId))
                    search = foreignCustomers.get(i);
            }
            if(search.equals("")){
                foreignCustomers.add(customerId);
                System.out.println(foreignCustomers);
                return true;
            }else return false;

        }
        return true;

    }

    public boolean dealWithBudget(String customerId, String itemID){
        try{
            if(customerId.substring(0,2).equals("QC")){
                String QCItem = sendUDP(2003, customerId, itemID, "getBudget",0);
                int cost = this.Stock.get(itemID).getPrice();
                int budget = Integer.parseInt(QCItem.trim());
                boolean check = budget >= cost;
                if(budget >= this.Stock.get(itemID).getPrice()){
                    System.out.println(dealWithCosts(customerId, cost));
                    return true;
                }
            }else if(customerId.substring(0,2).equals("BC")){
                String BCItem = sendUDP(2002, customerId, itemID, "getBudget",0);
                int cost = this.Stock.get(itemID).getPrice();
                int budget = Integer.parseInt(BCItem.trim());
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId, cost));
                    return true;
                }
            }else if(customerId.substring(0,2).equals("ON")){
                String ONItem = sendUDP(2001, customerId, itemID, "getBudget",0);
                int cost = this.Stock.get(itemID).getPrice();
                int budget = Integer.parseInt(ONItem.trim());
                if(budget >= cost){
                    System.out.println(dealWithCosts(customerId, cost));
                    return true;
                }

            }
        }catch(Exception e){
            e.printStackTrace();
            this.Customers.put(itemID, new Customer());
            return false;
        }
        return false;
    }

    public String dealWithCosts(String customerId, int cost){
        if(customerId.substring(0,2).equals("QC")){
            String QCItem = sendUDP(2003, customerId, "itemID", "setBudget",cost);
            return QCItem;
        }if(customerId.substring(0,2).equals("ON")){
            String ONItem = sendUDP(2001, customerId, "itemID", "setBudget",cost);
            return ONItem;
        }if(customerId.substring(0,2).equals("BC")){
            String BCItem = sendUDP(2002, customerId, "itemID", "setBudget",cost);
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


    public String returnItem(String customerID, String itemID, String dateOfReturn) throws java.rmi.RemoteException{
        try{
            if (returnPossible(dateOfReturn)){
                if(itemID.substring(0,2).equals("QC")){
                    String QCItem = sendUDP(2003, customerID, itemID, "returnItem",0);
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server QC |\n " +QCItem + "\n";
                    writeLog(logMessage);
                    return QCItem;
                }else if(itemID.substring(0,2).equals("ON")){
                    String ONItem = sendUDP(2001, customerID, itemID, "returnItem",0);
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server ON |\n " + ONItem + "\n";
                    writeLog(logMessage);
                    return ONItem;
                }else if(itemID.substring(0,2).equals("BC")){
                    String BCItem = sendUDP(2002, customerID, itemID, "returnItem",0);
                    String logMessage = "("+ (returnTimeStamp()) + ") "+"ReturnItem Executed on in-stock item by " + customerID
                            + " | Modifications made to Server BC |\n " +BCItem + "\n";
                    writeLog(logMessage);
                    return BCItem;
                }
            }else{
                return "Return no longer possible. This article is not covered under the return policy";
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new String("400");
    }

    public String returnLocalStock(String itemID){
        try{
            itemID = "BC"+itemID.substring(2,6);
            int qty = 1;
            qty = emptyWaitlist(itemID, qty);
            if(qty == 0){
                return "Return Successful, the item was assigned to a client on the waitlist";
            }
            this.Stock.get(itemID).setItemQty(this.Stock.get(itemID).getItemQty()+qty);
            String returnMessage = "("+ (returnTimeStamp()) + ") "+"Return successful. Updated Stock for this item \n ID | Item Name | Qty \n" + this.Stock.get(itemID).getItemID()
                    + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
            return returnMessage;
        }catch (Exception e){
            this.Stock.put(itemID,new Item(itemID.substring(2,6), "No Longer Sold Return", "QC",1, 100000));
            e.printStackTrace();
        }
        String returnMessage = "("+ (returnTimeStamp()) + ") "+"Return successful. Item stocked, but no longer in regular sale items Price adjusted until manager input \n ID | Item Name | Qty \n"
                + this.Stock.get(itemID).getItemID() + " | " + this.Stock.get(itemID).getItemName() + " | "
                + this.Stock.get(itemID).getItemQty() + "\n";
        return returnMessage;
    }

    public String findItem(String customerID,  String itemName) throws java.rmi.RemoteException{
        try{
            String localItem = sendUDP(2002, customerID, itemName,"findItem",0);
            String ONItem = sendUDP(2001, customerID, itemName,"findItem",0);
            String QCItem = sendUDP(2003, customerID, itemName,"findItem",0);
            String returnMessage = localItem+ONItem+QCItem;
            String logMessage = "("+ (returnTimeStamp()) + ") "+"findItem executed by " + customerID
                    + " | Modifications not made to Servers | Logged Response :  \n" + returnMessage;
            writeLog(logMessage);
            return returnMessage;
        }catch(Exception e){
            e.printStackTrace();
        }
        return new String("No Stock at the BC Store");
    }

    public String findLocalItem(String itemName){
        String itemID = getItemIDbyName(itemName);
        String localItem;
        try {
            itemID = "BC" + itemID;
             localItem = itemID + " | " + this.Stock.get(itemID).getItemName() + " | "
                    + this.Stock.get(itemID).getItemQty() + "\n";
        }catch(Exception e){
            return "No Stock of this item at the BC Store \n";
        }
        return localItem;
    }

    private String getItemIDbyName(String itemName){
        for (String i : this.Stock.keySet()){
            if (this.Stock.get(i).getItemName().equals(itemName)){
                return this.Stock.get(i).getItemID();
            }

        }
        return "404014";
    }

    public void writeLog(){

    }

    public static boolean validateManager(String username){
        boolean valid = username.substring(2,3).equals("M") ? true: false;
        return valid;

    }

    public boolean enoughStock(String key){
        key = "BC" + key.substring(2,6);
        try{
            if(this.Stock.get(key).getItemQty() > 0){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            ;return false;
        }
        return false;
    }


    public void addToWaitList(String customerID, String itemID){
        try {
            itemID = "BC"+itemID.substring(2,6);
            WaitList.get(itemID).add(customerID);
        }
        catch(Exception e){
            Queue queue = new LinkedList();
            queue.add(customerID);
            WaitList.put(itemID,queue);
        }
    }

    public static void writeLog(String message) throws IOException {
        String filePath = "C:\\Users\\Waqar's PC\\IdeaProjects\\SOEN423-A1\\src\\ServerLogs\\BCServer.txt";
        try {
            File myObj = new File(filePath);
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

    public static boolean returnPossible(String formattedDate){
        int day = Integer.parseInt(formattedDate.substring(0,2));
        int month = Integer.parseInt(formattedDate.substring(2,4));
        int year = Integer.parseInt(formattedDate.substring(4,8));
        LocalDate oldTime = LocalDate.of(year, month, day);
        LocalDate maxReturnDate  = LocalDate.now().minusDays(30);
        boolean isPossible = oldTime.isAfter(maxReturnDate);
        return isPossible;
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

    public static String returnTimeStamp(){
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm");
        String returnTime = formattedTime.format(currentTime);
        return returnTime;
    }

}