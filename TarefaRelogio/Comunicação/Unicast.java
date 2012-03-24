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


//Classe Unicast UDP
public class Unicast extends Thread {
	
	public final static int serverPort = 7896;
	
	private DatagramSocket aSocket;
	InetAddress address = null;
	 
	String bufferString = null;
	private boolean status = false;
	
	private final int tamByte = 1000;
	
	// Dado um endereço configurado e uma porta envia a mensagem para esse endereço e porta.
	public void enviaMsg(byte [] message){
		DatagramPacket pacote = new DatagramPacket(message, message.length, address, serverPort);
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
		int i =0;
		for(;i<tamByte;i++ ){
			if(buffer[i]==0){
				break;
			}
		}
		bufferString = new String(reply.getData());
		bufferString = bufferString.substring(0,i);
	}
	
	// Retorna a mensagem armazenada no bufferString, logo após limpa esse buffer.
	public String getMsg(){
		
		String bufferString2 = bufferString;
		bufferString = null;
		return bufferString2;
		
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
			aSocket = new DatagramSocket();
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
