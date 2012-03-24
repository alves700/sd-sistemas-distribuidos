package Comunica��o;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Multicast extends Thread{
    
    private MulticastSocket socket;
    public InetAddress group;
    
    public boolean isConnected;
    private final String MCAdrress = "239.0.0.1";
    public ArrayList<DatagramPacket> inBuffer;
	private final int tamByte = 1000;
	private int port;
    
    public Multicast(){
    	this.port = 6000;
    	inBuffer = new ArrayList<DatagramPacket>();
    }
    public void joinMulticast(){
        try {
            group = InetAddress.getByName(MCAdrress);
            socket = new MulticastSocket(port);
            socket.joinGroup(group);
            this.isConnected = true;
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }
    }
    public void leaveGroup() {
        try {
            this.isConnected = false;
            getSocket().leaveGroup(group);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {if(getSocket() != null) getSocket().close();}
    }
    public void enviaMsg(String msg){
        byte [] m = msg.getBytes();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, port);
        try {
			getSocket().send(messageOut);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  
    }
    // As mensagens recebidas s�o armazenadas em uma string IP+Mensagem, n�o sei se a informa��o do IP seria necess�ria em todos
    // os casos (quando o mestre requisita rel�gios por exemplo), mas poder�amos deixar assim, como se fosse um protocolo.
    public synchronized void recebeMsg(){
            
        byte[] buffer = new byte[1000];
                // get messages from others in group
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        try {
        	
        	Thread.sleep(10);
            getSocket().receive(messageIn);
     
            inBuffer.add(messageIn);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           
  
    }
    public DatagramPacket getDatagram(){
    	if ( existeMsg() ){
    		DatagramPacket dp = inBuffer.get(0);
	    	inBuffer.remove(0);
	    	return dp;
    	}
    	else{
    		return null;
    	}
    }
    public String getIP(DatagramPacket dp){
    	if (dp != null)
    		return dp.getAddress().getHostAddress();
    	else
    		return null;
    }
    public String getMsg(DatagramPacket dp){
    	if ( dp != null ){
    		return new String(dp.getData());
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

