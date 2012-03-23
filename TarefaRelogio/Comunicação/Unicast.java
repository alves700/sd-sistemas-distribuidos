package Comunicação;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


//Classe Unicast UDP
public class Unicast implements Runnable {
	
	private final int serverPort = 7896;
	
	private DatagramSocket aSocket;
	InetAddress address = null;;
	 
	byte[] buffer = new byte[1000];
	private boolean status = false;
	
	public void envia(byte [] message) throws IOException{
		DatagramPacket pacote = new DatagramPacket(message, message.length, address, serverPort);
		aSocket.send(pacote);
		
	}
	@Override
	public void run(){
		while(true){
			// coloco essa thread para dormir, será acordada por comunicação.
			try {
				this.wait(); 
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(status){
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				try {
					aSocket.receive(reply);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer = reply.getData();
			}
		}
	}
	public byte[] getMessage(){
		return buffer;
	}
	public void fechaSocket() throws IOException{
		aSocket.close();
	}
	public void setAddress(String ip) throws UnknownHostException{
		address = InetAddress.getByName(ip);
	}
	public void configuraSocket () throws IOException{
		aSocket = new DatagramSocket();
	}
	public void setStatus(boolean status){
		this.status = status;
	}
}
