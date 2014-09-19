package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class SigBox handles most of the certificate, trust ring counting, and association
 * which needs to be done by the server.
 * 
 * @author Callum Attryde 21142828, Dean Cook 21146117
 *
 */
public class SigBox {

	public HashMap<File, List<File>> signatureMap;

	public SigBox(String defaultPath) {
		File serverStateLocation = new File("server/serverState.txt");
		if (serverStateLocation.exists()) {
			this.signatureMap = (HashMap<File, List<File>>) ServerSaver.loadFromFile();
		} else {
			signatureMap = new HashMap<File, List<File>>();
		}
		
		loadFilesToMap(defaultPath);
	};
	
	public void loadFilesToMap(String folderPath) {
		File folder = new File(folderPath);
		String fileName;
		File[] listOfFiles = folder.listFiles(); 
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
					fileName = listOfFiles[i].getName();
					File file = new File(fileName);
					
					if (!this.signatureMap.containsKey(file)) {
						this.signatureMap.put(file, new ArrayList<File>());
						this.signatureMap.get(file).add(new File("Not Protected"));
					}
			}
		}
		
		/*
		 * removes any lingering filenames in the hashmap that may have been manually deleted
		 * on the server
		 */
		Iterator<File> it = this.signatureMap.keySet().iterator();
		while (it.hasNext()) {
			File next = it.next();
			File fullFile = new File(folderPath + next.getName());
			if (!fullFile.exists()) {
				it.remove();
			}
		}
	}

	public boolean ringGet(int circ, String fileName) {
		File file = new File(fileName);	
		
		int longestRing = 0;
		List<File> checkList = new ArrayList<File>();
		boolean search = true;

		if (signatureMap.containsKey(file)) {
			checkList = signatureMap.get(file);
			for (File s : checkList) {
				File end = s;
				int hops = 0;

				while (signatureMap.containsKey(s) && search == true) {
					s = signatureMap.get(s).get(0);
					hops++;
					if (s.equals(end)) {

						search = false;
						if (longestRing < hops) {
							longestRing = hops;
						}
					}
				}
			}
			if (longestRing >= circ) {
				System.out.println("Success, there is a trust circumference of: " + longestRing + " entrusting: " + file.getName());
				return true;
			}
		} else {
			System.out.println("File not found!");
		}
		System.out.println("Could not find any rings of trust on this file matching cicumference: " + circ);
		return false;
	}

	public boolean checkCert(byte[] sigToVerify, String pathToCert, String certName) {
		boolean fileVerified = false;
		
		File certFile = new File(pathToCert + certName);
		
		FileInputStream sigfis;
		
		InputStream inStream = null;
		PublicKey pubKey = null;

		try {
			inStream = new FileInputStream(certFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			pubKey = cert.getPublicKey();
			
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(pubKey);
			fileVerified = sig.verify(sigToVerify);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileVerified;
	}

public boolean associate(String fileName, String certName){
		boolean successfulAssociation = false;
		
		File file = new File(fileName);
		File cert = new File(certName);
		
		FileExaminer fileEx2 = new FileExaminer(file.getName(), '/', '.');
		FileExaminer fileEx = new FileExaminer(cert.getName(), '/', '.');
		
		if (fileName.equals(certName)) {
			System.out.println("Error! Client attempted to verify certificate with itself.");
			return successfulAssociation;
		}
		
		if(fileEx.extension().equals("cert")){
			System.out.println(signatureMap.get(file).toString());
			if(!signatureMap.containsKey(file)){
		
				System.out.println("HELLO1");
				
				List<File> list1 = new ArrayList<File>();
				list1.add(cert);
				signatureMap.put(file, list1);
				successfulAssociation = true;
		
			}
			if(signatureMap.containsKey(file) && !signatureMap.get(file).contains(cert)){
			
				System.out.println("HELLO2");
				signatureMap.get(file).add(cert);
				successfulAssociation = true;
			}
		} else{
			System.out.println("Error! Tried to associate file with non-certificate file. Vouching operation cancelled");
		}
		return successfulAssociation;				
	}   
}
