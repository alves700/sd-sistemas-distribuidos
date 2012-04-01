package Processo;

import java.io.IOException;
import java.net.DatagramPacket;

import Comunicação.Comunicacao;

public class Escravo extends Processo{
	
	//chavesDeCriptografia public do mestre
	private String chavePublicaMestre;
	
	//Variáveis de Eleicao.
	private final long tempoEleicao = 5000; // tempo da eleição demora 5 s
	private long tempoInicioEleicao = Long.MIN_VALUE; //Armazena o tempo em que a eleição é iniciada 
	private boolean eleicaoOcorrendo = false;
	
	//Variáveis do Hello.
	private final long tempoEsperaHello = 3000; // tempo de espera máximo aguardado pelo Hello do mestre.
	private long ultimoHelloRecebido; //Armazena o tempo em que o ultimo hello foi recebido.
	
	private int idNovoMestre = -1;
	
	
	public Escravo() throws IOException{
		System.out.println("Sou um Escravo");
	}
	
	/** 
	Inicia atributos de temporização
	*/
	public void iniciaVariaveis(){
		ultimoHelloRecebido = System.currentTimeMillis();
	}
	
	/** 
	Escravo envia sua ID caso ela seja maior que a ID armazenada por mensagens de eleições passadas recebidas pelo escravo.
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
	Método principal da thread. Entra em loop onde envia mensagens tanto para mestres quanto para outros escravos (no processo de eleição),
	verifica se há novas mensagens no buffer de entrada e verifica updates.  
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
	Caso o mestre não está respondendo envia mensagem de Eleição para outros processos. Os outros envios de mensagem para o mestre ocorrem na própria
	verificação de mensagens no buffer de entrada.
	*/
	public void envioDeMensagens() throws IOException{
		//Verifica quando foi a ultima vez que recebeu um hello, caso o tempo seja ultrapassado, inicia eleição.
		if(System.currentTimeMillis() > ultimoHelloRecebido + tempoEsperaHello){
			eleicao();
		}
		
	}
	/** 
	Verifica se há atualização do mestre após o término da eleição. Outras atualizações ocorrem na verificação do buffer de entrada. 
	*/
	public void update(){
		updateEleicaoMestre();
	}
	/** 
	Após o término da eleição verifica o ID do novo mestre. Se for o próprio processo, cria um objeto Mestre e inicia-o, logo após interrompe o próprio
	processo escravo.
	*/
	public void updateEleicaoMestre(){
		//Verifica se a eleicao ainda está ocorrendo, se estiver: eleicao acaba, e o mestre é consagrado.
		if(eleicaoOcorrendo && System.currentTimeMillis() > tempoInicioEleicao + tempoEleicao){
			eleicaoOcorrendo = false;
			idMestre = idNovoMestre;
			idNovoMestre = -1;
			
			if(idMestre != ID){
				System.out.println("NovoMestre ID " + idMestre);	
			}
			// Caso o esse escravo foi eleito como mestre, eçe instancia uma thread Mestre e finaliza seu processo como escravo.
			else{
				
				Mestre m;
				try {
					m = this.criaMestre();
					m.start();
					this.interrupt();
				} catch (IOException e2) {e2.printStackTrace();}
				catch (Throwable e) {e.printStackTrace();}
			}
			//Variáveis são reiniciadas.
			iniciaVariaveis();
		}
		
	}
	/** 
	Verifica o tipo de mensagem que chegou via Unicast ou Multicast, logo após faz ou não atualização. 
	Possíveis mensagens: Hello, Requisição de relógio, Ajuste de Relógio, Eleição, Cálculo do RTT máximo e de Envio de Chave Pública (pelo mestre). 
	Essas mensagens chegam a partir de outros escravos ou do próprio mestre.
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
				
				System.out.println("Debug" + msg[1]);
				String auxMsg[] = descriptografa(msg[1], chavePublicaMestre).split(" "); // descripografa e separa o ID da msg
				if ( Integer.parseInt(auxMsg[0]) == idMestre ){ //verifica se o ID está correto. Teste para ver se a descritografica ocorreu certo
					ajustaRelogio(auxMsg[1]); // ajuda o relógio;
				}else{
					System.out.println("Computador Mal intencionado na rede");
				}			
				break;
			case Comunicacao.ELEICAO:
				verificaEleicao(msg);
				break;
			case Comunicacao.CALC_RTT_MAX:
				getHorario(); //(Necessário, pois essa operação será feita quando o mestre pedir o horário, e ela demora 15ms no mínimo ¬¬).
				enviaMsgRTT();
				break;
			case Comunicacao.CHAVE_PUB:
				chavePublicaMestre = msg[Comunicacao.INDEX_MSG]; //recebe chave publica do mestre e seta em seu atributo para utilizar na autenticação posteriormente
		}
	}
	/** 
	Faz ajuste do relógio do escravo.
	@param msg - Mensagem contendo um valor em millisegundos de ajuste.    
	*/
	public void ajustaRelogio(String msg) throws IOException{
		long ajuste = Long.parseLong(msg);
		System.out.println("Novo ajuste feito de: "+ajuste+"ms do tempo atual");
		String horaAtual = getHorario(); 
		setHorario(converMillisHours(convertHoursMillis(horaAtual)+ajuste));
	}
	/** 
	Verifica mensagens que chegam de outros processos e compara o ID da mensagem com o maior ID armazenado no processo de eleição. 
	Faz a atualização do ID do novo mestre.
	@param msg - Mensagem contendo um ID.    
	*/
	public void verificaEleicao(String msg[]){
		//Verifica se a ID da mensagem de eleição que chegou é maior ou igual a sua. Se for
		//armazena o id do novo mestre como sendo o ID da mensagem que chegou.
		if(Integer.parseInt(msg[Comunicacao.INDEX_ID]) > idNovoMestre ){
			idNovoMestre = Integer.parseInt(msg[Comunicacao.INDEX_ID]);
		}
		
	}
	/** 
	Envia mensagem do tipo Calculo do RTT máximo para o mestre via Unicast.    
	*/
	public void enviaMsgRTT() throws IOException{
		//Envia mensagem para o mestre para isso utiliza o Ip do mestre e seu Id (ID calcula a porta do mestre).
		//O conteudo da msg informa que o tipo é de CALC RTT MAX e a ID desse processo escravo.
		uc.enviaMsg(ipMestre, +idMestre, comm.protMsg(Comunicacao.CALC_RTT_MAX, ID));
		
	}
	/** 
	Envia mensagem do tipo Requisição de Relógio o mestre via Unicast. A mensagem contém o relógio atual do escravo.    
	*/
	public void enviaMsgRelogio() throws IOException{
		//Envia mensagem para o mestre para isso utiliza o Ip do mestre e seu Id (ID calcula a porta do mestre).
		//O conteudo da msg informa que o tipo é de REQ_RELOGIO e a ID desse processo escravo, e o relógio.
		uc.enviaMsg(ipMestre, +idMestre, comm.protMsg(Comunicacao.REQ_RELOGIO, ID, ""+convertHoursMillis(getHorario())));
	}
	/** 
	Faz a descriptografia de uma mensagem através de uma chave.
	@param msg - mensagem encriptografada.
	@param chave - chave de descriptografia.
	@return mensagem original.
	*/
	public String descriptografa(String msg, String chave){
		return msg;
	}
}