package Testes;

import java.io.IOException;
import java.net.SocketException;

import Comunicação.Unicast;

public class testeUnicastServer {
	public static void main(String args []){
		Unicast u;
		try {
			u = new Unicast(23);
			
			u.start();
			while(true){
				String msg = u.getMsg(u.getDatagram());
				if ( msg != null ){
					System.out.println("MSG:"+ msg);
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}	
}

