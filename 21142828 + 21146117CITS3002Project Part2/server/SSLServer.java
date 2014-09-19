package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * SSLServer provides the core functionality to connect and send/receive files
 * to/from a client. This class is self contained, containing methods which are used
 * in the message protocols, as well as the structure of the protocols themselves 
 * (see the 'run()' method for where the protocols are laid out)
 * 
 * This class also contains it's own main() method
 * @author Callum Attryde 21142828, Dean Cook 21146117
 *
 */
public class SSLServer {
	private static final int PORT = 443;
	
	private static final String ROOT = "server/";
	private static final String FILEPATH = "server/files/";
	
	private SigBox sigBox;
	
	private SSLServerSocket listener;
	private SSLSocket socket;
	
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	
	public SSLServer() {
		try {
			System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
			System.setProperty("javax.net.ssl.keyStorePassword", "123456");
			
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	        this.listener = (SSLServerSocket) sslserversocketfactory.createServerSocket(PORT);
			
			newConnection();
			
		} catch (IOException e) {
			System.out.println("Issue initialising ServerSocket, exiting.");
			System.exit(1);
		}
	}
	
	/**
	 * (Re)establishes the connection with the client and 
	 * (re)initialises the input/output streams that the server needs
	 * (It's important that the streams are reset after each loop!)
	 */
	public void newConnection() {
		try {
			this.sigBox = new SigBox(FILEPATH);
			this.socket = (SSLSocket) listener.accept();
			//These streams are used for transferring raw bytes
			this.dataIn = new DataInputStream(this.socket.getInputStream());
			this.dataOut = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Issue connecting with client, attempting again.");
		}
	}
	
	/**
	 * Closes connection and all associated input/output streams
	 * Should be called at the end of loop (protocol).
	 */
	public void closeConnection() {
		try {
			this.socket.close();
		} catch (IOException e) {
			System.out.println("Issue closing connection, exiting.");
			System.exit(1);
		}
	}
	
	/**
	 * reads in integers appended to the input stream by the client for
	 * additional information in the protocol, such as the command flag
	 * or the minimun trust ring circumference
	 * @return the 
	 */
	private int receiveInt() {
		int header = -1;
		try {
			header = this.dataIn.readInt();
		} catch (IOException e) {
			System.out.println("Issue recieving header from client. Closing connection and restarting.");
		}
		return header;
	}
	
	/**
	 * Reads in the next message on the input stream from the client and
	 * returns the message as an array of bytes
	 * @return the message from the client as a byte[]
	 */
	private byte[] receiveBytes() {
		byte[] data = null;
		try {
			int dataLength = this.dataIn.readInt();
			data = new byte[dataLength];
			
			int read = 0;
			int pos = 0;
			
			while (pos < dataLength) {
				read = this.dataIn.read(data, pos, dataLength-pos);
				pos += read;
			}
		} catch (IOException e) {
			System.out.println("Issue recieving bytes from client. Closing connection and restarting.");
		}
		return data;
	}
	
