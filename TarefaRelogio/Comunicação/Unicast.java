package Comunicação;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


/** 
Classe responsável pela comunicação Unicast UDP.
*/
public class Unicast extends Conection{
	
	public final static int basePort = 5001;
	
	/** 
	Inicia o socket DatagramSocket da comunicação com uma porta. Conexão é liberada.
	@param id - ID do processo original.
    */
	public Unicast(int id) throws SocketException{
		port = basePort + id;
		setSocket(new DatagramSocket(port));
		isConnected = true;
		inBuffer = new ArrayList<DatagramPacket>();
	}
	// Dado um endereço configurado e uma porta envia a mensagem para esse endereço e porta.
	/** 
	Dado um endereço IP e uma porta envia uma mensagem para esse endereço e porta.
	@param ip - IP de destino.
	@param id - ID do processo de destino.
	@param msg - Mensagem enviada para o processo.
	*/  
	public void enviaMsg(String ip, int id, String msg) throws IOException{
		int port = basePort +id;
		address = InetAddress.getByName(ip);
		byte [] m = msg.getBytes();
		DatagramPacket pacote = new DatagramPacket(m, m.length, address, port);
		getSocket().send(pacote);
	}
	/** 
	Fecha a comunicação Unicast.
	*/  
	public void fechaSocket() {
		isConnected = false;
		getSocket().close();
	}
	
}
