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
	
	private int ID;
	private int idMestre;
	private int idNovoMestre;

	public Escravo(int ID, int idMestre) throws IOException{
		this.ID = ID;
		this.idMestre = super.idMestre;
		idNovoMestre = -1;
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
		while(true){
			//Verifica se a eleicao ainda est� ocorrendo, se estiver: eleicao acaba, e o mestre � consagrado.
			if(eleicaoOcorrendo && System.currentTimeMillis() > tempoInicioEleicao + tempoEleicao){
				eleicaoOcorrendo = false;
				idMestre = idNovoMestre;
				idNovoMestre = -1;
				tempoInicioEleicao = Long.MIN_VALUE;
				
				System.out.println("NovoMestre ID " + idMestre);
				// Caso o esse escravo foi eleito como mestre, e�e instancia uma thread Mestre e finaliza seu processo como escravo.
				if(idMestre == ID){
					
					Mestre m;
					try {
						m = new Mestre();
						m.start();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			 		
			 		try {
						this.finalize();
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//Vari�veis s�o reiniciadas.
				iniciaVariaveis();
			}
			//Verifica quando foi a ultima vez que recebeu um hello, caso o tempo seja ultrapassado, inicia elei��o.
			else if(System.currentTimeMillis() > ultimoHelloRecebido + tempoEsperaHello){
				try {
					eleicao();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	//Verifica as os tipos de mensagem que chegam.
	public void processaMensagem(DatagramPacket dp){
		String x = mc.getMsg(dp);
		String msg[] = x.split(" ");
		
		switch( Integer.parseInt(msg[Comunicacao.INDEX_TIPO]) ){
			//Reconfigura o ultimo hello recebido.
			case Comunicacao.HELLO:
				ultimoHelloRecebido = System.currentTimeMillis();
				break;
			case Comunicacao.REQ_RELOGIO:
				break;
			case Comunicacao.RELOGIO:
				break;
			case Comunicacao.RECONHECIMENTO:
				break;
			//Verifica se a ID da mensagem de elei��o que chegou � maior ou igual a sua. Se for
			//armazena o id do novo mestre como sendo o ID da mensagem que chegou.
			case Comunicacao.ELEICAO:
				if(Integer.parseInt(msg[Comunicacao.INDEX_MSG]) > idNovoMestre ){
					idNovoMestre = Integer.parseInt(msg[Comunicacao.INDEX_MSG]);
				}
				break;
			case Comunicacao.CALC_RTT_MAX:
				break;
			
		}
	}
}
