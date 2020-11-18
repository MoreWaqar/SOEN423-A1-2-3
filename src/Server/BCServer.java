package Server;

import ServerImpl.BCCommandsImpl;

import javax.xml.ws.Endpoint;
import java.rmi.Naming;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;
import java.sql.SQLOutput;

public class BCServer {
    public static void main(String args[]){
        try{
//            ORB orb = ORB.init(args, null);
//
//            POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
//            rootPOA.the_POAManager().activate();
//
//            BCCommandsImpl store = new BCCommandsImpl();
//            Object ref = rootPOA.servant_to_reference(store);
//            Store corbaRef = StoreHelper.narrow(ref);
//
//            Object objRef = orb.resolve_initial_references("NameService");
//            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//
//            NameComponent[] path = ncRef.to_name("BC");
//            ncRef.rebind(path, corbaRef);
//
//

//
//            orb.run();
              BCCommandsImpl store = new BCCommandsImpl();
//              Endpoint endpoint = Endpoint.publish("http://localhost:8100/BCStore", store);
              Runnable task = () -> {
                  receive(store);
              };
              Thread thread = new Thread(task);
              thread.start();


        }
        catch (Exception e) {
        }


    }

    public static void startTheRegistry() throws RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.list( );
        }catch(Exception e){
            Registry registry =  LocateRegistry.createRegistry(1099);
        }
    }


    private static void receive(BCCommandsImpl obj) {
        DatagramSocket socket = null;
        String returnMessage = "";
        try {
            socket = new DatagramSocket(2002);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String sentence = new String( request.getData(), request.getOffset(), request.getLength()).trim();
                String[] split = sentence.split("-");
                //action+"-"+username+"-"+itemId+"-"+cost"-"+oldItem
                System.out.println("Function Received " + split[0]);
                if(split[0].equals("purchaseItem")) {
                    returnMessage = obj.purchaseLocalItem(split[1],split[2]);
                    System.out.println(returnMessage);
                }
                if(split[0].equals("findItem")) {
                    returnMessage = obj.findLocalItem(split[2]);
                    System.out.println(split[2]);
                    System.out.println(returnMessage);

                }if(split[0].equals("returnItem")) {
                    returnMessage = obj.returnLocalStock(split[1],split[2]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getBudget")) {
                    returnMessage = Integer.toString(obj.getLocalBudget(split[1]));
                    System.out.println(returnMessage);
                }if(split[0].equals("setBudget")) {
                    returnMessage = obj.setLocalBudget(split[1],split[3]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getOldPrice")) {
                    returnMessage = obj.getLocalOldItemPrice(split[2],split[1]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getNewPrice")) {
                    returnMessage = obj.getLocalNewItemPrice(split[2],split[1]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getFirstShop")) {
                    boolean value = obj.firstShop(split[1]);
                    if(value){
                        returnMessage = "true";
                    }else{
                        returnMessage = "false";
                    }
                    System.out.println(returnMessage);
                }if(split[0].equals("ownsItem")) {
                    returnMessage = obj.localOwnsItem(split[1],split[2]);
                    System.out.println(returnMessage);
                }

                byte[] sendData = returnMessage.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, returnMessage.length(), request.getAddress(),
                        request.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
    }




}
