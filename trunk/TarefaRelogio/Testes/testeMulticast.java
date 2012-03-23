package Testes;

import Comunicação.Multicast;

public class testeMulticast {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Multicast m = new Multicast();
		m.joinMulticast();
		m.start();
		m.enviaMsg(new String("BuabU"));
		while(true){	
			String msg;
			msg = m.getMsg();
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
