package Server;

import ServerImpl.ONCommandsImpl;
import ServerImpl.QCCommandsImpl;

import java.rmi.Naming;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;

public class ONServer {
    public static void main(String args[]){
        try{
            startTheRegistry();
            ONCommandsImpl Obj = new ONCommandsImpl();
            String registryURL = "rmi://localhost:1099/ON";
            Naming.rebind(registryURL, Obj);
            System.out.println("Start Sequence Complete");


            Runnable task = () -> {
                receive(Obj);
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


    private static void receive(ONCommandsImpl obj) {
        DatagramSocket socket = null;
        String returnMessage = "";
        try {
            socket = new DatagramSocket(2001);
            byte[] buffer = new byte[1000];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String sentence = new String( request.getData(), request.getOffset(), request.getLength()).trim();
                String[] split = sentence.split("-");
                System.out.println("Function Received " + split[0]);
                if(split[0].equals("purchaseItem")) {
                    returnMessage = obj.purchaseLocalItem(split[1],split[2]);
                    System.out.println(returnMessage);
                }
                if(split[0].equals("findItem")) {
                    returnMessage = obj.findLocalItem(split[2]);
                    System.out.println(returnMessage);

                }if(split[0].equals("returnItem")) {
                    returnMessage = obj.returnLocalStock(split[2]);
                    System.out.println(returnMessage);
                }if(split[0].equals("getBudget")) {
                    returnMessage = Integer.toString(obj.getLocalBudget(split[1]));
                    System.out.println(returnMessage);
                }if(split[0].equals("setBudget")) {
                    returnMessage = obj.setLocalBudget(split[1],split[3]);
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
