package Comunicação;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Comunicacao {
	
	private ArrayList<byte[]> contatosIP = new ArrayList();
	private ArrayList<String> contatosID = new ArrayList();
	
	private final long tempoReconhecimento = 10000;
	
	private Unicast uc;
	private Multicast mc;
	public Comunicacao(){
	}
	// Método iniciado pelo Processo para reconhecer os processos vizinhos.
	public void reconheceOutrosProcessos(int ID){
		long t1 = System.currentTimeMillis();
		mc.enviaMsg(""+ID);
		while(t1 + tempoReconhecimento >= System.currentTimeMillis()){
			
			String msg = mc.getMsg();
			if ( msg != null ){
				contatosID.add(msg);
				//contatosIP.add(mc.getEnderecoMsg());
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*public void enviaMsgUni(){
		
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
	}*/
	
	public Unicast getUnicast(){
		return uc;
	}
	public Multicast getMulticast(){
		return mc;
	}


	
}
