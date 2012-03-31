package Processo;

import java.io.IOException;
import java.net.DatagramPacket;

import Comunica��o.Comunicacao;

public class Escravo extends Processo implements Runnable {
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
	}
	
	
	public void iniciaVariaveis(){
		ultimoHelloRecebido = System.currentTimeMillis();
	}
	
	//M�todo de eleicao, escravo envia sua ID caso ela seja maior q a ID armazenada por mensagens de eleicoes passadas.
	public void eleicao() throws IOException{
		if(!eleicaoOcorrendo){
			tempoInicioEleicao = System.currentTimeMillis();
			eleicaoOcorrendo = true;
			if(ID > idNovoMestre){
				mc.enviaMsg(comm.protMsg(comm.ELEICAO, ID));
			}
		}
	}
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
	public void envioDeMensagens() throws IOException{
		//Verifica quando foi a ultima vez que recebeu um hello, caso o tempo seja ultrapassado, inicia elei��o.
		if(System.currentTimeMillis() > ultimoHelloRecebido + tempoEsperaHello){
				eleicao();
		}
		
	}
	public void update(){
		updateEleicaoMestre();
	}
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
	//Verifica as os tipos de mensagem que chegam.
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
				ajustaRelogio(msg);
				break;
			case Comunicacao.RECONHECIMENTO:
				break;
			case Comunicacao.ELEICAO:
				verificaEleicao(msg);
				break;
			case Comunicacao.CALC_RTT_MAX:
				getHorario(); //(Necess�rio, pois essa opera��o ser� feita quando o mestre pedir o hor�rio, e ela demora 15ms no m�nimo ��).
				enviaMsgRTT();
				break;
		}
	}
	public void ajustaRelogio(String [] msg) throws IOException{
		long ajuste = Long.parseLong(msg[Comunicacao.INDEX_MSG]);
		System.out.println("Novo ajuste feito de: "+ajuste+"ms do tempo atual");
		String horaAtual = getHorario(); 
		setHorario(converMillisHours(convertHoursMillis(horaAtual)+ajuste));
	}
	public void verificaEleicao(String msg[]){
		//Verifica se a ID da mensagem de elei��o que chegou � maior ou igual a sua. Se for
		//armazena o id do novo mestre como sendo o ID da mensagem que chegou.
		if(Integer.parseInt(msg[Comunicacao.INDEX_ID]) > idNovoMestre ){
			idNovoMestre = Integer.parseInt(msg[Comunicacao.INDEX_ID]);
		}
		
	}
	public void enviaMsgRTT() throws IOException{
		//Envia mensagem para o mestre para isso utiliza o Ip do mestre e seu Id (ID calcula a porta do mestre).
		//O conteudo da msg informa que o tipo � de CALC RTT MAX e a ID desse processo escravo.
		uc.enviaMsg(ipMestre, +idMestre, comm.protMsg(Comunicacao.CALC_RTT_MAX, ID));
		
	}
	public void enviaMsgRelogio() throws IOException{
		//Envia mensagem para o mestre para isso utiliza o Ip do mestre e seu Id (ID calcula a porta do mestre).
		//O conteudo da msg informa que o tipo � de REQ_RELOGIO e a ID desse processo escravo, e o rel�gio.
		uc.enviaMsg(ipMestre, +idMestre, comm.protMsg(Comunicacao.REQ_RELOGIO, ID, ""+convertHoursMillis(getHorario())));
	}
}
