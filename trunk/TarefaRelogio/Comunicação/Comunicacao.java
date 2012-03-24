package Comunicação;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Comunicacao {
	
	private ArrayList<String> contatosIP = new ArrayList();
	private ArrayList<String> contatosID = new ArrayList();
	private String meuIP;
	// Duracao de reconhecimento de PC's = 10s
	private final long tempoReconhecimento = 10000;
	
	private Unicast uc;
	private Multicast mc;
	public Comunicacao(){
		try {
			InetAddress address = InetAddress.getLocalHost();
			meuIP = address.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		mc = new Multicast();
		mc.joinMulticast();
		mc.start();
		
		// O uc pode ser configurado somente quando for necessário enviar ou receber mensagens.
		uc = new Unicast();
	}
	// Método iniciado pelo Processo para reconhecer os processos vizinhos, não sei como fazer esse método para funcionar
	// tudo no mesmo PC, estou fazendo com q ele funcione somente em PC's diferentes. Testes realizados em 2 PC's diferentes.
	public void reconheceOutrosProcessos(int ID){
		long t1 = System.currentTimeMillis();
		mc.enviaMsg(""+ID);
		while(t1 + tempoReconhecimento >= System.currentTimeMillis()){
			if ( mc.existeMsg()){
				String msg = mc.getMsg();
				int i = 0;
				for(;i <msg.length();i++){
					if(msg.charAt(i) == ' '){
						break;
					}
				}
				String msg_ip = msg.substring(0, i);
				String msg_id = msg.substring(i+1);
				
				boolean processoExistente = false;
				// Verifica se o IP ja existe na sua lista de contatos.
				for(i = 0 ; i<contatosIP.size(); i++){
					if(msg_ip.equals(contatosIP.get(i))){
						processoExistente = true;
					}
				}
				//Se processo não existir na tabela de processos, adiciona-o na lista e envia sua ID para esse processo adicioná-lo.
				if(!processoExistente){
					contatosIP.add(msg_ip);
					contatosID.add(msg_id);
					System.out.println(msg_ip +" "+ msg_id);
					mc.enviaMsg(""+ID);
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Unicast getUnicast(){
		return uc;
	}
	public Multicast getMulticast(){
		return mc;
	}


	
}
