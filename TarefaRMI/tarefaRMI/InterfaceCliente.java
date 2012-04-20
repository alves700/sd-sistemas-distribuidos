package tarefaRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceCliente extends Remote{
	
	void notifica(String msg) throws RemoteException;
	
}
