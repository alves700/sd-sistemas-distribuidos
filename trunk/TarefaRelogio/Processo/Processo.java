package Processo;

import java.io.IOException;
import java.net.DatagramPacket;

import Comunicação.*;

public class Processo extends Thread{
	
	protected Comunicacao comm;;
	protected Multicast mc;
	protected Unicast uc;
	
	protected int idMestre = -1;
	protected int ID;
	
	public Processo(){
	}
	public void iniciaComunicacao() throws IOException{
		ID = (int) (Math.random()*40000)+1;
		comm = new Comunicacao(ID);
		mc = comm.getMulticast();
		uc = comm.getUnicast();
	}
	public Mestre criaMestre() throws IOException{
		Mestre m = new Mestre();
		m.ID = this.ID;
		m.comm = this.comm;
		m.mc = this.mc;
		m.uc = this.uc;
		return m;
	}
	public Escravo criaEscravo() throws IOException{
		Escravo e = new Escravo();
		e.ID = this.ID;
		e.comm = this.comm;
		e.mc = this.mc;
		e.uc = this.uc;
		e.idMestre = this.idMestre;
		return e;
	}
	
	public static void main(String [] args) {
		Processo p;
		try {
			p = new Processo();
			p.iniciaComunicacao();
			p.iniciaProcesso();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
		}
		
	}
	public void iniciaProcesso() throws InterruptedException, IOException{
;
		comm.reconheceOutrosProcessos(ID);
		System.out.println("Termino do Reconhecimento");
		
		
		//Verifico qual é o maior ID dos contatos. 
		idMestre = ID;
	 	for ( String i[] : comm.getContatos()){
	   		System.out.println("ip: "+ i[0] + "  id: "+ i[1]);
	   		int x = Integer.parseInt(i[1]);
	   		if(x > idMestre){
	   			idMestre = x;
	   		}
	   	}
	 	//Verifico se esse ID é do processo em questão, se for ele entra em mestreMode.
	 	if(idMestre == ID){
	 		Mestre m = this.criaMestre();
	 		m.start();
	 		
	 	}
	 	else{
	 		Escravo e = this.criaEscravo();
	 		e.start();
	 	}
	}
	public void verficaBufferEntrada(){
		if (mc.existeMsg()){
			processaMensagem(mc.getDatagram());
		}
		if (uc.existeMsg()){
			processaMensagem(uc.getDatagram());
		}
	}
	public void processaMensagem(DatagramPacket dp){
	}
}
