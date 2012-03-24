package Testes;

import java.io.IOException;
import java.net.UnknownHostException;

import Comunicação.Unicast;

public class testeUnicastCliente {
	public static void main(String args []){
		Unicast u = new Unicast();
		u.configuraSocket();
		u.setAddress("localhost");
		u.setStatus(true);
		
		u.start();
		
		
		String a = "oioi";
		u.enviaMsg(a.getBytes());
		
		while(true){
			String msg = u.getMsg();
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

