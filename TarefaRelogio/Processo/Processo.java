package Processo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;

import javax.crypto.Cipher;

import Comunicação.*;

/** 
Classe que possui o main, e além disso possui métodos do Processo. O processo é responsável pelo inicio de comunicações, 
instancia mestre ou escravo, processamento de mensagens e ajuste de horários. 
*/
public class Processo extends Thread{
	
	/** Objeto que aplica algoritmos de criptografia em uma mensagem dado uma chave.*/
	protected Cipher cipher;
	
	/** Comunicação do processo.*/
	protected Comunicacao comm;;
	/** Comunicação multicast.*/
	protected Multicast mc;
	/** Comunicação unicast.*/
	protected Unicast uc;
	
	/** ID do mestre.*/
	protected int idMestre = -1;
	/** IP do mestre.*/
	protected String ipMestre;
	/** ID do processo.*/
	protected int ID;
	
	/** 
	Inicia a comunicação instanciando um objeto da classe Comunicação.java. ID é iniciada nesse método.
	*/
	public void iniciaComunicacao() throws IOException{
		ID = (int) (Math.random()*40000)+1;
		comm = new Comunicacao(ID);
		mc = comm.getMulticast();
		uc = comm.getUnicast();
	}
	/** 
	Cria um objeto da classe Mestre repassando os atributos como: ID, Comunicações Unicast e Multicast para esse objeto.
	@return Objeto da classe Mestre.
	*/
	public Mestre criaMestre() throws IOException{
		Mestre m = new Mestre();
		m.ID = this.ID;
		m.comm = this.comm;
		m.mc = this.mc;
		m.uc = this.uc;
		return m;
	}
	/** 
	Cria um objeto da classe Escravo repassando os atributos como: ID, ID do mestre, IP do mestre , Comunicações Unicast e Multicast para esse objeto.
	@return Objeto da classe Escravo.
	*/
	public Escravo criaEscravo() throws IOException{
		Escravo e = new Escravo();
		e.ID = this.ID;
		e.comm = this.comm;
		e.mc = this.mc;
		e.uc = this.uc;
		e.idMestre = this.idMestre;
		e.ipMestre = this.ipMestre;
		return e;
	}
	/** 
	Executa o método main, um processo é instanciado e iniciado.
	*/
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
	}
	/** 
	Inicia o processo, depois verifica os processos vizinhos e verifica quem possui maior ID. O processo que possui maior ID é o mestre
	os outros são escravos. Caso esse processo seja mestre, instancia e inicia um objeto Mestre. Caso for escravo instancia e inicia um Objeto escravo.  
	*/
	public void iniciaProcesso() throws InterruptedException, IOException{

		comm.reconheceOutrosProcessos();
		System.out.println("Termino do Reconhecimento");
		
		
		//Verifico qual é o maior ID dos contatos.
		System.out.println("Minha Lista De Contatos:");
		idMestre = ID;
		ipMestre = comm.getIP();
	 	for ( String i[] : comm.getContatos()){
	   		System.out.println("ip: "+ i[0] + "  id: "+ i[1]);
	   		int x = Integer.parseInt(i[1]);
	   		if(x > idMestre){
	   			ipMestre = i[0];
	   			idMestre = x;
	   		}
	   	}
	 	System.out.println("");
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
	/** 
	Verifica se há mensagens no buffer de entrada de ambas conexões (Multicast e Unicast).
	*/
	public void verficaBufferEntrada() throws IOException{
		while (mc.existeMsg()){
			processaMensagem(mc.getDatagram());
		}
		while (uc.existeMsg()){
			processaMensagem(uc.getDatagram());
		}
	}
	/** 
	Faz o processamento da mensagem recebida no buffer de entrada.
	@param dp DatagramPacket que contem o conteúdo e IP da mensagem recebida.
	*/
	public void processaMensagem(DatagramPacket dp) throws IOException{
	}
	/** 
	Faz a conversão de um horário no formato String para millisegundos.
	@param horario String que possui o formato hh:mm:ss,xx onde xx indica centésimos de segundo.
	@return o horário em millisegundos.
	*/
	public long convertHoursMillis(String horario){
		 String [] c = horario.split(":");
		 
         long mills = Long.parseLong(c[0])*60*60*1000; //Horas convertidos em millis
         mills +=  Long.parseLong(c[1])*60*1000; //Minutos convertidos em millis
         String [] c2= c[2].split(","); 
         mills += Long.parseLong(c2[0])*1000; //Segundos convertidos em millis
         mills += Long.parseLong(c2[1])*10; // millis
         
         return mills;
	}
	/** 
	Faz a conversão de um horário em millisegundos para um horário no formato String.
	@param mills o horário em millisegundos.
	@return String que possui o formato hh:mm:ss,xx onde xx indica centésimos de segundo.
	*/
	public String converMillisHours(long mills){
  
        String format = String.format("%%0%dd", 2);
        String milliseconds = String.format(format, (mills % 1000)/10);
        mills = mills/ 1000;  
        String seconds = String.format(format, mills % 60);  
        String minutes = String.format(format, (mills % 3600) / 60);  
        String hours = String.format(format, mills/ 3600);  
        String time =  hours + ":" + minutes + ":" + seconds+","+milliseconds;
        
        return time;
	}
	/** 
	Retorna o horário atual utilizando o comando time do cmd.
	@return String que possui o formato hh:mm:ss,xx onde xx indica centésimos de segundo.
	*/
	public String getHorario() throws IOException{
		 String[] command =  new String[3];
         command[0] = "cmd";
         command[1] = "/C";
         command[2] = "time";//path of the compiler

         Process p = Runtime.getRuntime().exec(command);
         BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String s = stdInput.readLine();
         
         int i = s.indexOf(":");
         s = s.substring(i+2);
         return s;
        
	}
	/** 
	Muda o horário atual utilizando o comando time do cmd.
	@param horario String que possui o formato hh:mm:ss,xx onde xx indica centésimos de segundo.
	*/
	public void setHorario(String horario) throws IOException{
		String[] command =  new String[3];
        command[0] = "cmd";
        command[1] = "/C";
        command[2] = "time "+horario;//path of the compiler

        Runtime.getRuntime().exec(command);
	}
}
