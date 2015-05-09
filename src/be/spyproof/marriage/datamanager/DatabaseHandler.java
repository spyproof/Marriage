package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Marriage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {
    private String host = "";
    private String database = "";
    private String port = "";
    private String table = "";
    private String user = "";
    private String password = "";
    private String dbTable = "";
    private String connectionString = "";
    private String selectPlayerString = "";
    private String insertPlayerString = "";
    private String updatePlayerString = "";
    private String deletePlayerString = "";
    
	public DatabaseHandler()
	{
    	try {
    		Class.forName("com.mysql.jdbc.Driver");
    	} catch (ClassNotFoundException e) {
    		System.out.println("Where is your MySQL JDBC Driver?");
    		e.printStackTrace();
    		return;
    	}
    	
    	this.host = Marriage.plugin.getConfig().getConfigurationSection("MYSQL").getString("host");
    	this.port = Marriage.plugin.getConfig().getConfigurationSection("MYSQL").getString("port");
    	this.database = Marriage.plugin.getConfig().getConfigurationSection("MYSQL").getString("database");
    	this.user = Marriage.plugin.getConfig().getConfigurationSection("MYSQL").getString("user");
    	this.password = Marriage.plugin.getConfig().getConfigurationSection("MYSQL").getString("password");
    	this.table = Marriage.plugin.getConfig().getConfigurationSection("MYSQL").getString("table");
    	this.dbTable = database+"."+table;
    	if (!this.port.equals(""))
    	{
    		this.port = ":"+this.port;
    	}
    	this.connectionString = "jdbc:mysql://"+host+port+"/feedback?user="+user+"&password="+password;
    	this.selectPlayerString = "SELECT * FROM "+dbTable+" WHERE name = ?";
    	this.insertPlayerString = "INSERT INTO "+dbTable+" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	this.updatePlayerString = "UPDATE "+dbTable+" SET gender = ?, status = ?, partner = ?, trusts_partner = ?, home_set = ?, home_x = ?, home_y = ?, home_z = ?, last_seen = ?, balance = ? WHERE name = ?";
    	this.deletePlayerString = "DELETE FROM "+dbTable+" WHERE name = ?";
	}

	public void savePlayer(PlayerData player)
	{
		PlayerData p = getPlayer(player.getName());
		if (p != null)
		{
			updatePlayer(player);
		}
		else
		{
			insertPlayer(player);
		}
	}
	
	public void deletePlayer(String name)
	{
		Object[] temp = { name };
		executeQuery(deletePlayerString, temp);
	}
	
	public void insertPlayer(PlayerData player)
	{
		Object[] temp = {
				player.getName(), 
				player.getGender().name(), 
				player.getStatus().name(),
				player.getPartner(),
				player.trustsPartner(),
				player.isHomeSet(),
				player.getHomeX(),
				player.getHomeY(),
				player.getHomeZ(),
				player.getLastSeen(),
				player.getBalance()};
		executeQuery(insertPlayerString, temp);
	}
	
	public void updatePlayer(PlayerData player)
	{
		Object[] temp = { 
				player.getGender().name(), 
				player.getStatus().name(),
				player.getPartner(),
				player.trustsPartner(),
				player.isHomeSet(),
				player.getHomeX(),
				player.getHomeY(),
				player.getHomeZ(),
				player.getLastSeen(),
				player.getBalance(),
				player.getName()};
		executeQuery(updatePlayerString, temp);
	}
	
	public PlayerData getPlayer(String name)
	{
		Object[] temp = {name};
		ResultSet data = executeQuery(selectPlayerString, temp);
		if (data != null)
		{
			try {
				data.next();
				return new PlayerData(
						data.getString("name"), 
						PlayerManager.genderFromString(data.getString("gender")), 
						PlayerManager.statusFromString(data.getString("status")), 
						data.getString("partner"), 
						data.getBoolean("trusts_partner"), 
						data.getBoolean("home_set"), 
						data.getInt("home_x"), 
						data.getInt("home_y"), 
						data.getInt("home_z"),
						data.getLong("last_seen"),
						data.getDouble("balance"));
			} catch (SQLException e) {
				//TODO log this exception
			}
			finally
			{
				try {
					data.close();
				} catch (SQLException e) {}
			}
		}
		return null;
	}
	
	private ResultSet executeQuery(String query, Object[] args)
	{
		Connection connection;
		try {
			connection = DriverManager.getConnection(connectionString);
			PreparedStatement prepStatement = connection.prepareStatement(query);
			for (int i = 0; i < args.length; i++)
			{
				if (args[i] instanceof String)
				{
					prepStatement.setString(i, (String)args[i]);
				}
				else if (args[i] instanceof Boolean)
				{
					prepStatement.setBoolean(i, (Boolean)args[i]);
				}
				else if (args[i] instanceof Integer)
				{
					prepStatement.setInt(i, (Integer)args[i]);
				}
				else if (args[i] instanceof Long)
				{
					prepStatement.setLong(i, (Long)args[i]);
				}
				else if (args[i] instanceof Double)
				{
					prepStatement.setDouble(i, (Double)args[i]);
				}
			}
			
			ResultSet data;
			if (query.equals(selectPlayerString))
			{
				
				data = prepStatement.executeQuery();
			}
			else
			{
				prepStatement.executeUpdate();
				data = null;
			}

			try {
				connection.close();
			} catch (SQLException e) {}
			
			return data;
		}
		catch (Exception e)
		{	
		}
		return null;
	}
	
}
