package TarefaRelogio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/** 
Classe respons�vel pelo ajuste do rel�gio entre processos.
*/
public class Mestre extends Processo{
	
	/** Segmento de mensagem.*/
	private long segmento = 1;
	
	/** Gerador de chaves aleat�rias.*/
	private SecureRandom random;

	/** Chave p�blica.*/
	private Key chavePublica;
	/** Chave privada.*/
	private Key chavePrivada;
	
	//Vari�veis de Hello.
	/** Per�odo em que mestre envia hello para escravos.*/
	private final long tempoEnvioHello = 500;
	/** Tempo em que o ultimo hello foi enviado.*/
	private long ultimoHelloEnviado;
	
	//Vari�veis de Calculo do RTT maximo
	/** Valor do RTT m�ximo.*/
	private int RTTMax = 0;
	
	private final int maxP_RTTMax = 4;
	private int [] P_RMax = new int[maxP_RTTMax];
	private int pos_P_RMax = 0;
	
	/** Tempo de espera para a requisi�ao de RTTs dos escravos.*/
	private final long tempoEsperaRTT = 1000;//Espera por 1s o RTT de outros escravos
	/** Per�odo em que mestre envia requisi�a� de RTT para escravos.*/
	private final long tempoReqRTT = 5000;//Recalcula o RTT de 20 em 20 segundos.
	/** Informa o status da requisi��o de RTTs.*/
	private boolean requerindoRTT = false;//True quando mestre requisita RTT, modificada para false quando RTT � calculado.
	/** Tempo em que ocorreu a �ltima requisi��o do RTT.*/
	private long ultimoReqRTTEnviado; //"Hor�rio" em que ocorreu a ultima requisi��o de RTT, tamb�m � utilizada para o c�lculo do RTT de um processo.
	
	/** Lista de RTTs armazenados.*/
	ArrayList <Integer> RTT = new ArrayList <Integer>(); // ArrayList que armazena os RTTs dos processos.
	
	//Vari�veis que controlam a requisi��o de rel�gio
	/** Per�odo em que mestre envia requisi��o de rel�gio para escravos.*/
	private final long tempoReqRelogio = 5000;//Tempo entre um requerimento e outro do rel�gio
	/** Tempo de espera para a requisi��o de rel�gios dos escravos.*/
	private final long tempoEsperaRelogio = 1000;
	/** Informa o status da requisi��o de rel�gios.*/
	private boolean requerindoRelogio = false;
	/** Tempo em que ocorreu a �ltima requisi��o de rel�gios.*/
	private long ultimoReqRelogioEnviado;
	
	/** Index IP utilizado para a requisi��o e ajuste de rel�gios.*/
	private final int indIP = 0;
	/** Index ID utilizado para a requisi��o e ajuste de rel�gios.*/
	private final int indID = 1;
	/** Index do rel�gio utilizado para a requisi��o e ajuste de rel�gios.*/
	private final int indREL = 2;
	/** Index do RTT utilizado para a requisi��o e ajuste de rel�gios.*/
	private final int indRTT = 3;
	
