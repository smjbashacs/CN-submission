package serverpackage;

import java.io.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		String path = "./src/serverpackage/DataSource/users.txt";
//		BufferedReader reader = new BufferedReader(new FileReader(path));
//		reader.readLine();
//		String line;
//		while((line= reader.readLine())!=null){
//			System.out.println(line);
//
//		}
		
		 	File gFile = new File("./src/serverpackage/DataSource/CSE/members.txt");
	        File tempFile = new File("./src/serverpackage/DataSource/CSE/temp.txt");
	        
	        gFile.delete();
	        if(tempFile.renameTo(gFile)){
	        	System.out.println("renaming success");
	        }
	      
		
	}

}