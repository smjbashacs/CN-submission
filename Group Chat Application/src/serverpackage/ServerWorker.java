
package serverpackage;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class ServerWorker extends Thread{
	
	private final Socket cSocket;
	private final Server server;
	private String uname = null;
	
	private OutputStream opStream;
	private HashSet<String> topicSet = new HashSet<>();
	
	
	public ServerWorker(Server server, Socket cSocket){
		this.server = server;
		this.cSocket = cSocket;
	}
	

	@Override
	public void run(){
		try {
			handleClientSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void handleClientSocket() throws IOException, InterruptedException{
		InputStream ipStream = cSocket.getInputStream();
		this.opStream = cSocket.getOutputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(ipStream));
		
		String line;
		
		while( (line= reader.readLine())!= null){
			//System.out.println("Line:"+line);
			String[] tokens = line.split(" ");
			if(tokens!=null && tokens.length>0){
				String cmd = tokens[0];
				//System.out.println("cmd:"+cmd);
				if(("quit".equalsIgnoreCase(cmd)) || "logoff".equalsIgnoreCase(cmd)){
					handleLogoff();
					break;
				}
				else if("login".equalsIgnoreCase(cmd)){
					handleLogin(opStream,tokens);
				}
				else if("register".equalsIgnoreCase(cmd)){
					handleRegister(opStream,tokens);
				}
				else if("msg".equalsIgnoreCase(cmd)){
					String[] tokenMsg = line.split(" ", 3);
					handleMsg(tokenMsg);
				}
				else if("join".equalsIgnoreCase(cmd)){
					handleJoin(tokens);
				}
				else if("getgroups".equalsIgnoreCase(cmd)){
					handleGetGroups(opStream,tokens);
				}
				else if("getallgroups".equalsIgnoreCase(cmd)){
					handleGetAllGroups(opStream,tokens);
				}
				else if("addtogroup".equalsIgnoreCase(cmd)){
					handleAddToGroup(opStream,tokens);
				}
				else if("unjoinGroup".equalsIgnoreCase(cmd)){
					handleUnjoinGroup(opStream,tokens);
				}
				else if("msglog".equalsIgnoreCase(cmd)){
					handleMsgLog(tokens);
				}
				else{
					String msg = "Invalid command:" + cmd + "\n";
					opStream.write(msg.getBytes());
				}	
			}
			
		}
		cSocket.close();
	}
	
	
	
	private void handleUnjoinGroup(OutputStream opStream2, String[] tokens) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String group = tokens[1];
		if(!checkIfFieldPresent("./src/serverpackage/DataSource/groups.txt", group, 1)){
			String msg = "Invalid group\n";
			opStream.write(msg.getBytes());
		}
		else{
			String groupPath = "./src/serverpackage/DataSource/"+group+"/members.txt";
			if(checkIfFieldPresent(groupPath,uname,1)){
				String entry = uname;
				if(RemoveEntryToFile(group,entry)){
					String msg = "Removed from "+group+" successfully\n";
					opStream.write(msg.getBytes());
				}
				else{
					String msg = "Unable to add to group due to server exception\n";
					opStream.write(msg.getBytes());
				}
				
			}
			else{
				String msg = "User is not part of group to unjoin\n";
				opStream.write(msg.getBytes());
			}		
			
		}
		
	}


	private void handleMsgLog(String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		String group = tokens[1];
		System.out.println("group:"+group);
		String path = "./src/serverpackage/DataSource/"+group+"/messages.txt";
		
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line;
		String msg="";
		//opStream.write("in msglog func\n".getBytes());
		while((line= reader.readLine())!=null){
			//System.out.println(line);
			msg = line+"\n";
			opStream.write(msg.getBytes());
		}
		
	}


	private void handleAddToGroup(OutputStream opStream, String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		String group = tokens[1];
		if(!checkIfFieldPresent("./src/serverpackage/DataSource/groups.txt", group, 1)){
			String msg = "Invalid group\n";
			opStream.write(msg.getBytes());
		}
		else{
			String groupPath = "./src/serverpackage/DataSource/"+group+"/members.txt";
			if(checkIfFieldPresent(groupPath,uname,1)){
				String msg = "User already part of group\n";
				opStream.write(msg.getBytes());
			}
			else{
				String entry = uname;
				if(AppendEntryToFile(groupPath,entry)){
					String msg = "Added to group "+group+" successfully\n";
					opStream.write(msg.getBytes());
				}
				else{
					String msg = "Unable to add to group due to server exception\n";
					opStream.write(msg.getBytes());
				}
			}
		}
		
		
		
	}


	private void handleGetAllGroups(OutputStream opStream2, String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		String path = "./src/serverpackage/DataSource/groups.txt";
		BufferedReader reader = new BufferedReader(new FileReader(path));
		reader.readLine();
		String line;
		String[] contents;
		String msg;
		String groupList = "";
		boolean flag = false;
		while((line= reader.readLine())!=null){
			//System.out.println(line);
			contents = line.split(",");
			//System.out.println("group contents"+contents[0]);
			groupList = groupList+";"+contents[0];
			
		}
		//System.out.println("grouplist:"+groupList);
		msg = groupList+"\n";
		//System.out.println("msg in handlegetallgroups:"+msg);
		opStream.write(msg.getBytes());

		
	}


	private void handleGetGroups(OutputStream opStream,String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		//System.out.println("in hanglegetgroups");
		String path = "./src/serverpackage/DataSource/groups.txt";
		BufferedReader reader = new BufferedReader(new FileReader(path));
		reader.readLine();
		String line;
		String[] contents;
		String msg;
		String groupList = "";
		boolean flag = false;
		while((line= reader.readLine())!=null){
			//System.out.println(line);
			contents = line.split(",");
			//System.out.println("group contents"+contents[0]);
			String groupPath = "./src/serverpackage/DataSource/"+contents[0]+"/members.txt";
			if(checkIfFieldPresent(groupPath,uname,1)){
				groupList = groupList+";"+contents[0];
			}
			
		}
		if(groupList.equalsIgnoreCase("")){
			msg = "User not part of any group\n";
		}
		else{
			msg = groupList+"\n";
		}
		//System.out.println("grouplist:"+groupList);
		
		//System.out.println("msg in handlegetgroups:"+msg);
		opStream.write(msg.getBytes());
			
	}


	private void handleRegister(OutputStream opStream, String[] tokens) throws IOException {
		// TODO Auto-generated method stub
		if(tokens.length == 3){
			String uname = tokens[1];
			String password = tokens[2];
			boolean userRegistered = checkIfFieldPresent("./src/serverpackage/DataSource/users.txt",uname,1);
			if(userRegistered){
				String msg = "Username already registered\n";
				opStream.write(msg.getBytes());
			}
			else{
				String entry = uname+","+password;
				if(AppendEntryToFile("./src/serverpackage/DataSource/users.txt",entry)){
					String msg = "Registration successful\n";
					opStream.write(msg.getBytes());
				}
				else{
					String msg = "Registration failed\n";
					opStream.write(msg.getBytes());
				}
			}
			
		}
	}
	
	
	


	private boolean checkIfFieldPresent(String path, String fieldValue, int fieldNum) throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(path));
		reader.readLine();
		String line;
		String[] contents;
		boolean flag = false;
		while((line= reader.readLine())!=null){
			//System.out.println(line);
			contents = line.split(",");
			if(contents[fieldNum-1].equalsIgnoreCase(fieldValue)){
				flag = true;
				break;
			}
			
		}	
		
		return flag;
	}



	private void handleLogin(OutputStream opStream,String[] tokens) throws IOException{
		if(tokens.length == 3){
			String uname = tokens[1];
			String password = tokens[2];
			boolean userFound = checkUserCredentials("./src/serverpackage/DataSource/users.txt",uname,1,password,2);
			
			if(userFound){
				String msg = "Login successful\n";
				opStream.write(msg.getBytes());
				
				this.uname = uname;
				System.out.println("User logged in successfully:"+uname);
				
				List<ServerWorker> workerList =  server.getWorkerList();
				
			}
			else{
				String msg = "Login failed\n";
				opStream.write(msg.getBytes());
				System.err.println("login failed for :"+uname);
			}
		}
	}	
	
	

	private void handleJoin(String[] tokens) throws IOException {
		String group = tokens[1];
		boolean flagGroupPresent = false;
		flagGroupPresent = checkIfFieldPresent("./src/serverpackage/DataSource/groups.txt", group, 1);
		if(!flagGroupPresent){
			String msg = group+" is invalid option\n";
			opStream.write(msg.getBytes());
		}
		
		if(tokens.length > 1 && flagGroupPresent){
			
			String path = "./src/serverpackage/DataSource/"+group+"/members.txt";
			String user = uname;
			
			BufferedReader reader = new BufferedReader(new FileReader(path));
			reader.readLine();
			String line;
			String[] contents;
			String topic = null;
			boolean flag = false;
			while((line= reader.readLine())!=null){
				//System.out.println(line);
				contents = line.split(",");
				if(contents[0].equalsIgnoreCase(user)){
					flag = true;
					topic = group;
					topicSet.add(topic);
					break;
				}
				
			}	
			if(flag == true){
				System.out.println("User joined group successfully:"+uname);
				String msg = "join success\n";
				opStream.write(msg.getBytes());
				//Send current user other users status
				List<ServerWorker> workerList =  server.getWorkerList();
				for(ServerWorker worker: workerList){
					if(worker.isMemberOfTopic(topic)){
						if(!uname.equals(worker.getLogin())){
							String existingMsg = "online "+worker.uname+"\n";
							send(existingMsg);
						}
					}
				}
				
				//Send other users current users status
				String onlineMsg = "Online "+uname+"\n";
				for(ServerWorker worker: workerList){
					if(worker.isMemberOfTopic(topic)){
						if(!uname.equals(worker.getLogin())){
							worker.send(onlineMsg);
						}
					}	
				}	
				
			}
			else{
				String msg = "User not part of group\n";
				opStream.write(msg.getBytes());
			}
			
		}
		
	}
	
	public boolean isMemberOfTopic(String topic){
		return topicSet.contains(topic);
	}

	private void handleMsg(String[] tokens) throws IOException {
		
		String sendTo = tokens[1];
		String body = tokens[2];
		
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
        String strDate = dateFormat.format(date);  
		String content = strDate+" "+uname+" : "+body;
		String logPath = "./src/serverpackage/DataSource/"+sendTo+"/messages.txt";
		AppendEntryToFile(logPath,content);
		
		List <ServerWorker> workerList = server.getWorkerList();
		for(ServerWorker worker : workerList){
				if((worker.isMemberOfTopic(sendTo)) && (worker.uname!=uname)){
					String outMsg = "msg "+uname + " "+body+ "\n";
					worker.send(outMsg);
				}
		}
		
	}


	private void handleLogoff() throws IOException {
		server.removeWorker(this);
		
		//Send other users that current user logged off
		String onlineMsg = "Offline "+uname+"\n";
		List<ServerWorker> workerList =  server.getWorkerList();
		for(ServerWorker worker: workerList){
			if(!uname.equals(worker.getLogin())){
				worker.send(onlineMsg);
			}
		}
		cSocket.close();
	}


	public String getLogin(){
		return uname;
	}
	
	




	private void send(String msg) throws IOException {
		if(uname!=null){
			opStream.write(msg.getBytes());
		}
		
	}
	
	private boolean checkUserCredentials(String path, String fieldValue1,int fieldNum1,String fieldValue2,int fieldNum2) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));
		reader.readLine();
		String line;
		String[] contents;
		boolean flag = false;
		while((line= reader.readLine())!=null){
			//System.out.println(line);
			contents = line.split(",");
			if((contents[fieldNum1-1].equalsIgnoreCase(fieldValue1)) && (contents[fieldNum2-1].equals(fieldValue2))){
				flag = true;
				break;
			}
			
		}	
		
		return flag;
	}
	
	private boolean AppendEntryToFile(String path, String entry) {
		
		// TODO Auto-generated method stub
		try
		{
		    FileWriter fw = new FileWriter(path,true); //the true will append the new data
		    fw.write(entry+"\n");//appends the string to the file
		    fw.close();
		    return true;
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		    return false;
		}
		
	}
	
	
	private boolean RemoveEntryToFile(String group, String entry) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		String groupPath = "./src/serverpackage/DataSource/"+group+"/members.txt";
		
		File f = new File("./src/serverpackage/DataSource/"+group+"/temp.txt");
		 if(f.exists()){
			 f.delete();
		 }
		 f.createNewFile();
		 
		 
		  
	        BufferedReader br = new BufferedReader(new FileReader("./src/serverpackage/DataSource/"+group+"/members.txt")); 
	  
	        
	        FileWriter fw = new FileWriter("./src/serverpackage/DataSource/"+group+"/temp.txt",true); //the true will append the new data	    
	        String line = null; 
	          
	        // hashset for storing lines of delete.txt 
	         
	          
	        // loop for each line of delete.txt 
	        while((line= br.readLine())!=null)
	        { 
	          if(!entry.equalsIgnoreCase(line)){
	        	  fw.append(line+"\n");
	          }
	        }
	        fw.flush();  
	        br.close();
	        fw.close();
	        
	        
	        FileWriter fw1 = new FileWriter("./src/serverpackage/DataSource/"+group+"/members.txt");
	        fw1.write("");
	        fw1.close();
	        
	        FileWriter fw2 = new FileWriter("./src/serverpackage/DataSource/"+group+"/members.txt",true);
	        BufferedReader br1 = new BufferedReader(new FileReader("./src/serverpackage/DataSource/"+group+"/temp.txt"));
	        while((line= br1.readLine())!=null)
	        { 
	        	  fw2.append(line+"\n");
	        }
	        
	        
	        br1.close();
	        fw2.close();
	        
	        return true;
	}
	
	
	
	
}


