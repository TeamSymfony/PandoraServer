package nl.waldfelt.pandora.message;

import java.util.ArrayList;

import nl.waldfelt.pandora.Client;
import nl.waldfelt.pandora.player.Player;
import nl.waldfelt.pandora.player.PlayerDAO;

public class MessageHandler extends Thread {
	
	public static final long HANDLER_THROTTLE = 5;
	
	private Client mClient;
	private ArrayList<ServerMessage> mTasks;
	
	public MessageHandler(Client pClient) {
		mClient = pClient;
		
		mTasks = new ArrayList<ServerMessage>();
		
	}
	
	private ClientMessage getReply(ServerMessage pServerMessage) {
		String pCommand = pServerMessage.getCommand();
		Object pData = pServerMessage.getData();
		
		// Process client's request to sync
		if(pCommand.equals("discovery.sync")) {
			mClient.getServer().getDiscoveryProtocol().sync(mClient);
			return null;
		}
		
		if(pCommand.equals("discovery.update")) {
			if(pData instanceof float[][]) {
				float[][] lPath = (float[][]) pData;
				mClient.getPlayer().setPath(lPath);
				
				mClient.getServer().getDiscoveryProtocol().update(mClient);	
			}
			return null;
		}
		
		// Get client's phone model
		if(pCommand.equals("identify")) {
			Player lPlayer = new PlayerDAO().retrieve(pData.toString());
			
			if(lPlayer == null) {
				Player lNewPlayer = new Player();
				lNewPlayer.setName(pData.toString());
				lPlayer = new PlayerDAO().create(lNewPlayer);
			}
			
			mClient.setPlayer(lPlayer);
			
			System.out.println("Client is now known as: " + lPlayer.getName());
			mClient.getServer().getDiscoveryProtocol().add(mClient);
			
			return new ClientMessage("loadPlayerData", lPlayer);
		}
		
		// Get client position update
		if(pCommand.equals("move"))	{
			
			if(pData instanceof int[]) {
				int[] lPosition = (int[]) pData;
				mClient.getPlayer().setPosition(lPosition[0], lPosition[1]);
			}
			return null;
		}
		
		// Receive ping from client and respond with a pong!
		if(pCommand.equals("ping")) {
			System.out.println("ping?");
			return new ClientMessage(pCommand, null);
		}
		
		return null;
	}

	public void addTask(ServerMessage mServerMessage) {
		mTasks.add(mServerMessage);
	}
	
	public void run() {
		
		while(true) {
			
			if(!mTasks.isEmpty()) {
				ClientMessage response = getReply(mTasks.remove(0));
				
				if(response != null)
					mClient.addResponse(response);
			}
			
			try {
				Thread.sleep(HANDLER_THROTTLE);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
}
