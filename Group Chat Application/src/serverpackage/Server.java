package serverpackage;
import java.io.*;
import java.net.*;
import java.util.*;

import org.omg.PortableServer.ServantRetentionPolicyValue;
        
public class Server extends Thread{
	private final int serverPort;
	
	private ArrayList<ServerWorker> workerList = new ArrayList<>();
	
	public Server(int serverPort){
		this.serverPort = serverPort;
	}
	
	public List<ServerWorker> getWorkerList(){
		return workerList; 
	} 
	
	@Override
	public void run(){
		try {
			ServerSocket sSocket = new ServerSocket(serverPort);
			while(true){
				System.out.println("About to accept client connection");
				Socket cSocket = sSocket.accept();
				System.out.println("Accepted connection from "+cSocket);
				ServerWorker worker = new ServerWorker(this,cSocket);
				workerList.add(worker);
				worker.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeWorker(ServerWorker serverWorker) {
		workerList.remove(serverWorker);
	}
            
}