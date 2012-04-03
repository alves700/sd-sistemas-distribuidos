package Comunicação;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;

/** 
Classe que possui métodos e atributos que tanto a classe MultiCast e Unicast possuem em comum.
*/  
public abstract class Conection  extends Thread{
	
	/** Socket da conexão.*/
	protected DatagramSocket socket;
	/** Objeto que contém o endereço da conexão.*/
    protected InetAddress address;
    /** Buffer de entrada da comunicação.*/
    protected ArrayList<DatagramPacket> inBuffer;
    /** Identifica o status da conexão.*/
    protected boolean isConnected;
    /** Porta em que a conexão está sendo feita.*/
	protected int port;
	
	
    /**   
	Verifica se existe mensagem no buffer de entrada.
	@return boolean indicando se há ou não mensagens.
	*/ 
    public boolean existeMsg(){
    	if ( inBuffer.size() == 0)
    		return false;
    	else 
    		return true;
    }
	
	/** 
	@return Primeiro DatagramPacket da lista inBuffer.
	*/ 
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
	/**   
	@param dp - DatagramPacket.
	@return Endereço IP do DatagramPacket recebido.
	*/ 
    public String getIP(DatagramPacket dp){
    	if (dp != null)
    		return dp.getAddress().getHostAddress();
    	else
    		return null;
    }
    /**   
   	@param dp - DatagramPacket.
   	@return mensagem do DatagramPacket recebido.
     * @throws UnsupportedEncodingException 
   	*/ 
   public String getMsg(DatagramPacket dp) throws UnsupportedEncodingException{
	   	if ( dp != null ){
	   		byte [] m = dp.getData();
	   		String msg = new String(m, "ISO-8859-1");
	   		return msg.substring(0,dp.getLength());
	   	}
	   	else
	   		return null;
    }
    /**   
	@return DatagramSocket da conexão.
	*/ 
	public DatagramSocket getSocket() {
		return socket;
	}
    
   
	/** 
	Responsável pela leitura do buffer de entrada.
	 * @throws ClassNotFoundException 
	*/ 
	//Acho que esse synchronized pode ser removido.
	public synchronized void recebeMsg() throws IOException{
        byte[] buffer = new byte[1000];
                // get messages from others in group
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        getSocket().receive(messageIn);
        
        inBuffer.add(messageIn);
      
    }
    /**   
  	Método principal da Thread, verifica se a conexão foi estabelecida, chama o método recebeMsg().
  	*/ 
    @Override
    public void run(){
        while(true){
            if ( isConnected){
                try {
					recebeMsg();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
	 /**   
	@param socket - novo DatagramSocket da conexão.
	*/ 
	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}
	
	
}
