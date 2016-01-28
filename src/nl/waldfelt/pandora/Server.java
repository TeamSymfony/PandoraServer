package nl.waldfelt.pandora;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import nl.waldfelt.pandora.discovery.DiscoveryProtocol;
import nl.waldfelt.pandora.message.ClientMessage;


public class Server extends Thread {
	
	// ----- Constants
	
	// Port number: CRC32(waldfelt) % 100,000
	private static final int	_serverPort = 7274;
	public static final long SERVER_THROTTLE = 20;
	
	// ----- Server Variables
	private ServerSocket	 	mServerSocket;
	private Socket				mClientSocket;
	private ArrayList<Client> 	mClients;
	
	private DiscoveryProtocol mDiscoveryProtocol;
	
	
	public Server() {
		mDiscoveryProtocol = new DiscoveryProtocol();
		
		// Open server socket
		try {
			mServerSocket = new ServerSocket(_serverPort);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		mClients = new ArrayList<Client>();
		System.out.println("Server " + mServerSocket + " is created.");
	}
	
	public void run() {
		System.out.println("Server is listening...");
		
		while(true) {
			
			try {
				mClientSocket = mServerSocket.accept();
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			// If client connects, add them to the list of active clients and ask for identification
			System.out.println("Client " + mClientSocket + " has connected.");
			Client lClient = new Client(mClientSocket, this);
			lClient.addResponse( new ClientMessage("identify", null) );
			mClients.add( lClient );
			
			try {
				Thread.sleep(SERVER_THROTTLE);
				
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public DiscoveryProtocol getDiscoveryProtocol() {
		return mDiscoveryProtocol;
	}
	
	public void disconnect(Client pClient) {
		mClients.remove(pClient);
	}
	
	
}
