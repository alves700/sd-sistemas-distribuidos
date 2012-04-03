package Comunica��o;

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
Classe que possui m�todos e atributos que tanto a classe MultiCast e Unicast possuem em comum.
*/  
public abstract class Conection  extends Thread{
	
	/** Socket da conex�o.*/
	protected DatagramSocket socket;
	/** Objeto que cont�m o endere�o da conex�o.*/
    protected InetAddress address;
    /** Buffer de entrada da comunica��o.*/
    protected ArrayList<DatagramPacket> inBuffer;
    /** Identifica o status da conex�o.*/
    protected boolean isConnected;
    /** Porta em que a conex�o est� sendo feita.*/
	protected int port;
	
	
    /**   
	Verifica se existe mensagem no buffer de entrada.
	@return boolean indicando se h� ou n�o mensagens.
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
	@return Endere�o IP do DatagramPacket recebido.
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
	@return DatagramSocket da conex�o.
	*/ 
	public DatagramSocket getSocket() {
		return socket;
	}
    
   
	/** 
	Respons�vel pela leitura do buffer de entrada.
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
  	M�todo principal da Thread, verifica se a conex�o foi estabelecida, chama o m�todo recebeMsg().
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
	@param socket - novo DatagramSocket da conex�o.
	*/ 
	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}
	
	
}
