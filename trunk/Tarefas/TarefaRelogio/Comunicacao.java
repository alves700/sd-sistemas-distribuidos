package TarefaRelogio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

/** 
Classe responsável pela comunicação dos processos. Instancia tipos de conexão tanto multicast como unicast.
*/  
public class Comunicacao {
	
	
	/** Index do conteúdo do pacote recebido/enviado.*/  
	public final static int INDEX_MSG = 2;
	/** Index do tipo de mensagem do pacote recebido/enviado.*/  
	public final static int INDEX_TIPO = 0;
	/** Index do IP para a lista de contatos.*/  
	public final static int INDEX_IP = 0;
	/** Index do ID para pacote recebido/enviado ou lista de contato.*/
	public final static int INDEX_ID = 1;
	
	/**Tipo de pacote trocado entre processos.*/
	public final static int HELLO = 0;
	/**Tipo de pacote trocado entre processos.*/
	public final static int	REQ_RELOGIO = 1;
	/**Tipo de pacote trocado entre processos.*/
	public final static int	AJUSTE_RELOGIO = 2;
	/**Tipo de pacote trocado entre processos.*/
	public final static int	RECONHECIMENTO = 3;
	/**Tipo de pacote trocado entre processos.*/
	public final static int	ELEICAO = 4;
	/**Tipo de pacote trocado entre processos.*/
	public final static int	CALC_RTT_MAX = 5;
	/**Tipo de pacote trocado entre processos.*/
	public final static int CHAVE_PUB = 6;

	/** Contatos que o processo conhece.*/
	private ArrayList<String[]> contatos;
	
	/** IP do computador.*/
	private String IP;
	/** ID do processo.*/
	private int ID;
	
	/** Tempo de duração em milisegundos do reconhecimento entre processos.*/
	private final long tempoReconhecimento = 10000;
	
	/** Comunicação Unicast (UDP)*/
	private Unicast uc;
	/** Comunicação Multicast (UDP)*/
	private Multicast mc;
	
	/** 
	Objetos das classes Unicast e Multicast são instanciados e iniciados.
	@param id ID do processo que instanciou objeto dessa classe.
	*/ 
	public Comunicacao(int id) throws IOException{
		
		contatos = new ArrayList<String[]>();
		
		//Armazena seu próprio IP da rede.
		
		InetAddress address = InetAddress.getLocalHost();
		IP = address.getHostAddress();
		 
		mc = new Multicast();
		mc.joinMulticast();
		mc.start();

		uc = new Unicast(id);
		uc.start();
		
		ID = id;
		System.out.println("IP da máquina: " + IP +"    ID da máquina: " + id);
	}
	/** 
	@return Objeto ArrayList contendo a lista de contatos.
	*/
	public ArrayList<String[]> getContatos() {
		return contatos;
	}
	
	/** 
	@return String contendo o IP do computador.
	*/
	public String getIP(){
		return IP;
	}
	
	/** 
	@return Objeto Multicast instanciado por essa classe.
	*/
	public Multicast getMulticast(){
		return mc;
	}
	
	/** 
	@return Objeto Unicast instanciado por essa classe.
	*/
	public Unicast getUnicast(){
		return uc;
	}

	/** 
	Retorna mensagens de comunicação entre processos.
	@param tipo tipo da mensagem.
	@param ID ID do processo que enviou a mensagem.
	@param msg conteúdo da mensagem.
	@return String contendo a mensagem resultante.
	*/ 
	public String protMsg(int tipo, int ID, String msg){
		return ""+ tipo + " " + ID + " " + msg;
	}
	
	/** 
	Retorna mensagens de comunicação entre processos.
	@param tipo tipo da mensagem.
	@param ID ID do processo que enviou a mensagem.
	@return String contendo a mensagem resultante.
	*/
	public String protMsg(int tipo, int ID){
		return ""+ tipo + " " + ID;
	}
	/** 
	Reconhece os processos vizinhos (processos inciados em outros computadores situados na mesma rede) e armazena-os.
	em uma lista de contatos.
	*/ 
	public void reconheceOutrosProcessos() throws InterruptedException, IOException{
		long t1 = System.currentTimeMillis();
		long t2 = System.currentTimeMillis();
		
		mc.enviaMsg(protMsg(RECONHECIMENTO,ID));
		while(t1 + tempoReconhecimento >= t2){
			if ( mc.existeMsg()){
				
				DatagramPacket dp = mc.getDatagram();// pega datagrama do buffer de entrada do socket
				String msg = mc.getMsg(dp);
				
				String[] contato = msg.split(" ");
				
				if ( !contato[0].equals(""+ RECONHECIMENTO)) //se naum for msg de reconhecimento
					continue;//vai para a proxima iteração
				
				contato[INDEX_IP] = mc.getIP(dp); // coloca o IP no primeiro index do array
				
				
				boolean processoExistente = false;
                // Verifica se o IP ja existe na sua lista de contatos.
				int i;
                for(i = 0 ; i<contatos.size(); i++){
                        if(contato[INDEX_ID].equals(contatos.get(i)[INDEX_ID])){
                                processoExistente = true;
                        }
                }
                //Se processo não existir na tabela de processos, adiciona-o na lista e envia sua ID para esse processo adicioná-lo, dá mais
                //tempoDeReconhecimento para o término do reconhecimento entre processos
               if ( !processoExistente && Integer.parseInt(contato[INDEX_ID]) != ID){
					contatos.add(contato);
					//System.out.println(contato[INDEX_IP] + " "+ contato[INDEX_MSG]);
					mc.enviaMsg(protMsg(RECONHECIMENTO,ID));
					t1 = System.currentTimeMillis();
				}
				
			}
			
			Thread.sleep(10);
			
			t2 = System.currentTimeMillis();
		}
	}
}