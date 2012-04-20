package tarefaRMI;
import java.rmi.*;

public interface InterfaceServidor extends Remote{
	void registrarInteresse(InterfaceCliente refCli) throws RemoteException;
}