	/**
	 * Sends a message to the client over the output stream
	 * @param data, the message to be sent as a byte[]
	 */
	private void sendBytes(byte[] data) {
		try {
			int dataLength = data.length;
			
			this.dataOut.writeInt(dataLength);
			this.dataOut.write(data);
			
			this.dataOut.flush();
		} catch (IOException e) {
			System.out.println("Issue sending bytes to client. Closing connection and restarting.");
		}
	}
	
	
	/**
	 * Saves a byte[] to a file fileName, located at pathName
	 * @param data, the data to be saved as a byte[]
	 * @param pathName, the folder name that the file is located in
	 * @param fileName, the file name
	 */
	public void saveFile(byte[] data, String pathName, String fileName) {
		FileOutputStream outputStream = null;
		File file = new File (pathName + fileName);
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(data);
		} catch (IOException e) {
			System.out.println("Issue saving file to " + pathName + fileName + ", aborting attempt, closing connection and restarting");
		}
		
	}
	
	/**
	 * Saves a byte[] to a file fileName, located at the default location
	 * @param data, the data to be saved as a byte[]
	 * @param fileName, the file name
	 */
	public void saveFile(byte[] data, String fileName) {
		saveFile(data, FILEPATH, fileName);
	}
	
	/**
	 * Reads a file fileName, located at pathName, and returns the information as a byte[]
	 * @param pathName, the folder name that the file is located in
	 * @param fileName, the file name
	 * @return the data in the file
	 */
	public byte[] readFile(String pathName, String fileName) {
		byte[] fileData = null;
		
		FileInputStream inputStream;
		File file = new File(pathName + fileName);
		try {
			inputStream = new FileInputStream(file);
			int fileLength = inputStream.available();
			
			fileData = new byte[fileLength];
			inputStream.read(fileData, 0, fileLength);
			inputStream.close();
		} catch (IOException e) {
			System.out.println("Issue reading file from " + pathName + fileName + ", aborting attempt, closing connection and restarting");
		}
		return fileData;
	}
	
	/**
	 * Reads a file fileName, located at the default location, and returns the information as a byte[]
	 * @param fileName, the file name
	 * @return the data in the file
	 */
	public byte[] readFile(String fileName) {
		return readFile(FILEPATH, fileName);
	}
	
	/**
	 * Checks to see that the file exists within the specified location on the local
	 * machine
	 * @param pathName the path to the specified file
	 * @param fileName the name of the file
	 * @return true if file exists, false otherwise
	 */
	public boolean fileExists(String pathName, String fileName) {
		File file = new File(pathName + fileName);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks to see that the file exists within the default location on the local
	 * machine
	 * @param fileName the name of the file
	 * @return true if file exists, false otherwise
	 */
	public boolean fileExists(String fileName) {
		return fileExists(FILEPATH, fileName);
	}
	
	public void shutDownServer() {
		System.out.println("Server is shutting down");
		try {
			this.socket.close();
		} catch (IOException e) {
			System.out.println("Issue closing socket connection before shutdown, forcefully exiting.");
			System.exit(1);
		}
		System.exit(0);
	}
	
	/**
	 * Method used to neatly format the ouput for the -l command. Will add
	 * a spacer after string firstColumn, such that the spacer ends after 40
	 * characters. The spacer is constructed out of characters spaceChar.
	 * @param spaceChar
	 * @param firstColumn
	 * @return the spacer which can be appended after firstColumn for consistent
	 * column width
	 */
	public String addSpacer(char spaceChar, String firstColumn) {
		int len = firstColumn.length();
		String spacer = "";
		
		for (int i = firstColumn.length(); i < 40 ; i++) {
			spacer += spaceChar;
		}
		return spacer;
	}
	
	public static void main(String[] args) {
		System.out.println("Server is running");
		
		SSLServer server = new SSLServer();
		server.runServer();
		//server.shutDownServer(); //doesn't reach this method call ever
	}
	
	/**
	 * The method that checks to establishes a new socket connection, and then
	 * waits to see which protocol will be requested by the client. Once done, 
	 * this method then proceeds with various method calls in the correct order
	 * to fulfull the various commands the client can pass it.
	 * 
	 * Once a command has beenf fulfilled, the run method closes the connection, and
	 * reestablishes another, waiting for the next command from the client.
	 * 
	 * Note, invalid commands will not return anything, the server will simply drop
	 * the connection and restart.
	 */
	public void runServer() {
		if (this.socket.isClosed()) {
			newConnection();
		}
		
		//TODO implement server errcodes
		
		//TODO maintain file structure when saving/loading files
		int header = receiveInt();
		System.out.println("Header read from client: " + header);
		
		String fileName;
		String certName;
		File file;
		int requestedCirc;
		
		switch (header) {
			
		case 0: //add file to server
			fileName = new String(receiveBytes());
			
			file = new File(fileName);
			if (this.sigBox.signatureMap.containsKey(file)) {
				this.sigBox.signatureMap.put(file, new ArrayList<File>());
				this.sigBox.signatureMap.get(file).add(new File("Not Protected"));
			}
			
			System.out.println("Client adding file: " + fileName);
			saveFile(receiveBytes(), fileName);
			
			break;
			
		case 1: //retrieve file from server
			requestedCirc = receiveInt();
			
			fileName = new String(receiveBytes());
			System.out.println("Client asking for file: " + fileName + " if trust ring cirumference is greater than " + requestedCirc);
			
			if (sigBox.ringGet(requestedCirc, fileName)) {
				byte[] fileData = readFile(fileName);
				sendBytes(fileData);
			}
			
			break;
			
		case 2: //list all files on server
			requestedCirc = receiveInt();
			System.out.println("Client asking for all files of trust ring circumference " + requestedCirc);
			
			Iterator<Entry<File, List<File>>> it = sigBox.signatureMap.entrySet().iterator();
			String output = "";
			
			output += "File Name:" + addSpacer(' ', "File Name:") + "Vouched By:" + '\n';
			
			output += "__________________________" + 
			addSpacer(' ', "__________________________") + "__________________________" + '\n';
			
			while (it.hasNext()) {
				Entry<File, List<File>> entry = it.next();
				fileName = entry.getKey().toString();
				String protection = entry.getValue().toString();
				if (sigBox.ringGet(requestedCirc, fileName)) {
					output += fileName + addSpacer('.', fileName) + protection + '\n';
				}
			}
			
			byte[] data = output.getBytes();
			
			sendBytes(data);
			
			break;
			
		case 3: //add certificate to server
			certName = new String(receiveBytes());
			certName = certName + ".cert";
			
			file = new File(certName);
			if (this.sigBox.signatureMap.containsKey(file)) {
				this.sigBox.signatureMap.remove(file);
			}
			
			System.out.println("Client uploading cerfiticate: " + certName);
			saveFile(receiveBytes(), certName);
			break;
		
		case 4: //vouch for file on server with a certificate		
			fileName = new String(receiveBytes());
			certName = new String(receiveBytes());
			certName += ".cert";
			
			byte[] sigToVerify = receiveBytes();
//			for (int i = 0; i < sigToVerify.length; i++) {
//				System.out.print(sigToVerify[i]);
//			}
			
			System.out.println("Client vouching for file: " + fileName + " with certificate: " + certName);
			
			if (this.sigBox.checkCert(sigToVerify, FILEPATH, certName)) {
				if (fileExists(fileName)) {
					if (fileExists(certName)) {
						this.sigBox.associate(fileName, certName);
					} else {
						System.out.println("Certificate " + certName + " does not exist on the server");
					}
				} else {
					System.out.println("File " + fileName + " does not exist on the server");
				}
			} else {
				System.out.println("The digital signature doesn't match the certificate, aborting vouch.");
			}		
			
			break;
			
		default:
			System.out.println("Unrecognised header. Closing connection.");
			break;
		}
		
		ServerSaver.saveToFile(this.sigBox.signatureMap);
		closeConnection();
		runServer();
	}
}
