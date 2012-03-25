package Comunicação;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Multicast extends Conection{
    
    
    private final String MCAdrress = "239.0.0.1";
    
    private MulticastSocket socket;
    
    public Multicast(){
    	this.port = 5900;
    	inBuffer = new ArrayList<DatagramPacket>();
    }
    public void joinMulticast() throws IOException{
        address = InetAddress.getByName(MCAdrress);
        socket = new MulticastSocket(port);
        socket.joinGroup(address);
        this.isConnected = true;
        
    }
    public void leaveGroup() throws IOException {
    	this.isConnected = false;
        socket.leaveGroup(address);
        if(getSocket() != null) getSocket().close();
    }
    public void enviaMsg(String msg) throws IOException{
        byte [] m = msg.getBytes();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, address, port);
        getSocket().send(messageOut);
      
    }
    
}

