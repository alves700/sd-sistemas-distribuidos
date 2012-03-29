package Processo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

import Comunicação.Comunicacao;

public class Mestre extends Processo{
	
	//Variáveis do mestre.
	private final long tempoEnvioHello = 1000;
	private long ultimoHelloEnviado;
	
	//Variáveis de Calculo do RTT maximo
	private int RTTMax = 0;
	
	private final long tempoEsperaRTT = 1000;//Espera por 1s o RTT de outros escravos
	private final long tempoReqRTT = 3000;//Recalcula o RTT de 3 em 3 segundos.
	
	private boolean requerindoRTT = false;//True quando mestre requisita RTT, modificada para false quando RTT é calculado. 
	
	private long ultimoReqRTTEnviado; //"Horário" em que ocorreu a ultima requisição de RTT, também é utilizada para o cálculo do RTT de um processo. 
	
	ArrayList <Integer> RTT = new ArrayList <Integer>(); // ArrayList que armazena os RTTs dos processos.
	
	public Mestre() throws IOException {
		System.out.println("Sou o Mestre.");
	}
	
	public void iniciaVariaveis(){
		ultimoHelloEnviado = System.currentTimeMillis();
		ultimoReqRTTEnviado = System.currentTimeMillis();
	}

	@Override
	public void run() {
		iniciaVariaveis();
		while(true){
			try {
				
				envioDeMensagens();
				verficaBufferEntrada();
				update();
			
				Thread.sleep(1);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	public void envioDeMensagens() throws IOException{
		//Envio de Hello periodicamente.
		if(System.currentTimeMillis() > ultimoHelloEnviado + tempoEnvioHello){
			try {
				mc.enviaMsg(comm.protMsg(Comunicacao.HELLO,ID));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ultimoHelloEnviado = System.currentTimeMillis();
		}
		//Envio de requisição troca de mensagens para cálculo de RTT.
		if(System.currentTimeMillis() > ultimoReqRTTEnviado + tempoReqRTT){
			
			mc.enviaMsg(comm.protMsg(Comunicacao.CALC_RTT_MAX,ID));
			ultimoReqRTTEnviado = System.currentTimeMillis();
			requerindoRTT = true;
		}
		
	}
	public void update() throws IOException{
		
		// Cálcula o RTT máximo após o tempoEsperaRTT ter passado.
		if(requerindoRTT && System.currentTimeMillis() > ultimoReqRTTEnviado + tempoEsperaRTT){
			
			requerindoRTT = false;
			RTTMax = calculaRTTmax();
			System.out.println("RTTMaximo Calculado: "+RTTMax+" Num de processos que participaram: "+RTT.size());
			
			RTT.clear();
			
		}
	}
	
	public void processaMensagem(DatagramPacket dp){
		String[] msg = mc.getMsg(dp).split(" ");
		 
		switch( Integer.parseInt(msg[Comunicacao.INDEX_TIPO]) ){

			case Comunicacao.REQ_RELOGIO:
				break;
			case Comunicacao.RELOGIO:
				break;
			case Comunicacao.RECONHECIMENTO:
				break;
			case Comunicacao.ELEICAO:
				// Caso recebeu mensagem de eleição é pq o tempo limite para hello estourou em pelo menos 
				// um processo, então mestre se mata.
				//System.exit(0);
				break;
			case Comunicacao.CALC_RTT_MAX:
				//Caso recebeu msg de Calc de RTT Max que não seja de si mesmo, e está requerindo RTT's adiciona o tempo de RTT na lista. 
				if(requerindoRTT && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
					int rttt = (int) (System.currentTimeMillis()-ultimoReqRTTEnviado);
					System.out.println("Msg de ID: " +  msg[Comunicacao.INDEX_ID]+" Seu RTT: "+rttt);
					RTT.add(rttt);
				}
				break;
			
		}
	}
	public int calculaRTTmax() throws IOException{
		
		double mediaRTT = 0;
		double RTTMax = 0;
		double desvioPadraoRTT = 0;
		int contRespostas = RTT.size();
		
		if(contRespostas == 0)
			return 0;
		
		for ( int i =0; i< contRespostas; i++){
			mediaRTT += (double)RTT.get(i)/contRespostas; 
		}
		System.out.println("Media RTT: "+ mediaRTT);
		if(contRespostas>1){
			for ( int i =0; i< contRespostas; i++){
				desvioPadraoRTT += Math.pow((double)(RTT.get(i) - mediaRTT), 2)/(contRespostas-1);
			}
			desvioPadraoRTT = Math.sqrt(desvioPadraoRTT);
		}
		else{
			desvioPadraoRTT = 0;
		}
				
		System.out.println("Desvio Padrao RTT: "+ desvioPadraoRTT);
		RTTMax= mediaRTT + desvioPadraoRTT;
		
		return (int)RTTMax;
	}
}
