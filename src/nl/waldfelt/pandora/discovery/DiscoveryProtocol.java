package nl.waldfelt.pandora.discovery;

import java.util.ArrayList;
import java.util.HashMap;

import nl.waldfelt.pandora.Client;
import nl.waldfelt.pandora.message.ClientMessage;

public class DiscoveryProtocol {
	HashMap<String, ArrayList<Client>> mCommunities;
	
	public DiscoveryProtocol() {
		mCommunities = new HashMap<String, ArrayList<Client>>();
		
		mCommunities.put("", new ArrayList<Client>());
	}
	
	public void add(Client pClient) {
		// Retrieve list of players in that location
		ArrayList<Client> lSubCommunity = mCommunities.get(pClient.getPlayer().getLocation());
		
		// If list doesn't exist, create list
		if(lSubCommunity == null) {
			lSubCommunity = new ArrayList<Client>();
		}
		
		// Notify all other clients of new client
		for(Client lClient : lSubCommunity) {
			lClient.addResponse( new ClientMessage("discovery.add", pClient.getPlayer()) );
		}
		
		// Add client to the subcommunity
		lSubCommunity.add(pClient);
		 
		// Update list
		mCommunities.put(pClient.getPlayer().getLocation(), lSubCommunity);		
	}
	
	public void remove(Client pClient) {
		// Retrieve list of players in that location
		ArrayList<Client> lSubCommunity = mCommunities.get(pClient.getPlayer().getLocation());
		
		// If list doesn't exist, create list
		if(lSubCommunity == null) {
			lSubCommunity = new ArrayList<Client>();
		}
		
		// Notify all other clients of new client
		for(Client lClient : lSubCommunity) {
			lClient.addResponse( new ClientMessage("discovery.remove", pClient.getPlayer()) );
		}
		
		// Add client to the subcommunity
		lSubCommunity.remove(pClient);
		
		// Update list
		mCommunities.put(pClient.getPlayer().getLocation(), lSubCommunity);		
	}
	
	public void sync(Client pClient) {
		// Retrieve list of players in that location
		ArrayList<Client> lSubCommunity = mCommunities.get(pClient.getPlayer().getLocation());

		
		// Notify new client of all other clients
		for(Client lClient : lSubCommunity) {
			if(!lClient.equals(pClient)) {
				pClient.addResponse( new ClientMessage("discovery.add", lClient.getPlayer()) );
			}
		}
	}

	public void update(Client pClient) {
		// Retrieve list of players in that location
		ArrayList<Client> lSubCommunity = mCommunities.get(pClient.getPlayer().getLocation());
		
		for(Client lClient : lSubCommunity) {
			if(!lClient.equals(pClient)) {
				lClient.addResponse( new ClientMessage("discovery.update", pClient.getPlayer()) );
			}
		}
		
	}
}
