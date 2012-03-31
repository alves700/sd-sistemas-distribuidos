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
	private final long tempoReqRTT = 20000;//Recalcula o RTT de 20 em 20 segundos.
	private boolean requerindoRTT = false;//True quando mestre requisita RTT, modificada para false quando RTT é calculado. 
	private long ultimoReqRTTEnviado; //"Horário" em que ocorreu a ultima requisição de RTT, também é utilizada para o cálculo do RTT de um processo.
	
	ArrayList <Integer> RTT = new ArrayList <Integer>(); // ArrayList que armazena os RTTs dos processos.
	
	//Variáveis que controlam a requisição de relógio
	private final long tempoReqRelogio = 5000;//Tempo entre um requerimento e outro do relógio
	private final long tempoEsperaRelogio = 1000;
	private boolean requerindoRelogio = false;
	private long ultimoReqRelogioEnviado;
	
	private final int indIP = 0;
	private final int indID = 1;
	private final int indREL = 2;
	private final int indRTT = 3;
	
	
	private ArrayList <String> relogios = new ArrayList <String>(); //ArryList que armazena IPs IDs e os relogios recebidos por processos e seus RTTs.
	
	
	public Mestre() throws IOException {
		System.out.println("Sou o Mestre.");
	}
	
	public void iniciaVariaveis(){
		ultimoHelloEnviado = 0;
		ultimoReqRTTEnviado = 0;
		ultimoReqRelogioEnviado  = 0;
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
		enviaHello();
		enviaReqRTT();
		enviaReqRelogio();
	}
	private void enviaHello(){
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
	}
	private void enviaReqRTT() throws IOException{
		//Envio de requisição troca de mensagens para cálculo de RTT.
		if(System.currentTimeMillis() > ultimoReqRTTEnviado + tempoReqRTT){
			
			mc.enviaMsg(comm.protMsg(Comunicacao.CALC_RTT_MAX,ID));
			ultimoReqRTTEnviado = System.currentTimeMillis();
			requerindoRTT = true;
		}
	}
	private void enviaReqRelogio() throws IOException{
		//Envio de mensagem para requisição de relógios
		if(System.currentTimeMillis() > ultimoReqRelogioEnviado + tempoReqRelogio){
			mc.enviaMsg(comm.protMsg(Comunicacao.REQ_RELOGIO,ID));
			requerindoRelogio = true;
			relogios.add(comm.getIP()+" "+ID+" "+convertHoursMillis(getHorario())+" "+0);
			ultimoReqRelogioEnviado = System.currentTimeMillis();
		}
	}
	public void update() throws IOException{
		updateRTT();
		updateRelogio();
	}
	public void updateRelogio() throws NumberFormatException, IOException{
		if(requerindoRelogio && System.currentTimeMillis() > ultimoReqRelogioEnviado + tempoEsperaRelogio){
			requerindoRelogio =false;
			long media = calcNovoRelogio();
			ajusteNovoRelogio(media);
			relogios.clear();
		}
	}
	public long calcNovoRelogio(){
		int numRelogios = 0;
		long media = 0;
		
		for(int i = 0 ; i< relogios.size();i++){
			
			String [] rel = relogios.get(i).split(" ");
			
			if(Long.parseLong(rel[indRTT]) <= RTTMax){
				media += Long.parseLong(rel[indREL]) + Long.parseLong(rel[indRTT])/2;
				numRelogios++;
			}
		}
		
		media = media/numRelogios;
		System.out.println("Numero de processos que participaram do Algoritmo de Berkeley: "+numRelogios);
		return media;
	}
	public void ajusteNovoRelogio(long media) throws NumberFormatException, IOException{
		for(int i = 0 ; i< relogios.size();i++){
			
			String [] rel = relogios.get(i).split(" ");
			
			if(Long.parseLong(rel[indID]) != ID){
				long ajuste =  media- Long.parseLong(rel[indREL]);
				uc.enviaMsg(rel[indIP], Integer.parseInt(rel[indID]), comm.protMsg(Comunicacao.AJUSTE_RELOGIO, ID, ""+ajuste));
			}
			
			else{
				long ajuste =  media- Long.parseLong(rel[indREL]);
				System.out.println("Novo ajuste feito de: "+ajuste+"ms do tempo atual");
				
				String horaAtual = getHorario(); 
				setHorario(converMillisHours(convertHoursMillis(horaAtual)+ajuste));
			}
		}
	}
	
	private void updateRTT() throws IOException{
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
				addRelogio(dp, msg);
				break;
			case Comunicacao.AJUSTE_RELOGIO:
				break;
			case Comunicacao.RECONHECIMENTO:
				break;
			case Comunicacao.ELEICAO:
				// Caso recebeu mensagem de eleição é pq o tempo limite para hello estourou em pelo menos 
				// um processo, então mestre se mata.
				//System.exit(0);
				break;
			case Comunicacao.CALC_RTT_MAX:
				addRTT(msg);
				break;
			
		}
	}
	private void addRelogio(DatagramPacket dp, String msg[]) {
		if(requerindoRelogio && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
			long RTT = (System.currentTimeMillis()-ultimoReqRelogioEnviado);
			relogios.add(mc.getIP(dp)+" "+msg [Comunicacao.INDEX_ID]+" "+msg[Comunicacao.INDEX_MSG]+" "+ RTT);//IP + ID + RELOGIO + RTT
		}
	}
	private void addRTT(String msg[]){
		//Caso recebeu msg de Calc de RTT Max que não seja de si mesmo, e está requerindo RTT's adiciona o tempo de RTT na lista. 
		if(requerindoRTT && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
			int rttt = (int) (System.currentTimeMillis()-ultimoReqRTTEnviado);
			//System.out.println("Msg de ID: " +  msg[Comunicacao.INDEX_ID]+" Seu RTT: "+rttt);
			RTT.add(rttt);
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
		//System.out.println("Media RTT: "+ mediaRTT);
		if(contRespostas>1){
			for ( int i =0; i< contRespostas; i++){
				desvioPadraoRTT += Math.pow((double)(RTT.get(i) - mediaRTT), 2)/(contRespostas-1);
			}
			desvioPadraoRTT = Math.sqrt(desvioPadraoRTT);
		}
		else{
			desvioPadraoRTT = 0;
		}
				
		//System.out.println("Desvio Padrao RTT: "+ desvioPadraoRTT);
		RTTMax= mediaRTT + desvioPadraoRTT;
		
		return (int)RTTMax;
	}
}
