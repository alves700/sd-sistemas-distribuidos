package Processo;

import Comunica��o.*;

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
		System.out.println("Termino da Elei��o");
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
