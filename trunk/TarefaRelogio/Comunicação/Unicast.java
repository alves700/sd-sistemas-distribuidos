package Comunicação;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;


//Classe Unicast UDP
public class Unicast extends Thread {
	
	public final static int serverPort = 7896;
	
	public final static int basePort = 5000;
	
	public ArrayList<DatagramPacket> inBuffer;
	private DatagramSocket aSocket;
	InetAddress address = null;
	private boolean status = false;
	
	private final int tamByte = 1000;
	
	private int port;
	
	public Unicast(int id){
		port = basePort + id;
		inBuffer = new ArrayList<DatagramPacket>();
	}
	// Dado um endereço configurado e uma porta envia a mensagem para esse endereço e porta.
	public void enviaMsg(byte [] message, String ip, int port){
		
		setAddress(ip);
		
		DatagramPacket pacote = new DatagramPacket(message, message.length, address, port);
		try {
			aSocket.send(pacote);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run(){
		while(true){
			if(status){
				recebeMsg();
			}
		}
	}
	// Método de leitura. Dados que chegam são armazenados na variável bufferString.
	// O método receive é bloqueador, portanto não é necessário o uso de semáforos.
	public void recebeMsg(){
		byte [] buffer = new byte [tamByte];
		DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
		//InetAddress IPAddress = reply.getAddress();
		
		try {
			aSocket.receive(reply);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		inBuffer.add(reply);
	}
	
	// Retorna a mensagem armazenada no bufferString, logo após limpa esse buffer.
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
	
	public void fechaSocket() {
		aSocket.close();
	}
	
	public void setAddress(String ip) {
		try {
			address = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Instancia um novo aSocket. (utilizado pelo cliente)
	public void configuraSocket () {
		try {
			aSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Instancia um novo aSocket. (utilizado pelo server)
	public void configuraSocket (int serverPort) {
		try {
			aSocket = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Configura os status da conexão.
	public void setStatus(boolean status){
		this.status = status;
	}
}
