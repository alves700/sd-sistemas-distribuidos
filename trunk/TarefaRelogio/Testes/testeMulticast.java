package Testes;

import java.io.IOException;
import java.net.DatagramPacket;

import Comunicação.Multicast;

public class testeMulticast {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Multicast m = new Multicast();
		try {
			m.joinMulticast();
			m.start();
			m.enviaMsg(new String("BuabU"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		while(true){	
			String msg;
			DatagramPacket dp = m.getDatagram();
			
			msg = m.getMsg(dp);
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
