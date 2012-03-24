package Processo;

import java.net.DatagramPacket;

import Comunicação.*;

public class Processo extends Thread {
	
	Comunicacao comm = new Comunicacao();
	
	private int RTTmax;
	private int idMestre;
	private int ID;
	
	public static void main(String [] args){
		Processo p = new Processo();
		p.start();
	}
	public Processo(){
		ID = (int) (Math.random()*10);
		idMestre = 10;
	}
	public void run(){
		
		configuraSockets();
		comm.reconheceOutrosProcessos(ID);
		System.out.println("Termino do Reconhecimento");
		
		
	 	for ( String i[] : comm.getContatos()){
	   		System.out.println("ip: "+ i[Comunicacao.INDEX_IP] + "  id: "+ i[Comunicacao.INDEX_ID]);
	   	}
	 	
		while(true){
			try {
				
				verficaBufferEntrada();
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void configuraSockets(){
		
	}
	public void verficaBufferEntrada(){
		
		if (comm.getMulticast().existeMsg()){
			processaMensagem(comm.getMulticast().getDatagram());
		}
		if (comm.getUnicast().existeMsg()){
			processaMensagem(comm.getUnicast().getDatagram());
		}
	}
	public void processaMensagem(DatagramPacket dp){
		
		String msg[] = comm.getMulticast().getMsg(dp).split(" ");
		
		switch( msg[Comunicacao.INDEX_TIPO] ){
		
		}
		
	}
	public int calculaRTTmax(){
		
		int RTT[] = new int[comm.getContatos().size()];
		double mediaRTT = 0;
		double RTTMax = 0;
		double desvioPadraoRTT = 0;
		int maxTime = 1000; //1s
		int contRespostas = 0;
		Multicast mc = comm.getMulticast();
		
		long startTime = System.currentTimeMillis();
		
		mc.enviaMsg(""+ Comunicacao.TIPO_MSG.CALC_RTT_MAX +"rtt");
		
		while( System.currentTimeMillis() - startTime < maxTime){
			if ( mc.existeMsg()){
				
				String msg[] =  mc.getMsg(mc.getDatagram()).split(" ");
				//se o tipo da msg for de CALC_RTT_MAX
				if ( msg[Comunicacao.INDEX_TIPO].equals(Comunicacao.TIPO_MSG.CALC_RTT_MAX)){
					
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
