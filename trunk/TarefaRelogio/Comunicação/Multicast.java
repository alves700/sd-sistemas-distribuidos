package Comunicação;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Multicast extends Thread{
    
    private MulticastSocket socket;
    public InetAddress group;
    public boolean isConnected;
    private final String MCAdrress = "224.0.0.1";
    public ArrayList<String> inBuffer;
    
    public Multicast(){
    	inBuffer = new ArrayList<String>();
    }
    public void joinMulticast(){
        try {
            group = InetAddress.getByName("224.0.0.1");
            socket = new MulticastSocket(6780);
            socket.joinGroup(group);
            this.isConnected = true;
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }//finally {if(getSocket() != null) getSocket().close();}
    }
    public void leaveGroup() {
        try {
            this.isConnected = false;
            getSocket().leaveGroup(group);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void enviaMsg(String msg){
        byte [] m = msg.getBytes();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6780);
        try {
			getSocket().send(messageOut);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  
    }
    public synchronized void recebeMsg(){
            
        byte[] buffer = new byte[1000];
                // get messages from others in group
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        try {
        	
        	Thread.sleep(50);
            getSocket().receive(messageIn);
            inBuffer.add(new String(messageIn.getData()));
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           
  
    }
    public String getMsg(){
    	if ( existeMsg() ){
	    	String msg = inBuffer.get(0);
	    	inBuffer.remove(0);
	    	return msg;
    	}
    	else
    		return null;
    }
    public boolean existeMsg(){
    	if ( inBuffer.size() == 0)
    		return false;
    	else 
    		return true;
    }
    @Override
    public void run(){
        while(true){
            if ( isConnected){
                recebeMsg();
            }
        }
    }
	public MulticastSocket getSocket() {
		return socket;
	}
	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}
}

