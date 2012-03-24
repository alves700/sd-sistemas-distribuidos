package Processo;

import Comunicação.*;

public class Processo extends Thread {
	
	Comunicacao comm = new Comunicacao();
	
	private int idMestre;
	private int ID;
	
	public static void main(String [] args){
		Processo p = new Processo();
		p.start();
	}
	
	public void run(){
		ID = (int) (Math.random()*10);
		idMestre = 10;
		comm.reconheceOutrosProcessos(ID);
		System.out.println("Termino do Reconhecimento");
		
		
	 	for ( String i[] : comm.getContatos()){
	   		System.out.println("ip: "+ i[0] + "  id: "+ i[1]);
	   	}
	 	
	 	
		while(true){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
