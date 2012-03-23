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
		m.enviaMsg("asdjas");
		while(true){
			
			System.out.println("MSG:"+ m.getMsg());
			
			
		}
	}

}
