package Testes;

import java.io.IOException;

import Comunicação.Unicast;

public class testeUnicastServer {
	public static void main(String args []){
		Unicast u = new Unicast(23);
		
		u.configuraSocket(Unicast.serverPort);
		u.start();
		u.setStatus(true);
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
	}	
}

