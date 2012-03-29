package Testes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;

import Comunicação.Unicast;

public class testeUnicastCliente {
	public static void main(String args []){
		int id =60;
		int idDocara = 80;
		Unicast u;
		try {
			u = new Unicast(id);
			
			u.start();
			String a = "oioi";
			//u.enviaMsg(a, "localhost", 5000 + id);
			
			while(true){
				DatagramPacket dp = u.getDatagram();
				if ( dp != null){
						
					String msg = u.getMsg(dp);
					if ( msg != null ){
						System.out.println("MSG:"+ msg);
					}
					//u.enviaMsg(("burro"+ id), "localhost", 5000+ idDocara);
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
}

