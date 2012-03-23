package Comunicação;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Unicast implements Runnable {
	
	private Socket s;
	private final int serverPort = 7896; 
	private DataInputStream in;
	private DataOutputStream out;
	
	private String mensagem;
	private boolean status = false;
	
	public void envia(byte [] message) throws IOException{
		out = new DataOutputStream(s.getOutputStream());
		out.write(message);
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
				try {
					in = new DataInputStream(s.getInputStream());
					mensagem = in.readUTF();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public String getMessage(){
		return mensagem;
	}
	public void fechaSocket() throws IOException{
		s.close();
	}
	public void configuraSocket (InetAddress address) throws IOException{
		s = new Socket (address,serverPort);
	}
	public void setStatus(boolean status){
		this.status = status;
	}
}
