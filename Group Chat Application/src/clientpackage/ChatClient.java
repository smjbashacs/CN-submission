package clientpackage;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatClient {

	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream serverOut;
	private InputStream serverIn;
	private BufferedReader bufferedIn;
	private String clientGroup=null;
	
	private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
	private ArrayList<MessageListener> msgListeners = new ArrayList<>();

	public ChatClient(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
	}

	public static void main(String[] args) throws IOException {
		ChatClient client = new ChatClient("localhost",3410);
		
		client.addUserStatusListener(new UserStatusListener() {
			
			@Override
			public void online(String uname) {
				System.out.println(uname+"  ONLINE");
			}
			
			@Override
			public void offline(String uname) {
				System.out.println(uname+"  OFFLINE");
			}
		});
		
		
		client.addMessageListener(new MessageListener() {
			
			@Override
			public void onMessage(String fromLogin, String msgBody) {
				System.out.println("		"+fromLogin+": "+msgBody);
			}
		});
		
		if(!client.connect()){
			System.out.println("Connect failed");
		}
		else{
			System.out.println("		===========  Connection successful  =============        ");
			Scanner scanner = new Scanner(System. in);
			System.out.println("Enter 1 to login, 2 to register");
			String flagLogin = scanner.nextLine();
			if("2".equalsIgnoreCase(flagLogin)){
				System.out.println("Enter a username to register:");
				String rUname = scanner.nextLine();
				System.out.println("Enter a password to complete registration:");
				String rPassword = scanner.nextLine();
				if(!client.register(rUname,rPassword)){
					System.exit(0);
				}
				System.out.println("Registration successful! Continue to login");
				
			}
			
				System.out.print("Enter username:");
				String uname = scanner.nextLine();
				System.out.print("Enter password:");
				String password = scanner.nextLine();
				String group = null;
				String addGroup = null;
				String unjoinGroup = null;
				if(client.login(uname,password)){
					System.out.println("\n		==========Welcome "+uname+", Login successful===========");
					while(true){
						
						//Display list of groups User is part of and give options 
						client.getListOfJoinedGroups(uname);
						
						//Read the option of Groupname or ADD or UNJOIN or LISTGROUPS
						group = scanner.nextLine();
						
						if(group.equalsIgnoreCase("LIST")){
							client.getListOfAllGroups();
						}
						else if(group.equalsIgnoreCase("ADD")){
							client.getListOfAllGroups();
							System.out.println("Select a group to join");
							addGroup = scanner.nextLine();
							client.addToGroup(addGroup);
						}
						else if(group.equalsIgnoreCase("UNJOIN")){
							client.displayListOfJoinedGroups(uname);
							//System.out.println("Select a group to unjoin");
							unjoinGroup = scanner.nextLine();
							client.unjoinGroup(unjoinGroup);
						}
						else if(group.equalsIgnoreCase("LOGOFF")){
							client.logoff();
							System.exit(0);
						}
						else{
							if(client.sendGroupToJoin(group)){
								System.out.println("	Start messaging in the group : "+group);
								System.out.println("Use below commands in format *cmd command* for below special operations");
								System.out.println(" GetChatLog:	gcl \n ListGroupFiles:lgf \n ListAllFiles:	laf \n UploadFile:	upload \n ShareFile:	share \n DeleteFile:	delete");
								while(true){
									String msg = scanner.nextLine();
									String[] msgTokens = msg.split(" ", 2);
									//System.out.println("in while loop");
									if("logoff".equalsIgnoreCase(msgTokens[0])){
										client.logoff();
										System.exit(0);
										break;
									}
									else if(msgTokens[0].equalsIgnoreCase("cmd")){
										client.cmd(client.clientGroup,msgTokens[1]);
									}
									else{
										//client.msg(msgTokens[0],msgTokens[1]);
										//System.out.println("in msg");
										client.msg(client.clientGroup,msg);
									}	
								}
							}	
							else{
								//System.out.println("You are not part of group");
							}
						}

					}
					
				}
				else{
					System.out.println("Login failed");
				}
			
			
			
			//client.logoff();
			
		}
	}

	private void unjoinGroup(String unjoinGroup) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "unjoinGroup "+unjoinGroup+"\n";
		//System.out.println("cmd:"+cmd);
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		System.out.println(response);
		
	}

	private void cmd(String clientGroup,String cmd) throws IOException {
		// TODO Auto-generated method stub
		//if(cmd.equals
		String msg = "msglog "+clientGroup+"\n";
		serverOut.write(msg.getBytes());
		
	}
	
	private void msg(String sendTo, String msgBody) throws IOException {
		String cmd = "msg "+sendTo+" "+msgBody+ "\n";
		serverOut.write(cmd.getBytes());
		
	}
	
	private void addToGroup(String addGroup) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "addtogroup "+addGroup+"\n";
		//System.out.println("cmd:"+cmd);
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		System.out.println(response);
		
	}

	private void getListOfAllGroups() throws IOException {
		// TODO Auto-generated method stub
		String cmd = "getallgroups \n";
		//System.out.println("cmd:"+cmd);
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		
		String[] groups = response.split(";");
		System.out.println("	List of all available groups");
		for(int i = 1;i< groups.length ;i++){
			System.out.println("		"+groups[i]);
		}
		
		
	}

	private void getListOfJoinedGroups(String uName) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "getgroups "+uName+"\n";
		//System.out.println("cmd:"+cmd);
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		//System.out.println("Response line:"+response);
		
		if(response.equalsIgnoreCase("User not part of any group")){
			System.out.println("warning: User not part of any group till now, Select ADD option to join a new group");
			System.out.println("Enter ADD To join a new group");
			
		}
		else {
			System.out.println("\nEnter a group name to enter:");
			String[] groups = response.split(";");
			for(int i = 1;i< groups.length ;i++){
				System.out.println("	"+groups[i]+" --> To enter "+groups[i]+" group");
			}
			
			System.out.println("	LIST --> To get list of all groups");
			System.out.println("	ADD --> To join a new group");
			System.out.println("	UNJOIN --> to leave an existing group");
			
		}
		
	}
	
	
	
	
	private void displayListOfJoinedGroups(String uName) throws IOException {
		// TODO Auto-generated method stub
		String cmd = "getgroups "+uName+"\n";
		//System.out.println("cmd:"+cmd);
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		//System.out.println("Response line:"+response);
		
		if(response.equalsIgnoreCase("User not part of any group")){
			System.out.println("User not part of any group till now");
			
		}
		else {
			System.out.println("\nSelect a group name to unjoin:");
			String[] groups = response.split(";");
			for(int i = 1;i< groups.length ;i++){
				System.out.println("	"+groups[i]);
			}
			
		}
		
	}
	
	
	private boolean sendGroupToJoin(String group) throws IOException{
		String cmd = "join "+group+ "\n";
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		//System.out.println("response line:"+response);
		if(response.equalsIgnoreCase("join success")){
			startMessageReader();
			clientGroup = group;
			return true;
		}
		else{
			System.out.println(response);
			return false;
		}
		
	}
	
	
	

	private boolean register(String rUname, String rPassword) throws IOException {
		String cmd = "register "+rUname+" "+rPassword+"\n";
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		System.out.println("response line:"+response);
		if(response.equalsIgnoreCase("Registration successful")){
			return true;
		}
		else{
			return false;
		}
			
	}



	private void logoff() throws IOException {
		String cmd = "logoff"+ "\n";
		serverOut.write(cmd.getBytes());
		
	}

	private boolean login(String login, String password) throws IOException {
		String cmd = "login "+login+" "+password+"\n";
		serverOut.write(cmd.getBytes());
		String response = bufferedIn.readLine();
		System.out.println("response line:"+response);
		if(response.equalsIgnoreCase("Login successful")){
			return true;
		}
		else{
			return false;
		}
		
		
	}

	private void startMessageReader() {
		Thread t = new Thread(){
			@Override
			public void run() {
				readMessageLoop();
			}
	
		};
		t.start();
		
	}

	private void readMessageLoop() {
		String line;
		try {
			while((line=bufferedIn.readLine())!=null){
				//System.out.println("in read loop");
				String[] tokens = line.split(" ");
				if(tokens!=null && tokens.length>0){
					String cmd = tokens[0];
					if("online".equalsIgnoreCase(cmd)){
						handleOnline(tokens);
					}
					else if("offline".equalsIgnoreCase(cmd)){
						handleOffline(tokens);
					}
					else if("msg".equalsIgnoreCase(cmd)){
						//System.out.println("Line:"+line);
						String[] tokenMsg = line.split(" ", 3);
						handleMessage(tokenMsg);
					}
					else{
						System.out.println("	"+line);
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void handleMessage(String[] tokens) {
		String login = tokens[1];
		String msgBody = tokens[2];
		for(MessageListener listener : msgListeners){
			listener.onMessage(login, msgBody);
		}
		
	}

	private void handleOffline(String[] tokens) {
		String login = tokens[1];
		for(UserStatusListener listener : userStatusListeners){
			listener.offline(login);
		}
		
	}

	private void handleOnline(String[] tokens) {
		String login = tokens[1];
		for(UserStatusListener listener : userStatusListeners){
			listener.online(login);
		}
		
	}

	public boolean connect() {
		try {
			this.socket = new Socket(serverName,serverPort);
			System.out.println("Client port is :"+socket.getLocalPort());
			this.serverOut = socket.getOutputStream();
			this.serverIn = socket.getInputStream();
			this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
			return true;
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	public void addUserStatusListener(UserStatusListener listener){
		userStatusListeners.add(listener);
	}
	
	public void removeUserStatusListener(UserStatusListener listener){
		userStatusListeners.remove(listener);
	}
	
	public void addMessageListener(MessageListener listener){
		msgListeners.add(listener);
	}
	
	public void removeMessageListener(MessageListener listener){
		msgListeners.remove(listener);
	}
	
}
