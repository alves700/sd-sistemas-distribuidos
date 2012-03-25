package Comunicação;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TreeSet;

public class Comunicacao {
	
	
	public final static int INDEX_MSG = 1;
	public final static int INDEX_IP = 0;
	public final static int INDEX_TIPO = 0;
	
	public final static int HELLO = 0;
	public final static int	REQ_RELOGIO = 1;
	public final static int	RELOGIO = 2;
	public final static int	RECONHECIMENTO = 3;
	public final static int	ELEICAO = 4;
	public final static int	CALC_RTT_MAX = 5;

	
	private ArrayList<String[]> contatos;
	
	
	private String meuIP;
	// Duracao de reconhecimento de PC's = 10s
	private final long tempoReconhecimento = 10000;
	
	private Unicast uc;
	private Multicast mc;
	
	
	public Comunicacao(int id) throws IOException{
		
		contatos = new ArrayList<String[]>();
		
		//Armazena seu próprio IP da rede.
		
		InetAddress address = InetAddress.getLocalHost();
		meuIP = address.getHostAddress();
		 
		mc = new Multicast();
		mc.joinMulticast();
		Thread t1 = new Thread(mc);
		t1.start();
		// O uc pode ser configurado somente quando for necessário enviar ou receber mensagens.
		uc = new Unicast(id);
	}
	// Método iniciado pelo Processo para reconhecer os processos vizinhos, não sei como fazer esse método para funcionar
	// tudo no mesmo PC, estou fazendo com q ele funcione somente em PC's diferentes. Testes realizados em 2 PC's diferentes.
	public void reconheceOutrosProcessos(int ID) throws InterruptedException, IOException{
		long t1 = System.currentTimeMillis();
		long t2 = System.currentTimeMillis();
		
		mc.enviaMsg(protMsg(RECONHECIMENTO,ID));
		while(t1 + tempoReconhecimento >= t2){
			if ( mc.existeMsg()){
				
				DatagramPacket dp = mc.getDatagram();// pega datagrama do buffer de entrada do socket
				String msg = mc.getMsg(dp);
				
				String[] contato = msg.split(" ");
				
				if ( !contato[0].equals(""+ RECONHECIMENTO)) //se naum for msg de reconhecimento
					continue;//vai para a proxima iteração
				
				contato[INDEX_IP] = mc.getIP(dp); // coloca o IP no primeiro index do array
				
				
				boolean processoExistente = false;
                // Verifica se o IP ja existe na sua lista de contatos.
				int i;
                for(i = 0 ; i<contatos.size(); i++){
                        if(contato[INDEX_MSG].equals(contatos.get(i)[INDEX_MSG])){
                                processoExistente = true;
                        }
                }
                //Se processo não existir na tabela de processos, adiciona-o na lista e envia sua ID para esse processo adicioná-lo, dá mais
                //tempoDeReconhecimento para o término do reconhecimento entre processos
               if ( !processoExistente ){
					contatos.add(contato);
					System.out.println(contato[INDEX_IP] + " "+ contato[INDEX_MSG]);
					mc.enviaMsg(protMsg(RECONHECIMENTO,ID));
					t1 = System.currentTimeMillis();
				}
				
			}
			
			Thread.sleep(10);
			
			t2 = System.currentTimeMillis();
		}
	}
	public String protMsg(int tipo, String msg){
		return ""+tipo+" "+msg;
	}
	public String protMsg(int tipo, int ID){
		return ""+tipo+" "+ID;
	}
	
	public Unicast getUnicast(){
		return uc;
	}
	public Multicast getMulticast(){
		return mc;
	}
	public ArrayList<String[]> getContatos() {
		return contatos;
	}

}