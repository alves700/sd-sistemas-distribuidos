package TarefaRelogio;

import java.net.*;
import java.security.Key;
import java.util.ArrayList;
import java.io.*;

/** 
Classe responsável pela comunicação Multicast.
*/  
public class Multicast extends Conection{
    
    /** Endereço do grupo.*/
    private final String MCAddress = "239.0.0.1";
    
    /** Socket da comunicação.*/
    private MulticastSocket socket;
    /** 
    Inicia a porta onde a conexão Multicast ocorrera.
    */ 
    public Multicast(){
    	this.port = 4000;
    	inBuffer = new ArrayList<DatagramPacket>();
    }
    /** 
    Inicia o socket da conexão e adere ao grupo especificado pelo IP MCAddress. Conexão é liberada.
    */ 
    public void joinMulticast() throws IOException{
        address = InetAddress.getByName(MCAddress);
        setSocket(new MulticastSocket(port));
        getSocket().joinGroup(address);
        this.isConnected = true;
        
    }
    /** 
    Deixa o grupo especificado pelo IP MCAddress, fecha o socket.
    */
    public void leaveGroup() throws IOException {
    	this.isConnected = false;
        getSocket().leaveGroup(address);
        if(getSocket() != null) getSocket().close();
    }
    /** 
    Envia mensagem ao grupo.
    @param msg Mensagem enviada para o grupo.
    */
    public void enviaMsg(String msg) throws IOException{
        byte [] m = msg.getBytes("ISO-8859-1");
        DatagramPacket messageOut = new DatagramPacket(m, m.length, address, port);
        getSocket().send(messageOut);
      
    }
    /** 
    @return socket responsável pela comunicação Multicast.
    */
	public MulticastSocket getSocket() {
		return socket;
	}
	/** 
    @param socket novo MulticastSocket da comunicação.
    */
	public void setSocket(MulticastSocket socket) {
		this.socket = socket;
	}
}

