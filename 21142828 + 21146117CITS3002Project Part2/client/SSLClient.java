package client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import server.FileExaminer;

/**
 * SSLClient provides the core functionality to connect and send/receive files
 * to/from the server. No protocol details are listed in this class, but the methods
 * here are used by the ClientStarter class as part of the protocols.
 * @author Dean Cook 21146117, Callum Attryde 21142828
 *
 */
public class SSLClient {
	private String hostName;
	private int port;
	
	public static final String ROOT = "client/";
	public static final String FILEPATH = "client/files/";
	
	private SSLSocket socket;
	
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	
	private int minimumCircumference;

	/**
	 * Client constructor, creates a client that will (when establishConnection
	 * is called) attempt to connect to a server located at address 'hostName'
	 * listening on port 'port'.
	 * @param hostName
	 * @param port
	 */
	public SSLClient(String hostName, int port) {	
		System.setProperty("javax.net.ssl.trustStore", "mySrvKeystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		setSocketAddress(hostName, port);
		minimumCircumference = 0;
	}
	
	/**
	 * Method established connection with the server (either default or specified
	 * by -h command) and prepares for data transfer.
	 */
	public void establishConnection() {
		try {
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			this.socket = (SSLSocket) sslsocketfactory.createSocket(this.hostName, this.port);
			
			//These streams are used for transferring raw bytes
			this.dataIn = new DataInputStream(this.socket.getInputStream());
			this.dataOut = new DataOutputStream(this.socket.getOutputStream());
			
			
		} catch (UnknownHostException e) {
			System.out.println("Uknown host, Client exiting, or if permanent, please try the -h command again and check the input.");
		} catch (IOException e) {
			System.out.println("Issue establishing data streams, please try connecting again.");
		}
	}
	
	/**
	 * appends an integer to the output stream to inform the server of any 
	 * commands or extra information required in the protocol
	 * @param message, the message to send to the server
	 */
	public void sendInt(int message) {
		try {
			this.dataOut.writeInt(message);
			this.dataOut.flush();
		} catch (Exception e) {
			System.out.println("Issue sending int message to server. Closing connection and stopping/restarting.");
		}
	}
	
	/**
	 * Sends a message to the server over the output stream
	 * @param data, the message to be sent as a byte[]
	 */
	public void sendBytes(byte[] data) {
		try {
			int dataLength = data.length;
			
			this.dataOut.writeInt(dataLength);
			this.dataOut.write(data);
			
			this.dataOut.flush();
		} catch (Exception e) {
			System.out.println("Issue sending byte message to server. Closing connection and stopping/restarting.");
		}
	}
	
	/**
	 * Reads in the next message on the input stream from the server and
	 * returns the message as an array of bytes
	 * @return the message from the client as a byte[]
	 */
	public byte[] receiveBytes() {
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
		} catch (Exception e) {
			System.out.println("Issue receiving byte message from server. Closing connection and stopping/restarting.");
		}
		return data;
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
		} catch (Exception e) {
			System.out.println("Issue saving file on local machine. Aborting and stopping/restarting.");
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
		} catch (Exception e) {
			System.out.println("Issue reading file on local machine. Aborting and stopping/restarting.");
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
	 * Sets the server located at "hostName" listening to "port" that the client
	 * attempts to connect to.
	 * 
	 * @param hostName the IP address of the target server
	 * @param port the port number the target server is listening to.
	 */
	public void setSocketAddress(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}
	
	/**
	 * Method sets a minimum circumference of trust that the client will
	 * pass to the server so the server can determine which files are 'trusted'.
	 * Once set, it will become the default circumference for the rest of the
	 * life of the client.
	 * 
	 * @param circumference the trust ring size used to determine the 'trustworthiness
	 * of a file.
	 */
	public void setMinimumTrust(int circumference) {
		this.minimumCircumference = circumference;
	}
	
	/**
	 * Returns the currently set minimum circumference size that the client
	 * is using.
	 * @return
	 */
	public int getMinimumTrust() {
		return this.minimumCircumference;
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
	
	/**
	 * Creates a digital signature based off of the client's certificate. 
	 * The signature is then saved to a default file called 'sig' which is
	 * then sent to the server to be verified
	 * @param pathToCert the location of the certificate to be used 
	 * @param certName the name of the certificate file
	 */
	public byte[] makeSig(String pathToCert, String certName) {
    	FileExaminer fileEx = new FileExaminer(certName, '/', '.');
    	certName = fileEx.filename();
    	byte[] signatureBytes = null;		
		
    	String fileName = pathToCert + certName + ".pk8";
    	Signature sig = null;
    	
    	try{
    		RandomAccessFile raf = new RandomAccessFile(fileName, "r");
    		byte[] buf = new byte[(int)raf.length()];
    		raf.readFully(buf);
    		raf.close();

    		PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(buf);
    		KeyFactory kf = KeyFactory.getInstance("RSA");
    		PrivateKey privKey = kf.generatePrivate(kspec);
    		
    		sig = Signature.getInstance("SHA1withRSA");
    		sig.initSign(privKey);
    		
    		signatureBytes = sig.sign();
    		
    	} catch (Exception e){
    		e.printStackTrace();
    	} 
    	return signatureBytes;
    }
	
	/**
	 * Overloaded method to create digital signature, assuming that the
	 * certificate is located in the default location
	 * @param certName the name of the certificate file.
	 */
	public byte[] makeSig(String certName) {
		return makeSig(FILEPATH, certName);
	}
	
	/**
	 * Closes connection and all associated input/output streams
	 * Should be called at the end of every protocol.
	 */
	public void closeConnection() {
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
