package User;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.*;

import Interface.commandsInterface;
import StoreApp.Store;
import StoreApp.StoreHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.rmi.Naming;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;

public class serverUser {
    static Store store;

    public static void main(String args[]){
        try {
            String hostName = "rmi://localhost:1099/";
            Scanner sc = new Scanner(System.in);

            while(true) {
                System.out.println("Welcome to the Item Stores");
                System.out.print("Please enter your username : ");
                String username = sc.nextLine().toUpperCase();
                while (!validLogin(username)) {
                    System.out.print("Please enter your username : ");
                    username = sc.nextLine();
                }
                System.out.println("Here are the locations currently available");
                System.out.println("-----");
                String userLocation = userLocation(username).toUpperCase();
                System.out.println("You've been identified to be at the " + userLocation + " location");
                try {
                    ORB orb = ORB.init(args, null);
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                    store = StoreHelper.narrow(ncRef.resolve_str(userLocation));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (username.substring(2, 3).equals("M")) {
                    managerMenu(username, hostName, userLocation, sc);
                } else {
                    userMenu(username, hostName, userLocation, sc);
                }

                System.out.println("App automatically set to loop on purpose for demo purposes");

            }
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void managerMenu(String username, String hostName, String userLocation, Scanner sc) {
        boolean repeatFlag = true;
        while(repeatFlag) {
            System.out.println("What action would you like to carry out?");
            System.out.println("1. See the list of available items");
            System.out.println("2. Add an Item");
            System.out.println("3. Remove an Item");
            System.out.println("Please enter the number of your decision : ");
            try{
               int decision = sc.nextInt();
                if (decision > 3 || decision < 1){
                    continue;
                }
                if(decision == 1){
                    listItemsPath(hostName, sc, username, userLocation);

                }
                if(decision == 2){
                    addAnItem(username, hostName, userLocation, sc);
                }
                if(decision == 3){
                    removeAnItem(username, hostName, userLocation, sc);
                }

            }catch (Exception e){
                System.out.println("Invalid input. Please choose a numerical value");
                repeatFlag = true;
            }
            System.out.println("Whew what a day. Do you want to keep going y/n");
            sc.nextLine();
            String exit = sc.nextLine();
            if (exit.equals("n"))
                repeatFlag = false;
        }
    }
    public static void userMenu(String username, String hostName, String userLocation, Scanner sc) {
        boolean repeatFlag = true;
        while(repeatFlag) {
            System.out.println("What action would you like to carry out?");
            System.out.println("1. Purchase an Item");
            System.out.println("2. Find an Item");
            System.out.println("3. Return an Item");
            System.out.println("4. Exchange an Item");
            System.out.println("Please enter the number of your decision : ");
            try{
                int decision = sc.nextInt();
                if (decision > 4 || decision < 1){
                    continue;
                }
                if(decision == 1){
                    makeAPurchase(username, hostName, sc, userLocation);
                }
                if(decision == 2){
                    findAnItem(username, hostName, sc, userLocation);
                }
                if(decision == 3){
                    makeAReturn(username, hostName, sc, userLocation);
                }
                if(decision == 4){
                    makeAnExchange(username, hostName, sc, userLocation);
                }

            }catch (Exception e){
                System.out.println("Invalid input. Please choose a numerical value");
                repeatFlag = true;
            }
            System.out.println("Whew what a day. Do you want to keep going y/n");
            sc.nextLine();
            String exit = sc.nextLine();
            if (exit.equals("n"))
                repeatFlag = false;
        }
    }


    public static boolean validLogin(String username){
        if(username.substring(0,3).toUpperCase().equals("QCM") || username.toUpperCase().substring(0,3).equals("QCU")
        ||username.substring(0,3).toUpperCase().equals("ONM") || username.toUpperCase().substring(0,3).equals("ONU")
                ||username.substring(0,3).toUpperCase().equals("BCM") || username.toUpperCase().substring(0,3).equals("BCU")){
            return true;
        }
            return false;
    }

    public static String userLocation(String username){
        return username.substring(0,2);
    }



    public static void addAnItem(String username, String hostName, String userLocation, Scanner sc){
        try {
            System.out.println("Please enter the item ID of the item you want to add");
            String desiredItemID = sc.next();
            System.out.println("Please enter the item name of the item you want to add");
            String desiredItemName = sc.next();
            System.out.println("Please enter the item qty you wish to add");
            int desiredItemQty = sc.nextInt();
            System.out.println("Please enter the item price");
            int desiredItemPrice = sc.nextInt();
            String returnMessage = store.addItem(username,desiredItemID,desiredItemName,desiredItemQty,desiredItemPrice);
            System.out.println(returnMessage);
            writeLog(username, returnMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    public static void removeAnItem(String username, String hostName, String userLocation, Scanner sc){
        try {
            System.out.println("Please enter the item ID of the item you want to remove");
            String desiredItemID = sc.next();
            System.out.println("Please enter the item qty you wish to remove");
            int desiredItemQty = sc.nextInt();
            String returnMessage = store.removeItem(username,desiredItemID, desiredItemQty);
            System.out.println(returnMessage);
            writeLog(username, returnMessage);
        }catch(Exception e){
            System.out.println(e);
        }
    }




    public static void makeAPurchase(String username, String hostName, Scanner sc, String userLocation){
        try {
        System.out.println("Please enter the item ID of the item you want to purchase");
            sc.nextLine();
        String desiredItemID = sc.nextLine();
        String returnMessage = store.purchaseItem(username,desiredItemID, returnCurrentTime());
            System.out.println(returnMessage);
            String logMessage = "User " + username + " attempted to purchase an item. The following response was received : \n "
                    + returnMessage + " ------------------- \n";
            writeLog(username, logMessage);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void makeAnExchange(String username, String hostName, Scanner sc, String userLocation){
        try {
            sc.nextLine();
            System.out.println("Please enter the item ID of the item you want to purchase");
            String desiredItemID = sc.nextLine();
            System.out.println("Please enter the item ID of the item you want to exchange");
            String desiredExchange = sc.nextLine();
            System.out.println("Please enter the date of return in the form [ddmmyyyy]");
            String dateOfReturn = sc.nextLine();
            String returnMessage = store.exchangeLogic(username,desiredItemID, desiredExchange, dateOfReturn);
            System.out.println(returnMessage);
            String logMessage = "User " + username + " attempted to exchange an item. The following response was received : \n "
                    + returnMessage + " ------------------- \n";
            writeLog(username, logMessage);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void findAnItem(String username, String hostName, Scanner sc, String userLocation){
        try {
            sc.nextLine();
            System.out.println("Please enter the item Name of the item you want to search our stores for");
            String desiredItemName = sc.nextLine();
            String returnMessage = store.findItem(username, desiredItemName);
            System.out.println("Here is the stock across our servers : \n" + returnMessage);
            System.out.println("----------");
            String logMessage = "User " + username + " accessed find item at " + userLocation + " The following message was shown : \n "
                    + returnMessage + " ------------------- \n";
            writeLog(username, logMessage);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public static void makeAReturn(String username, String hostName, Scanner sc, String userLocation){
        try {
            sc.nextLine();
            System.out.println("Please enter the item id you want to return");
            String desiredItemID = sc.nextLine();
            System.out.println("Please enter the date of return in the form [ddmmyyyy]");
            String dateOfReturn = sc.nextLine();
            String returnMessage = store.returnItem(username,desiredItemID,dateOfReturn);
            System.out.println(returnMessage);
            System.out.println("----------");
            String logMessage = "User " + username + " returned item from their home server " + userLocation + " The following message was shown : \n "
                    + returnMessage + " ------------------- \n";
            writeLog(username, logMessage);
        }catch (Exception e){
            System.out.println(e);
        }

    }




    public static void listItems(String username, String userLocation){
        try {
            String returnMessage = store.listItemAvailability(username);
            System.out.println(userLocation + " Location Stock");
            System.out.println(returnMessage);
            String logMessage = "User " + username + " accessed list item at " + userLocation + " \n The following message was shown : \n " + returnMessage
                    + " \n ------------------- \n";
            writeLog(username, logMessage);
        }catch (Exception e){}

    }




    public static String selectLocation(String hostName, Scanner sc) {
        int chosenLocation;
        boolean validInput = true;
        while (validInput) {
            try {
               String[] availableStores;
                System.out.println("Please enter the number of the store you'd like to shop at?");
                availableStores = listStores(hostName);
                chosenLocation = sc.nextInt();
                if(chosenLocation > 0 && chosenLocation < 4)
                return availableStores[chosenLocation-1];
                } catch (Exception e) {
                System.out.println("Invalid Input. Try again!");
                    }
        }
        return("Error!");
    }




    public static String[] listStores(String hostName) throws RemoteException, MalformedURLException {
        String[] availableStores = new String[Naming.list(hostName).length];
        for (int i = 0; i < Naming.list(hostName).length; i++) {
            availableStores[i] = ((String) Arrays.stream(Naming.list(hostName)).toArray()[i]).substring(17, 19);
            System.out.println(i+1 +". " + availableStores[i]);
        }
        return availableStores;
    }




    public static void writeLog(String username, String message) throws IOException {
        String filePath = "C:\\Users\\Waqar's PC\\IdeaProjects\\SOEN423-A1\\src\\Log\\" + username+".txt";
        try {
            File myObj = new File(filePath);
            if (!myObj.exists()) {
                myObj.createNewFile();
                PrintWriter pw = new PrintWriter(new FileWriter(myObj)); //New File
                pw.write(message);
                pw.close();
            } else {
                PrintWriter pw = new PrintWriter(new FileWriter(myObj, true)); //File Already Exists
                pw.append(message);
                pw.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }


    }




    public static String returnCurrentTime(){
        LocalDate currentTime = LocalDate.now();
        DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("ddMMyyyy");
        String formattedDate = currentTime.format(formattedTime);
        return formattedDate;
    }







    public static void listItemsPath(String hostName, Scanner sc, String username, String userLocation) {
        listItems(username, userLocation);
    }



}
