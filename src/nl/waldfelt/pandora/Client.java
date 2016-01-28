package nl.waldfelt.pandora;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import nl.waldfelt.pandora.message.ClientMessage;
import nl.waldfelt.pandora.message.MessageHandler;
import nl.waldfelt.pandora.message.ServerMessage;
import nl.waldfelt.pandora.player.Player;
import nl.waldfelt.pandora.player.PlayerDAO;


public class Client {

	// ----- Constants
	public static final long USER_THROTTLE = 1;
	
	// ----- Game Variables
	private Player mPlayer;
	
	// ----- Server Variables
	private Server mServer;
	
	// ----- Client Variables
	private Socket mSocket;
	private boolean isConnected;
	private Receiver mReceiver;
	private Transmitter mTransmitter;
	private Broadcaster mBroadcaster;

	private ArrayList<ClientMessage> mResponses;
	private MessageHandler mHandler;
	
	public Client(Socket pClientSocket, Server pServer) {
		mSocket = pClientSocket;
		mServer = pServer;
		
		isConnected = true;
		mResponses = new ArrayList<ClientMessage>();
		
		// Initialize Player
		mPlayer = new Player();
		
		// Start command & response handler
		mHandler = new MessageHandler(this);
		mHandler.start();

		// Start receiver thread
		mReceiver = new Receiver();
		mReceiver.start();

		// Start transmitter thread
		mTransmitter = new Transmitter();
		mTransmitter.start();
		
		// Start broadcaster thread
		mBroadcaster = new Broadcaster();
		mBroadcaster.start();
		
	}
	
	// Returns whether the client is connected or not
	public boolean isConnected() {
		return isConnected;
	}
	
	// Disconnect a client from the server	
	public void disconnect() {
		try {
			isConnected = false;
			mSocket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// Add new message in order to be sent to client
	public void addResponse(ClientMessage pResponse) {
		mResponses.add(pResponse);		
	}
	
	
	// Receiver Thread. Receives the command and passes it on to the MessageHandler
	private class Receiver extends Thread {
		
		private ObjectInputStream ois;
		
		public void run() {
			try {
				ois = new ObjectInputStream(mSocket.getInputStream());
			} catch (Exception e) {
				disconnect();
			}
			while(isConnected) {
				try {
					
					// Read Command
					Object mServerMessage = ois.readObject();
					if(mServerMessage instanceof ServerMessage) {
						mHandler.addTask( (ServerMessage) mServerMessage);
					}
					
				} catch(Exception e) {
					// Write data to persistant storage and crash succesfully
					handleDisconnect();
					return;
				}
			}
		}
		
	}
	
	// Transmitter Thread. Transmits a command and its data back to the client.
	private class Transmitter extends Thread {
		private ObjectOutputStream oos;
		
		public void run() {
			try {
				oos = new ObjectOutputStream(mSocket.getOutputStream());
			} catch( Exception e ) {
				e.printStackTrace();
			}
			
			while(isConnected) {
				try {
					
					// Send Response
					if(!mResponses.isEmpty()) {
						oos.writeObject(mResponses.remove(0));
						oos.flush();
						oos.reset();
					}
					
				} catch(Exception e) {
					// Write data to persistant storage and crash succesfully
					handleDisconnect();
					return;
				}
			
				try {
					Thread.sleep(USER_THROTTLE);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Broadcast standard message to all users
	private class Broadcaster extends Thread {
		
		public void run() {
			
			while(true) {
				try {
					mResponses.add( new ClientMessage("", null) );
				} catch(Exception e) {
					e.printStackTrace();
					return;
				}
			
				try {
					Thread.sleep(3000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void handleDisconnect() {
		if(isConnected) {
			new PlayerDAO().update(mPlayer);
			isConnected = false;
			
			mServer.getDiscoveryProtocol().remove(this);
			mServer.disconnect(this);
			
			System.out.println(mPlayer.getName() + " has disconnected!");
		}
	}

	public Player getPlayer() {
		return mPlayer;
	}
	
	public void setPlayer(Player pPlayer) {
		mPlayer = pPlayer;
	}

	public Server getServer() {
		return mServer;
	}
	
	public boolean equals(Object pOther) {
		if(pOther instanceof Client) {
			Client that = (Client) pOther;
			return that.toString().equals(this.toString()); 
		}
		
		return false;
	}
	
	public String toString() {
		return mSocket.toString();
	}

}
