package nl.waldfelt.pandora.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nl.waldfelt.pandora.database.ConnectionFactory;


// Player Create/Retrieve/Update/Delete
public class PlayerDAO {
	private Connection mConnection;
	private PreparedStatement mStatement;
	
	public PlayerDAO() {
		
	}
	
	public Player retrieve(String pPlayerName) {
		String lQuery = "SELECT * FROM player WHERE name = ?";
		
		ResultSet lResultSet = null;
		Player lPlayer = null;
		
		try {
			mConnection = ConnectionFactory.getConnection();
			mStatement = mConnection.prepareStatement(lQuery);
			mStatement.setString(1, pPlayerName);
			lResultSet = mStatement.executeQuery();
			
			if(lResultSet.next()) {
				lPlayer = new Player();
				lPlayer.setName(lResultSet.getString("name"));
				lPlayer.setPosition(lResultSet.getInt("x"), lResultSet.getInt("y"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				lResultSet.close();
				mStatement.close();
				mConnection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return lPlayer;
	}
	
	public boolean update(Player pPlayer) {
		String query = "UPDATE player SET x = ?, y = ? WHERE name = ?";
		
		boolean isUpdated = false;
		
		try {
			mConnection = ConnectionFactory.getConnection();
			mStatement = mConnection.prepareStatement(query);

			mStatement.setInt(1, pPlayer.getX());
			mStatement.setInt(2, pPlayer.getY());
			mStatement.setString(3, pPlayer.getName());
			
			isUpdated = mStatement.execute();
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mStatement.close();
				mConnection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}		
		
		return isUpdated;
	}
	
	public Player create(Player pPlayer) {
		String query = "INSERT INTO player(name, x, y) VALUES(?, ?, ?)";
		
		boolean isInserted = false;
		
		try {
			mConnection = ConnectionFactory.getConnection();
			mStatement = mConnection.prepareStatement(query);

			mStatement.setString(1, pPlayer.getName());
			mStatement.setInt(2, pPlayer.getX());
			mStatement.setInt(3, pPlayer.getY());
			
			isInserted = mStatement.execute();
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mStatement.close();
				mConnection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}		
		
		return isInserted ? pPlayer : null;
	}
}
