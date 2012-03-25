package Processo;

import java.net.DatagramPacket;

import Comunica��o.*;

public class Processo{
	
	protected Comunicacao comm;;
	protected Multicast mc;
	protected Unicast uc;
	
	protected int idMestre = -1;
	protected int ID;
	
	public Processo(){
		comm = new Comunicacao(ID);
		mc = comm.getMulticast();
		uc = comm.getUnicast();
	}
	public static void main(String [] args){
		Processo p = new Processo();
		p.iniciaProcesso();
	}
	public void iniciaProcesso(){
		ID = (int) (Math.random()*40000)+1;
		comm.reconheceOutrosProcessos(ID);
		System.out.println("Termino do Reconhecimento");
		
		
		//Verifico qual � o maior ID dos contatos. 
	 	for ( String i[] : comm.getContatos()){
	   		System.out.println("ip: "+ i[0] + "  id: "+ i[1]);
	   		int x = Integer.parseInt(i[1]);
	   		if(x > idMestre){
	   			idMestre = x;
	   		}
	   	}
	 	//Verifico se esse ID � do processo em quest�o, se for ele entra em mestreMode.
	 	if(idMestre == ID){
	 		Mestre m = new Mestre();
	 		Thread t = new Thread(m);
	 		t.start();
	 		
	 	}
	 	else{
	 		Escravo e = new Escravo(ID,idMestre);
	 		Thread t = new Thread(e);
	 		t.start();
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
