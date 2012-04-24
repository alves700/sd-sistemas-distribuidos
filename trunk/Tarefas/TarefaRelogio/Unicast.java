package TarefaRelogio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


/** 
Classe respons�vel pela comunica��o Unicast UDP.
*/
public class Unicast extends Conection{
	
	/** Porta base, serve como base para a porta onde a conex�o est� sendo feita (Porta = PortaBase + ID do processo).*/
	public final static int basePort = 5001;
	
	/** 
	Inicia o socket DatagramSocket da comunica��o com uma porta. Conex�o � liberada.
	@param id ID do processo original.
    */
	public Unicast(int id) throws SocketException{
		port = basePort + id;
		setSocket(new DatagramSocket(port));
		isConnected = true;
		inBuffer = new ArrayList<DatagramPacket>();
	}
	/** 
	Dado um endere�o IP e uma porta envia uma mensagem para esse endere�o e porta.
	@param ip IP de destino.
	@param id ID do processo de destino.
	@param msg Mensagem enviada para o processo.
	*/  
	public void enviaMsg(String ip, int id, String msg) throws IOException{
		int port = basePort +id;
		address = InetAddress.getByName(ip);
		byte [] m = msg.getBytes("ISO-8859-1");
		DatagramPacket pacote = new DatagramPacket(m, m.length, address, port);
		getSocket().send(pacote);
	}
	/** 
	Fecha a comunica��o Unicast.
	*/  
	public void fechaSocket() {
		isConnected = false;
		getSocket().close();
	}
	
}
