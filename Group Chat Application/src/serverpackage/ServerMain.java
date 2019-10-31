package serverpackage;

import java.io.*;
import java.net.*;
import java.util.Date;


public class ServerMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int port = 3410;
		Server server = new Server(port);
		server.start();
		
		
	}
	

}
