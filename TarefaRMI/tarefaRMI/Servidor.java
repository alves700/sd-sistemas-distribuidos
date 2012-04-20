package tarefaRMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Servidor extends UnicastRemoteObject implements InterfaceServidor{
	private int porta = 1099;
	Registry n;
	private int numCliente = 0;
	
	public Servidor() throws RemoteException{
		n = LocateRegistry.createRegistry(porta);
		n.rebind("Servidor", this);
	}
	
	public static void main(){
		
	}

	@Override
	public void registrarInteresse(InterfaceCliente refCli)
			throws RemoteException {
		n.rebind(""+numCliente, refCli);
		numCliente ++;
		
		
	}

}
