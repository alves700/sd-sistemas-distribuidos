package Processo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Comunica��o.Comunicacao;

public class Escravo extends Processo{
	
	//chavesDeCriptografia public do mestre
	private Key chavePublicaMestre;
	
	//Vari�veis de Eleicao.
	private final long tempoEleicao = 5000; // tempo da elei��o demora 5 s
	private long tempoInicioEleicao = Long.MIN_VALUE; //Armazena o tempo em que a elei��o � iniciada 
	private boolean eleicaoOcorrendo = false;
	
	//Vari�veis do Hello.
	private final long tempoEsperaHello = 3000; // tempo de espera m�ximo aguardado pelo Hello do mestre.
	private long ultimoHelloRecebido; //Armazena o tempo em que o ultimo hello foi recebido.
	
	private int idNovoMestre = -1;
	
	
	public Escravo() throws IOException{
		System.out.println("Sou um Escravo");
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		try {
			cipher = Cipher.getInstance("RSA", "BC");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 
	Inicia atributos de temporiza��o
	*/
	public void iniciaVariaveis(){
		ultimoHelloRecebido = System.currentTimeMillis();
	}
	
	/** 
	Escravo envia sua ID caso ela seja maior que a ID armazenada por mensagens de elei��es passadas recebidas pelo escravo.
	*/
	public void eleicao() throws IOException{
		if(!eleicaoOcorrendo){
			tempoInicioEleicao = System.currentTimeMillis();
			eleicaoOcorrendo = true;
			if(ID > idNovoMestre){
				mc.enviaMsg(comm.protMsg(comm.ELEICAO, ID));
			}
		}
	}
	/** 
	M�todo principal da thread. Entra em loop onde envia mensagens tanto para mestres quanto para outros escravos (no processo de elei��o),
	verifica se h� novas mensagens no buffer de entrada e verifica updates.  
	*/
	@Override
	public void run(){
		iniciaVariaveis();
		while(!this.isInterrupted()){
			try{
				envioDeMensagens();
				verficaBufferEntrada();
				update();
				
				if(!this.isInterrupted()){
					Thread.sleep(1);
				}
				
			}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
	}
	/** 
	Caso o mestre n�o est� respondendo envia mensagem de Elei��o para outros processos. Os outros envios de mensagem para o mestre ocorrem na pr�pria
	verifica��o de mensagens no buffer de entrada.
	*/
	public void envioDeMensagens() throws IOException{
		//Verifica quando foi a ultima vez que recebeu um hello, caso o tempo seja ultrapassado, inicia elei��o.
		if(System.currentTimeMillis() > ultimoHelloRecebido + tempoEsperaHello){
			eleicao();
		}
		
	}
	/** 
	Verifica se h� atualiza��o do mestre ap�s o t�rmino da elei��o. Outras atualiza��es ocorrem na verifica��o do buffer de entrada. 
	*/
	public void update(){
		updateEleicaoMestre();
	}
	/** 
	Ap�s o t�rmino da elei��o verifica o ID do novo mestre. Se for o pr�prio processo, cria um objeto Mestre e inicia-o, logo ap�s interrompe o pr�prio
	processo escravo.
	*/
	public void updateEleicaoMestre(){
		//Verifica se a eleicao ainda est� ocorrendo, se estiver: eleicao acaba, e o mestre � consagrado.
		if(eleicaoOcorrendo && System.currentTimeMillis() > tempoInicioEleicao + tempoEleicao){
			eleicaoOcorrendo = false;
			idMestre = idNovoMestre;
			idNovoMestre = -1;
			
			if(idMestre != ID){
				System.out.println("NovoMestre ID " + idMestre);	
			}
			// Caso o esse escravo foi eleito como mestre, e�e instancia uma thread Mestre e finaliza seu processo como escravo.
			else{
				
				Mestre m;
				try {
					m = this.criaMestre();
					m.start();
					this.interrupt();
				} catch (IOException e2) {e2.printStackTrace();}
				catch (Throwable e) {e.printStackTrace();}
			}
			//Vari�veis s�o reiniciadas.
			iniciaVariaveis();
		}
		
	}
	/** 
	Verifica o tipo de mensagem que chegou via Unicast ou Multicast, logo ap�s faz ou n�o atualiza��o. 
	Poss�veis mensagens: Hello, Requisi��o de rel�gio, Ajuste de Rel�gio, Elei��o, C�lculo do RTT m�ximo e de Envio de Chave P�blica (pelo mestre). 
	Essas mensagens chegam a partir de outros escravos ou do pr�prio mestre.
	@param dp - DatagramPacket da mensagem que chegou.    
	*/
	public void processaMensagem(DatagramPacket dp) throws IOException{
		String x = mc.getMsg(dp);
		String msg[] = x.split(" ");
		
		switch( Integer.parseInt(msg[Comunicacao.INDEX_TIPO]) ){
			//Reconfigura o ultimo hello recebido.
			case Comunicacao.HELLO:
				ultimoHelloRecebido = System.currentTimeMillis();
				break;
			case Comunicacao.REQ_RELOGIO:
				enviaMsgRelogio();
				break;
			case Comunicacao.AJUSTE_RELOGIO:
				String auxMsg[] = descriptografa(x.substring(2)).split(" "); // descripografa e separa o ID da msg
				//System.out.println("Debug" + auxMsg[1]);
				if ( Integer.parseInt(auxMsg[0]) == idMestre ){ //verifica se o ID est� correto. Teste para ver se a descritografica ocorreu certo
					ajustaRelogio(auxMsg[1]); // ajuda o rel�gio;
				}else{
					System.out.println("Computador Mal intencionado na rede");
				}			
				break;
			case Comunicacao.ELEICAO:
				verificaEleicao(msg);
				break;
			case Comunicacao.CALC_RTT_MAX:
				getHorario(); //(Necess�rio, pois essa opera��o ser� feita quando o mestre pedir o hor�rio, e ela demora 15ms no m�nimo ��).
				enviaMsgRTT();
				break;
			case Comunicacao.CHAVE_PUB:
				updateChavePublica(x); //passa a String com a mensagem inteira pois essa eh a unica msg que vem com 4 parametros.
				 //recebe chave publica do mestre e seta em seu atributo para utilizar na autentica��o posteriormente
		}
	}
	public void updateChavePublica(String obj){
		try {
		    BigInteger m = new BigInteger(obj.split(" ")[Comunicacao.INDEX_MSG]);
		    BigInteger e = new BigInteger(obj.split(" ")[Comunicacao.INDEX_MSG +1]);
		    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		    KeyFactory fact = KeyFactory.getInstance("RSA");
		    chavePublicaMestre = fact.generatePublic(keySpec);
		    System.out.println(chavePublicaMestre);
		  } catch (Exception e) {
		    throw new RuntimeException("Spurious serialisation error", e);
		  }
	}
	/** 
	Faz ajuste do rel�gio do escravo.
	@param msg - Mensagem contendo um valor em millisegundos de ajuste.    
	*/
	public void ajustaRelogio(String msg) throws IOException{
		long ajuste = Long.parseLong(msg);
		System.out.println("Novo ajuste feito de: "+ajuste+"ms do tempo atual");
		String horaAtual = getHorario(); 
		setHorario(converMillisHours(convertHoursMillis(horaAtual)+ajuste));
	}
	/** 
	Verifica mensagens que chegam de outros processos e compara o ID da mensagem com o maior ID armazenado no processo de elei��o. 
	Faz a atualiza��o do ID do novo mestre.
	@param msg - Mensagem contendo um ID.    
	*/
	public void verificaEleicao(String msg[]){
		//Verifica se a ID da mensagem de elei��o que chegou � maior ou igual a sua. Se for
		//armazena o id do novo mestre como sendo o ID da mensagem que chegou.
		if(Integer.parseInt(msg[Comunicacao.INDEX_ID]) > idNovoMestre ){
			idNovoMestre = Integer.parseInt(msg[Comunicacao.INDEX_ID]);
		}
		
	}
	/** 
	Envia mensagem do tipo Calculo do RTT m�ximo para o mestre via Unicast.    
	*/
	public void enviaMsgRTT() throws IOException{
		//Envia mensagem para o mestre para isso utiliza o Ip do mestre e seu Id (ID calcula a porta do mestre).
		//O conteudo da msg informa que o tipo � de CALC RTT MAX e a ID desse processo escravo.
		uc.enviaMsg(ipMestre, +idMestre, comm.protMsg(Comunicacao.CALC_RTT_MAX, ID));
		
	}
	/** 
	Envia mensagem do tipo Requisi��o de Rel�gio o mestre via Unicast. A mensagem cont�m o rel�gio atual do escravo.    
	*/
	public void enviaMsgRelogio() throws IOException{
		//Envia mensagem para o mestre para isso utiliza o Ip do mestre e seu Id (ID calcula a porta do mestre).
		//O conteudo da msg informa que o tipo � de REQ_RELOGIO e a ID desse processo escravo, e o rel�gio.
		uc.enviaMsg(ipMestre, +idMestre, comm.protMsg(Comunicacao.REQ_RELOGIO, ID, ""+convertHoursMillis(getHorario())));
	}
	/** 
	Faz a descriptografia de uma mensagem atrav�s de uma chave.
	@param msg - mensagem encriptografada.
	@param chave - chave de descriptografia.
	@return mensagem original.
	*/
	public String descriptografa(String msg){
		byte[] plainText = null;
		try {
			System.out.println("crypt: " + msg);
			cipher.init(Cipher.DECRYPT_MODE, chavePublicaMestre);
			plainText = cipher.doFinal(msg.getBytes());
		    System.out.println("plain: " + new String(plainText));
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		return new String(plainText);
	}
}