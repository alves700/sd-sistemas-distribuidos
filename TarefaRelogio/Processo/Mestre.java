package Processo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

import Comunicação.Comunicacao;
/** 
Classe responsável pelo ajuste do relógio entre processos.
*/
public class Mestre extends Processo{
	
	//chavesDeCritografia
	private String chavePrivada;
	private String chavePublica;
	
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
		geraChaves();
	}
	/** 
	Gera as chaves privada e pública do mestre.
	*/
	public void geraChaves(){
		
	}
	
	/** 
	Inicia atributos de temporização
	*/
	public void iniciaVariaveis(){
		ultimoHelloEnviado = 0;
		ultimoReqRTTEnviado = 0;
		ultimoReqRelogioEnviado  = 0;
	}
	
	/** 
	Método principal da thread. Inicialmente ocorre o envio das chaves públicas pelo mestre, e este entra em loop onde envia mensagens,
	verifica se há novas mensagens no buffer de entrada e verifica updates.  
	*/
	@Override
	public void run() {
		try {
			iniciaVariaveis();
			enviaChavePublica();
			while(true){
					envioDeMensagens();
					verficaBufferEntrada();
					update();
					Thread.sleep(1);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	/** 
	Envia a chave pública através da comunicação Multicast.  
	*/
	public void enviaChavePublica() throws IOException{
		   mc.enviaMsg(comm.protMsg(Comunicacao.CHAVE_PUB,ID, chavePublica));
	}
	/** 
	Envia mensagens Hello, Requisição de RTT e Requisição de Relógio.  
	*/
	public void envioDeMensagens() throws IOException{
		enviaHello();
		enviaReqRTT();
		enviaReqRelogio();
	}
	/** 
	Envia uma mensagem do tipo Hello via Multicast.  
	*/
	private void enviaHello() throws IOException{
		//Envio de Hello periodicamente.
		if(System.currentTimeMillis() > ultimoHelloEnviado + tempoEnvioHello){
		   mc.enviaMsg(comm.protMsg(Comunicacao.HELLO,ID));
		   ultimoHelloEnviado = System.currentTimeMillis();
		}
	}
	/** 
	Envia uma mensagem do tipo Requisição RTT via Multicast.  
	*/
	private void enviaReqRTT() throws IOException{
		//Envio de requisição troca de mensagens para cálculo de RTT.
		if(System.currentTimeMillis() > ultimoReqRTTEnviado + tempoReqRTT){
			
			mc.enviaMsg(comm.protMsg(Comunicacao.CALC_RTT_MAX,ID));
			ultimoReqRTTEnviado = System.currentTimeMillis();
			requerindoRTT = true;
		}
	}
	/** 
	Envia uma mensagem do tipo Requisição do Relógio via Multicast.  
	*/
	private void enviaReqRelogio() throws IOException{
		//Envio de mensagem para requisição de relógios
		if(System.currentTimeMillis() > ultimoReqRelogioEnviado + tempoReqRelogio){
			mc.enviaMsg(comm.protMsg(Comunicacao.REQ_RELOGIO,ID));
			requerindoRelogio = true;
			relogios.add(comm.getIP()+" "+ID+" "+convertHoursMillis(getHorario())+" "+0);
			ultimoReqRelogioEnviado = System.currentTimeMillis();
		}
	}
	/** 
	Atualiza o RTT máximo e o relógio atual.
	*/  
	public void update() throws IOException{
		updateRTT();
		updateRelogio();
	}
	/** 
	Após receber as mensagens dos escravos, aplica o algoritmo de Berkeley para o ajuste do relógio que deve ser enviado para cada escravo. 
	*/
	public void updateRelogio() throws NumberFormatException, IOException{
		if(requerindoRelogio && System.currentTimeMillis() > ultimoReqRelogioEnviado + tempoEsperaRelogio){
			requerindoRelogio =false;
			long media = calcNovoRelogio();
			ajusteNovoRelogio(media);
			relogios.clear();
		}
	}
	/** 
	Cálcula novo relógio a partir do relógio do mestre e dos escravos que enviaram o relógio em um tempo inferior a RTTMax.
	@return a média dos relógios que participaram do algortimo de Berkeley. 
	*/
	public long calcNovoRelogio(){
		int numRelogios = 0;
		long media = 0;
		
		for(int i = 0 ; i< relogios.size();i++){
			
			String [] rel = relogios.get(i).split(" ");
			
			if(Long.parseLong(rel[indRTT]) <= RTTMax){
				media += (Long.parseLong(rel[indREL]) + Long.parseLong(rel[indRTT])/2);
				//System.out.println("tempo ms:"+ rel[indREL]+" RTT:"+Long.parseLong(rel[indRTT])/2);
				numRelogios++;
			}
		}
		
		media = media/numRelogios;
		System.out.println("Numero de processos que participaram do Algoritmo de Berkeley: "+numRelogios);
		return media;
	}
	/** 
	Envia mensagens de ajuste para os escravos. Mestre atualiza o seu relógio nesse método.
	@param media - media dos relógios que participaram do algoritmo de Berkeley. 
	*/
	public void ajusteNovoRelogio(long media) throws NumberFormatException, IOException{
		for(int i = 0 ; i< relogios.size();i++){
			
			String [] rel = relogios.get(i).split(" ");
			
			if(Long.parseLong(rel[indID]) != ID){
				long ajuste =  media- Long.parseLong(rel[indREL]);
				String msg = criptografa(""+ ID +" "+ajuste, chavePrivada);//Criptografa o ID junto com a msg
				uc.enviaMsg(rel[indIP], Integer.parseInt(rel[indID]), ""+ Comunicacao.AJUSTE_RELOGIO +" "+  msg);// naum usei o protMSG pq o ID esta junto com a msg critografada
			}
			else{
				long ajuste =  media- Long.parseLong(rel[indREL]);
				System.out.println("Novo ajuste feito de: "+ajuste+"ms do tempo atual");
				
				String horaAtual = getHorario(); 
				setHorario(converMillisHours(convertHoursMillis(horaAtual)+ajuste));
			}
		}
	}
	/** 
	Atualiza o valor do RTT máximo calculando-o através do método calculaRTTmax(). 
	*/
	private void updateRTT() throws IOException{
		// Cálcula o RTT máximo após o tempoEsperaRTT ter passado.
		if(requerindoRTT && System.currentTimeMillis() > ultimoReqRTTEnviado + tempoEsperaRTT){
			
			requerindoRTT = false;
			RTTMax = calculaRTTmax();
			System.out.println("RTTMaximo Calculado: "+RTTMax+" Num de processos que participaram: "+RTT.size());
			
			RTT.clear();
			
		}
	}
	/** 
	Verifica o tipo de mensagem que chegou via Unicast. Possíveis mensagens: 
	Requisição de relógio e Cálculo de RTT máximo. Essas mensagens chegam a partir dos escravos
	@param dp - DatagramPacket da mensagem que chegou.    
	*/
	public void processaMensagem(DatagramPacket dp){
		String[] msg = mc.getMsg(dp).split(" ");
		 
		switch( Integer.parseInt(msg[Comunicacao.INDEX_TIPO]) ){

			case Comunicacao.REQ_RELOGIO:
				addRelogio(dp, msg);
				break;
			case Comunicacao.CALC_RTT_MAX:
				addRTT(msg);
				break;
			
		}
	}
	/** 
	Adiciona os dados de relógios que chegaram em uma lista.
	@param dp - possui informação do IP do processo.
	@param msg - possui a informação do relógio.
	*/
	private void addRelogio(DatagramPacket dp, String msg[]) {
		if(requerindoRelogio && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
			long RTT = (System.currentTimeMillis()-ultimoReqRelogioEnviado);
			relogios.add(mc.getIP(dp)+" "+msg [Comunicacao.INDEX_ID]+" "+msg[Comunicacao.INDEX_MSG]+" "+ RTT);//IP + ID + RELOGIO + RTT
		}
	}
	/** 
	Verifica se o mestre está requerindo mensagens para o cálculo de RTT, se estiver armazena o valor do RTT da mensagem que chegou
	@param msg - possui ID do processo.
	*/
	private void addRTT(String msg[]){
		//Caso recebeu msg de Calc de RTT Max que não seja de si mesmo, e está requerindo RTT's adiciona o tempo de RTT na lista. 
		if(requerindoRTT && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
			int rttt = (int) (System.currentTimeMillis()-ultimoReqRTTEnviado);
			//System.out.println("Msg de ID: " +  msg[Comunicacao.INDEX_ID]+" Seu RTT: "+rttt);
			RTT.add(rttt);
		}
	}
	/** 
	O cálculo do RTT máximo foi feito utilizando a média das mensagens de RTT que chegam pelos escravos somada ao desvio padrão desses RTTs.
	@return RTTMax.
	*/
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
	/** 
	Aplica o algoritmo de criptografia em uma mensagem.
	@param msg - que será criptografada.
	@param chave - chave de criptografia
	@return mensagem criptografada.
	*/
	public String criptografa(String msg, String chave){
		//implementar CRITOGRAFIA
		return msg;
	}
}
