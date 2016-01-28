package nl.waldfelt.pandora.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
	
	private static ConnectionFactory instance = new ConnectionFactory();

	// Database
	private String dbUrl = "jdbc:mysql://localhost:3306/";
	private String dbName = "pandoraserver";
	private String dbDriver = "com.mysql.jdbc.Driver";
	private String dbUser = "root";
	private String dbPass = "viking";
	
	private ConnectionFactory() {
		try {
			Class.forName(dbDriver);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Connection createConnection() {
		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection(dbUrl + dbName, dbUser, dbPass);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return connection;
	}
	
	public static Connection getConnection() {
		return instance.createConnection();
	}
}
