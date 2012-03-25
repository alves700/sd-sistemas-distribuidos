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
        setSocket(new MulticastSocket(port));
        getSocket().joinGroup(address);
        this.isConnected = true;
        
    }
    public void leaveGroup() throws IOException {
    	this.isConnected = false;
        getSocket().leaveGroup(address);
        if(getSocket() != null) getSocket().close();
    }
    public void enviaMsg(String msg) throws IOException{
        byte [] m = msg.getBytes();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, address, port);
        getSocket().send(messageOut);
      
    }
    public synchronized void recebeMsg() throws InterruptedException, IOException{
        byte[] buffer = new byte[1000];
                // get messages from others in group
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        Thread.sleep(10);
        getSocket().receive(messageIn);
        inBuffer.add(messageIn);
      
    }
	public MulticastSocket getSocket() {
		return socket;
	}
	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}
}