	/** Lista que cont�m informa��es de IP ID Rel�gio e RTT dos escravos e do pr�prio mestre.*/
	private ArrayList <String> relogios = new ArrayList <String>(); //ArryList que armazena IPs IDs e os relogios recebidos por processos e seus RTTs.
	
	
	public Mestre() throws IOException {
		System.out.println("Sou o Mestre.");
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		try {
				geraChaves();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	/** 
	Gera as chaves privada e p�blica do mestre.
	*/
	public void geraChaves() throws NoSuchAlgorithmException, NoSuchProviderException{
		
		random = new SecureRandom();
	    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

	    generator.initialize(1024);

	    KeyPair pair = generator.generateKeyPair();
	    chavePublica = pair.getPublic();
	    chavePrivada = pair.getPrivate();
	}
	
	/** 
	Inicia atributos de temporiza��o
	*/
	public void iniciaVariaveis(){
		ultimoHelloEnviado = 0;
		ultimoReqRTTEnviado = 0;
		ultimoReqRelogioEnviado  = 0;
	}
	
	/** 
	M�todo principal da thread. Inicialmente ocorre o envio das chaves p�blicas pelo mestre, e este entra em loop onde envia mensagens,
	verifica se h� novas mensagens no buffer de entrada e verifica updates.  
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
	Envia a chave p�blica atrav�s da comunica��o Multicast.  
	*/
	public void enviaChavePublica() throws IOException{
		
		KeyFactory fact;
		RSAPublicKeySpec pub = null;
		try {
			fact = KeyFactory.getInstance("RSA");
			pub = fact.getKeySpec(chavePublica, RSAPublicKeySpec.class);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    		
    	String m = pub.getModulus().toString();
    	String e = pub.getPublicExponent().toString();
    	//System.out.println(chavePublica);
	    mc.enviaMsg(comm.protMsg(Comunicacao.CHAVE_PUB, ID, ""+ m + " "+ e));
	}
	/** 
	Envia mensagens Hello, Requisi��o de RTT e Requisi��o de Rel�gio.  
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
	Envia uma mensagem do tipo Requisi��o RTT via Multicast.  
	*/
	private void enviaReqRTT() throws IOException{
		//Envio de requisi��o troca de mensagens para c�lculo de RTT.
		if(System.currentTimeMillis() > ultimoReqRTTEnviado + tempoReqRTT){
			
			mc.enviaMsg(comm.protMsg(Comunicacao.CALC_RTT_MAX,ID));
			ultimoReqRTTEnviado = System.currentTimeMillis();
			requerindoRTT = true;
		}
	}
	/** 
	Envia uma mensagem do tipo Requisi��o do Rel�gio via Multicast.  
	*/
	private void enviaReqRelogio() throws IOException{
		//Envio de mensagem para requisi��o de rel�gios
		if(System.currentTimeMillis() > ultimoReqRelogioEnviado + tempoReqRelogio){
			mc.enviaMsg(comm.protMsg(Comunicacao.REQ_RELOGIO,ID));
			requerindoRelogio = true;
			relogios.add(comm.getIP()+" "+ID+" "+convertHoursMillis(getHorario())+" "+0);
			ultimoReqRelogioEnviado = System.currentTimeMillis();
		}
	}
	/** 
	Atualiza o RTT m�ximo e o rel�gio atual.
	*/  
	public void update() throws IOException{
		updateRTT();
		updateRelogio();
	}
	/** 
	Ap�s receber as mensagens dos escravos, aplica o algoritmo de Berkeley para o ajuste do rel�gio que deve ser enviado para cada escravo. 
	*/
	public void updateRelogio() throws NumberFormatException, IOException{
		if(requerindoRelogio && System.currentTimeMillis() > ultimoReqRelogioEnviado + tempoEsperaRelogio){
			requerindoRelogio =false;
			long media = calcNovoRelogio();
			ajusteNovoRelogio(media);
			relogios.clear();
			segmento++;
		}
	}
	/** 
	C�lcula novo rel�gio a partir do rel�gio do mestre e dos escravos que enviaram o rel�gio em um tempo inferior a RTTMax.
	@return a m�dia dos rel�gios que participaram do algortimo de Berkeley. 
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
	Envia mensagens de ajuste para os escravos. Mestre atualiza o seu rel�gio nesse m�todo.
	@param media media dos rel�gios que participaram do algoritmo de Berkeley. 
	*/
	public void ajusteNovoRelogio(long media) throws NumberFormatException, IOException{
		for(int i = 0 ; i< relogios.size();i++){
			
			String [] rel = relogios.get(i).split(" ");
			
			if(Long.parseLong(rel[indID]) != ID){
				long ajuste =  media- Long.parseLong(rel[indREL]);
				String msg = (ajuste +" "+ criptografa(ID +" "+ segmento)); 
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
	Atualiza o valor do RTT m�ximo calculando-o atrav�s do m�todo calculaRTTmax(). 
	*/
	private void updateRTT() throws IOException{
		// C�lcula o RTT m�ximo ap�s o tempoEsperaRTT ter passado.
		if(requerindoRTT && System.currentTimeMillis() > ultimoReqRTTEnviado + tempoEsperaRTT){
			
			requerindoRTT = false;
			P_RMax[pos_P_RMax] = calculaRTTmax();
			pos_P_RMax = (pos_P_RMax+1) % maxP_RTTMax; 
			RTTMax = 0;
			for(int i = 0 ; i<P_RMax.length ; i++){
				RTTMax +=P_RMax[i];
			}
			RTTMax /= P_RMax.length;
			
			RTTMax += RTTMax*0.1;
			System.out.println("RTTMaximo Calculado: "+RTTMax+" Num de processos que participaram: "+RTT.size());
			
			RTT.clear();
			
		}
	}
	/** 
	Verifica o tipo de mensagem que chegou via Unicast. Poss�veis mensagens: 
	Requisi��o de rel�gio e C�lculo de RTT m�ximo. Essas mensagens chegam a partir dos escravos
	@param dp DatagramPacket da mensagem que chegou.    
	*/
	public void processaMensagem(DatagramPacket dp) throws UnsupportedEncodingException{
		String[] msg = mc.getMsg(dp).split(" ");
		 
		switch( Integer.parseInt(msg[Comunicacao.INDEX_TIPO]) ){
			case Comunicacao.ELEICAO:
				System.exit(0);
			break;
			case Comunicacao.REQ_RELOGIO:
				addRelogio(dp, msg);
				break;
			case Comunicacao.CALC_RTT_MAX:
				addRTT(msg);
				break;
			
		}
	}
	/** 
	Adiciona os dados de rel�gios que chegaram em uma lista.
	@param dp possui informa��o do IP do processo.
	@param msg possui a informa��o do rel�gio.
	*/
	private void addRelogio(DatagramPacket dp, String msg[]) {
		if(requerindoRelogio && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
			long RTT = (System.currentTimeMillis()-ultimoReqRelogioEnviado);
			relogios.add(mc.getIP(dp)+" "+msg [Comunicacao.INDEX_ID]+" "+msg[Comunicacao.INDEX_MSG]+" "+ RTT);//IP + ID + RELOGIO + RTT
		}
	}
	/** 
	Verifica se o mestre est� requerindo mensagens para o c�lculo de RTT, se estiver armazena o valor do RTT da mensagem que chegou
	@param msg possui ID do processo.
	*/
	private void addRTT(String msg[]){
		//Caso recebeu msg de Calc de RTT Max que n�o seja de si mesmo, e est� requerindo RTT's adiciona o tempo de RTT na lista. 
		if(requerindoRTT && Integer.parseInt(msg [Comunicacao.INDEX_ID]) != ID){
			int rttt = (int) (System.currentTimeMillis()-ultimoReqRTTEnviado);
			//System.out.println("Msg de ID: " +  msg[Comunicacao.INDEX_ID]+" Seu RTT: "+rttt);
			RTT.add(rttt);
		}
	}
	/** 
	O c�lculo do RTT m�ximo foi feito utilizando a m�dia das mensagens de RTT que chegam pelos escravos somada ao desvio padr�o desses RTTs.
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
	@param msg que ser� criptografada.
	@return mensagem criptografada.
	*/
	public String criptografa(String msg) throws UnsupportedEncodingException{
		byte[] cipherText = null;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, chavePrivada);
			cipherText = cipher.doFinal(msg.getBytes());
		    //System.out.println("cipher: " + new String(cipherText, "ISO-8859-1"));
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	   
		return new String( cipherText, "ISO-8859-1");
	}
}
