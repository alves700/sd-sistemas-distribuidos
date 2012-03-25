package Comunicação;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


//Classe Unicast UDP
public class Unicast extends Conection{
	
	public final static int basePort = 5001;
	
	public Unicast(int id) throws SocketException{
		port = basePort + id;
		setSocket(new DatagramSocket(port));
		isConnected = true;
		inBuffer = new ArrayList<DatagramPacket>();
	}
	// Dado um endereço configurado e uma porta envia a mensagem para esse endereço e porta.
	public void enviaMsg(String msg, String ip, int port) throws IOException{
		
		address = InetAddress.getByName(ip);
		byte [] m = msg.getBytes();
		DatagramPacket pacote = new DatagramPacket(m, m.length, address, port);
		getSocket().send(pacote);
	}
	public void fechaSocket() {
		isConnected = false;
		getSocket().close();
	}
	
}
