package TarefaRMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Servidor extends UnicastRemoteObject implements InterfaceServidor{
	
	private static final long serialVersionUID = 1L;
	int porta = 1099;
	Registry n;
	int numCliente = 0;
	
	public Servidor() throws RemoteException{
		n = LocateRegistry.createRegistry(porta);
		n.rebind("Servidor", this);
	}
	
	public static void main(String []args) {
		
		Servidor s;
		try {
			s = new Servidor();
			
			while(true){
				Thread.sleep(5000);
				for(int i = 0; i<s.numCliente ; i++){
					((InterfaceCliente)s.n.lookup(""+i)).notifica("OI");
				}
				
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public void registrarInteresse(InterfaceCliente refCli)
			throws RemoteException {
		n.rebind(""+numCliente, refCli);
		numCliente ++;
		
		
	}
	

}
