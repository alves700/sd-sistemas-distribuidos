package Comunicação;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public abstract class Conection  extends Thread{
	
	protected DatagramSocket socket;
    protected InetAddress address;
    protected ArrayList<DatagramPacket> inBuffer;
    protected boolean isConnected;
	protected final int tamByte = 1000;
	protected int port;
	
	public synchronized void recebeMsg() throws InterruptedException, IOException{
        byte[] buffer = new byte[1000];
                // get messages from others in group
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        Thread.sleep(10);
        getSocket().receive(messageIn);
        inBuffer.add(messageIn);
      
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
    		byte [] m = dp.getData();
    		int i = 0;
    		for(; i<dp.getLength();i++){
    			if(m[i] == 0){
    				break;
    			}
    		}
    		String msg = new String(dp.getData());
    		return msg.substring(0,i);
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
                try {
					recebeMsg();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}
	

	
}
