package client;

/**
 * Class ClientStarter handles the running and arguments of the client. This class controls
 * whether or not the client should restart upon finishing each protocol, which can be specified
 * using the -r command when the client is started.
 * 
 * @author Dean Cook 21146117, Callum Attryde 21142828
 *
 */
public class ClientStarter {
	private static final String DEFAULTHOST = "localhost";
	private static final int DEFAULTPORT = 443;
	
	private UserInputReader userInputReader;
	
	private SSLClient client;
	private String[] args;
	
	
	public ClientStarter(String[] args, boolean remainOn, String customSocket) {
		this.args = args;
		
		String socketAddress[] = customSocket.split(":");
		this.client = new SSLClient(socketAddress[0], Integer.parseInt(socketAddress[1]));
		
		this.userInputReader = new UserInputReader();
		
		do {
			handleCommands();
			if (remainOn) {
				this.args = this.userInputReader.getUserInput("Client waiting for further commands, type \"exit\" to exit").split(" ");
			}
		} while (remainOn);
		
		
	}
	
	/*
	 * Main method handles all command line arguments. 
	 */
	public static void main(String[] args) {
		String customSocket = DEFAULTHOST + ":" + DEFAULTPORT;
		boolean remainOn = false;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h")) {
				if (i < args.length-1) {
					customSocket = args[i+1];
				} else {
					System.out.println("For command -h, you must specify a hostname and port in the form: hostname:port");
				}
				
			}
			
			if (args[i].equals("-r")) {
				remainOn = true;
			}
		}
		
		ClientStarter handler = new ClientStarter(args, remainOn, customSocket);

	}
	
	private void handleCommands() {
		boolean aflag = false;
		String aarg = "";
		
		boolean cflag = false;
		int carg = 0;
		
		boolean fflag = false;
		String farg= "";
		
		boolean hflag = false;
		String harg= "";
		
		boolean lflag = false;
		
		boolean uflag = false;
		String uarg= "";
		
		boolean vflag = false;
		String[] varg = new String[2];
		
		/*
		 * the following switch statement determines which command flags have been used
		 * and also tries to determine their relative arguments.
		 * 
		 * Currently no warnings are output if the arguments are supplied incorrectly
		 */
		
		for (int i = 0; i < this.args.length; i++) {
			switch (args[i]) {
			case "-a":
				if (i < args.length - 1) {
					aarg = args[i+1];
					aflag = true;
				}
				break;
			case "-c":
				if (i < args.length - 1) {
					carg = Integer.parseInt(args[i+1]);
					cflag = true;
				}
				break;
			case "-f":
				if (i < args.length - 1) {
					farg = args[i+1];
					fflag = true;
				}
				break;
			case "-h":
				if (i < args.length - 1) {
					harg = args[i+1];
					hflag = true;
				}
				break;
			case "-l":
				lflag = true;
				break;
			case "-u":
				if (i < args.length - 1) {
					uarg = args[i+1];
					uflag = true;
				}
				break;
			case "-v":
				if (i < args.length - 2) {
					varg[0] = args[i+1];
					varg[1] = args[i+2];
					vflag = true;
				}
				break;
			default:
				break;
			}
		}
		
		/*
		 * Below the protocols for running the client are established
		 * in the order they need to run based on which flags were used.
		 * 
		 * Notes:
		 * 		cflag and hflag don't have return statements, allowing a string of commands
		 */
		
		if (cflag) {
			client.setMinimumTrust(carg);
		}
		
		if (hflag) {
			String[] socketAddress = harg.split(":");
			client.setSocketAddress(socketAddress[0], Integer.parseInt(socketAddress[1]));
		}
		
		if (aflag) {
			System.out.println("Uploaded file: " + aarg + " to the server.");
			
			client.establishConnection();
			client.sendInt(0);
			
			byte[] fileName = aarg.getBytes();
			client.sendBytes(fileName);
			
			//TODO check to see if aarg is a pathname+filename or just filename
			byte[] fileData = client.readFile(aarg);
			client.sendBytes(fileData);
			
			client.closeConnection();
			return;
		}
		
		if (lflag) {
			System.out.println("List of all files on server with trust ring of circumference: " + client.getMinimumTrust());
			System.out.println("*******************************************************************************");
			client.establishConnection();
			client.sendInt(2);
			client.sendInt(client.getMinimumTrust());
			
			byte[] data = client.receiveBytes();
			for (int i = 0; i < data.length; i++) {
				System.out.print((char)data[i]);
			}
			System.out.println("*******************************************************************************");
			return;
		}
		
		if (fflag) {
			System.out.println("Requesting file: " + farg + " from the server.");
			
			client.establishConnection();
			client.sendInt(1);
			
			client.sendInt(client.getMinimumTrust());
			
			//TODO check to see if farg is a pathname+filename or just filename
			byte[] fileName = farg.getBytes();
			client.sendBytes(fileName);
			
			byte[] fileData = client.receiveBytes();
			
			if (fileData != null && fileData.length != 0 ) {
				client.saveFile(fileData, farg);
			}		
			
			client.closeConnection();
			return;
		}
		
		if (uflag) {
			System.out.println("Uploading certificate: " + uarg + " to the server.");
			
			client.establishConnection();
			client.sendInt(3);
			
			byte[] certName = uarg.getBytes();
			client.sendBytes(certName);
			
			byte[] certData = client.readFile(uarg + ".cert");
			client.sendBytes(certData);
			
			client.closeConnection();
			return;
		}
		
		if (vflag) {
			System.out.println("Client vouching for file: " + varg[0] + " with certificate: " + varg[1]);
			
			client.establishConnection();
			client.sendInt(4);
			
			byte[] fileName = varg[0].getBytes();
			client.sendBytes(fileName);
			
			byte[] certName = varg[1].getBytes();
			client.sendBytes(certName);
			
			byte[] signature = client.makeSig(varg[1]);
			client.sendBytes(signature);
			
			client.closeConnection();
			return;
		}
	}
}
