package server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ServerSaver {
		
	public static void saveToFile(Map<File, List<File>> map) {
		
		FileOutputStream fOutStream;
		ObjectOutputStream objOutStream;
		try {
			fOutStream = new FileOutputStream("server/serverState.txt");
			objOutStream = new ObjectOutputStream(fOutStream);
			objOutStream.writeObject(map);
			objOutStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	

	public static Map loadFromFile() {
			
		FileInputStream fInStream;
		ObjectInputStream objInStream;
		Map<File, List<File>> map = null; 
		try {
			fInStream = new FileInputStream("server/serverState.txt");
			objInStream = new ObjectInputStream(fInStream);
			map = (Map<File, List<File>>) objInStream.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		return map;
	}
}
