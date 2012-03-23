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
		m.enviaMsg("sdasd");
		while(true){
			
			String msg;
			msg = m.getMsg();
			if ( msg != null ){
				System.out.println("MSG:"+ m.getMsg());
				
			}
			
			
		}
	}

}
