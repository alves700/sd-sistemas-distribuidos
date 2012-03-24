package Processo;

import Comunicação.*;

public class Processo extends Thread {
	
	Comunicacao comm = new Comunicacao();
	
	private boolean modoMestre;
	private int ID;
	public Processo(){
		// Verificar se vamos fazer assim.
		ID = (int) (Math.random()*10);
		modoMestre = false;
	}
	public void run(){
		comm.reconheceOutrosProcessos(ID);
		while(true){
			
		}
	}

}
