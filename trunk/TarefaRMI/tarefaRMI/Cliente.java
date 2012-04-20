package tarefaRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Cliente extends UnicastRemoteObject implements InterfaceCliente{
	
	protected Cliente() throws RemoteException {
	}
	public static void main(){
		
	}

	@Override
	public void notifica(String msg) throws RemoteException {
		System.out.println(msg);
		
	}
	
}
