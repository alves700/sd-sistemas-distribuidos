package Processo;

import java.net.DatagramPacket;

import Comunicação.Comunicacao;

public class Escravo extends Processo implements Runnable {
	//Variáveis de Eleicao.
	private final long tempoEleicao = 5000;
	private long tempoInicioEleicao = Long.MIN_VALUE;
	private boolean eleicaoOcorrendo = false;
	
	//Variáveis do Hello.
	private final long tempoEsperaHello = 3000;
	private long ultimoHelloRecebido;
	
	
	
	public void iniciaVariaveis(){
		ultimoHelloRecebido = System.currentTimeMillis();
	}
	public void eleicao(){
		if(!eleicaoOcorrendo){
			tempoInicioEleicao = System.currentTimeMillis();
			eleicaoOcorrendo = true;
		}
		if(ID >= idNovoMestre){
			mc.enviaMsg(comm.protMsg(comm.ELEICAO, ID));
		}
	}
	@Override
	public void run() {
		iniciaVariaveis();
		while(true){
			//Verifica se a eleicao ainda está ocorrendo, se estiver: eleicao acaba, e o mestre é consagrado.
			if(eleicaoOcorrendo && System.currentTimeMillis() > tempoInicioEleicao + tempoEleicao){
				eleicaoOcorrendo = false;
				idMestre = idNovoMestre;
				tempoInicioEleicao = Long.MIN_VALUE;
				
				System.out.println("NovoMestre ID" + idMestre);
				
				if(idNovoMestre == ID){
					
					Mestre m = new Mestre();
			 		Thread t = new Thread(m);
			 		t.start();
			 		try {
						this.finalize();
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else if(System.currentTimeMillis() > ultimoHelloRecebido + tempoEsperaHello){
				eleicao();
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

			case Comunicacao.HELLO:
				ultimoHelloRecebido = System.currentTimeMillis();
				break;
			case Comunicacao.REQ_RELOGIO:
				break;
			case Comunicacao.RELOGIO:
				break;
			case Comunicacao.RECONHECIMENTO:
				break;
			case Comunicacao.ELEICAO:
				if(Integer.parseInt(msg[Comunicacao.INDEX_MSG]) >= ID){
					idNovoMestre = Integer.parseInt(msg[Comunicacao.INDEX_MSG]);
				}
				if(!eleicaoOcorrendo){
					tempoInicioEleicao = System.currentTimeMillis();
					eleicaoOcorrendo = true;
				}
				break;
			case Comunicacao.CALC_RTT_MAX:
				break;
			
		}
	}
}
