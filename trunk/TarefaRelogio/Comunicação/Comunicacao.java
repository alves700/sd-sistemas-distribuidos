package Comunicação;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Comunicacao {
	private Unicast uc;
	private Multicast mc;
	public Comunicacao(){
		uc = new Unicast();
		uc.setStatus(false);
		uc.run();
		
		
	}
	
	public void enviaMsgUni(){
		
	}
	public String recebeMsgUni(){
		//Contém o código de protocolo, para saber se é uma mensagem de requisição de relógio ou
		//de eleição ou "Hello" do Mestre. Caso a mensagem estiver encriptografada ela sera desencriptografada.
		return null;
	}
	public void enviaMsgMulti(){
	}
	public String recebeMsgMulti(){
		return null;
	}
	
	public Unicast getUnicast(){
		return uc;
	}
	public Multicast getMulticast(){
		return mc;
	}


	
}
