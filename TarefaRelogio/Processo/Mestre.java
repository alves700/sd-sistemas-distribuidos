package Processo;

import java.net.DatagramPacket;

import Comunicação.Comunicacao;

public class Mestre extends Processo implements Runnable {
	//Variáveis do mestre.
	private final long tempoEnvioHello = 1000;
	private long ultimoHelloEnviado;
	
	
	public void iniciaVariaveis(){
		ultimoHelloEnviado = System.currentTimeMillis();
	}


	@Override
	public void run() {
		iniciaVariaveis();
		while(true){
			if(System.currentTimeMillis() > ultimoHelloEnviado + tempoEnvioHello){
				mc.enviaMsg(""+Comunicacao.HELLO+" "+"hello");
				ultimoHelloEnviado = System.currentTimeMillis();
			}
			
			verficaBufferEntrada();
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void processaMensagem(DatagramPacket dp){
		String msg[] = comm.getMulticast().getMsg(dp).split(" ");
		 
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
				System.exit(0);
				break;
			case Comunicacao.CALC_RTT_MAX:
				break;
			
		}
	}
	public int calculaRTTmax(){
		
		int RTT[] = new int[comm.getContatos().size()];
		double mediaRTT = 0;
		double RTTMax = 0;
		double desvioPadraoRTT = 0;
		int maxTime = 1000; //1s
		int contRespostas = 0;

		
		long startTime = System.currentTimeMillis();
		
		mc.enviaMsg(""+ Comunicacao.CALC_RTT_MAX +"rtt");
		
		while( System.currentTimeMillis() - startTime < maxTime){
			if ( mc.existeMsg()){
				
				String msg[] =  mc.getMsg(mc.getDatagram()).split(" ");
				//se o tipo da msg for de CALC_RTT_MAX
				if ( msg[Comunicacao.INDEX_TIPO].equals(""+Comunicacao.CALC_RTT_MAX)){
					
					RTT[contRespostas] =(int)(System.currentTimeMillis() - startTime);
					
					contRespostas++;
				}
			}
			
		}
		for ( int i =0; i< contRespostas; i++){
			mediaRTT += (double)RTT[i]/contRespostas; 
		}
		for ( int i =0; i< contRespostas; i++){
			desvioPadraoRTT += Math.pow((double)(RTT[i] - mediaRTT), 2)/contRespostas;
			desvioPadraoRTT = Math.sqrt(desvioPadraoRTT);
		}
		RTTMax= mediaRTT + desvioPadraoRTT;
		
		return (int)RTTMax;
	}
}
