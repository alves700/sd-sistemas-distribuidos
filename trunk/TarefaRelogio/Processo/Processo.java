package Processo;

import Comunicação.*;

public class Processo extends Thread {
	
	Comunicacao comm = new Comunicacao();
	
	private boolean modoMestre;
	private int ID;
	
	public static void main(String [] args){
		Processo p = new Processo();
		p.start();
	}
	
	public void run(){
		ID = (int) (Math.random()*10);
		modoMestre = false;
		comm.reconheceOutrosProcessos(ID);
		System.out.println("Termino da Eleição");
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
