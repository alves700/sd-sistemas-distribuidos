package tarefaRMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Cliente extends UnicastRemoteObject implements InterfaceCliente{
	
	InterfaceServidor s;
	Registry n;
	protected Cliente() throws RemoteException, NotBoundException {
		n = LocateRegistry.getRegistry();
		s = (InterfaceServidor) n.lookup("Servidor");
		s.registrarInteresse(this);
	}
	public static void main(String [] args) {
		try {
			Cliente c = new Cliente();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void notifica(String msg) throws RemoteException {
		System.out.println(msg);
		
	}
	
}
