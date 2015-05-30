package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Gender;
import be.spyproof.marriage.handlers.Messages;
import be.spyproof.marriage.Status;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Idlehumor
 */

public class DatabaseHandler {
    private String host = "";
    private String database = "";
    private String port = "";
    private String table = "";
    private String serversTable = "";
    private String user = "";
    private String password = "";
    private String serverName = "";
    private String connectionString = "";
    private String selectPlayerString = "";
    private String selectPlayerServerString = "";
    private String selectPlayerDataString = "";
    private String insertPlayerString = "";
    private String insertPlayerServerString = "";
    private String updatePlayerString = "";
    private String updatePlayerServerString = "";
    private String updatePlayerEveryServerString = "";
    private String deletePlayerString = "";
    private String deletePlayerServerString = "";
    private Connection connection;
    
	public DatabaseHandler()
	{
    	try {
    		Class.forName("com.mysql.jdbc.Driver");
    	} catch (ClassNotFoundException e) {
    		System.out.println("Where is your MySQL JDBC Driver?");
    		e.printStackTrace();
    		return;
    	}
    	
    	this.host = Marriage.config.getString("host");
    	this.port = Marriage.config.getString("port");
    	this.database = Marriage.config.getString("database");
    	this.user = Marriage.config.getString("user");
    	this.password = Marriage.config.getString("password");
    	this.table = Marriage.config.getString("table");
    	this.serversTable = Marriage.config.getString("servers_table");
    	this.serverName = Marriage.config.getString("server_name");
    	if (this.port != null && !this.port.equals("")) {
    		this.port = ":"+this.port;
    	}
    	else {
    		this.port = "";
    	}
    	this.connectionString = "jdbc:mysql://"+this.host+this.port+"/"+this.database;
    	this.selectPlayerString = "SELECT * FROM "+table+" WHERE name = ?";
    	this.selectPlayerDataString = "SELECT "+table+".name, gender, status, partner, trusts_partner, home_set, home_world, home_x, home_y, home_z, home_pitch, home_yaw, last_seen, balance FROM "+table+","+serversTable+" WHERE "+table+".name = ? AND "+serversTable+".server = ? GROUP BY "+table+".name";
    	this.selectPlayerServerString = "SELECT * FROM "+serversTable+" WHERE name = ? AND server = ?";
    	this.insertPlayerString = "INSERT INTO "+table+" SET name = ?, gender = ?, status = ?, partner = ?, trusts_partner = ?";
        this.insertPlayerServerString = "INSERT INTO "+serversTable+" SET name = ?, server = ?, home_set = ?, home_world = ?, home_x = ?, home_y = ?, home_z = ?, home_pitch = ?, home_yaw = ?, last_seen = ?, balance = ?";
        this.updatePlayerString = "UPDATE "+table+" SET gender = ?, status = ?, partner = ?, trusts_partner = ? WHERE name = ?";
        this.updatePlayerServerString = "UPDATE "+serversTable+" SET home_set = ?, home_world = ?, home_x = ?, home_y = ?, home_z = ?, home_pitch = ?, home_yaw = ?, last_seen = ?, balance = ? WHERE name = ? AND server = ?";
        this.updatePlayerEveryServerString = "UPDATE "+serversTable+" SET home_set = ?, home_world = ?, home_x = ?, home_y = ?, home_z = ?, home_pitch = ?, home_yaw = ?, balance = ? WHERE name = ?";
        this.deletePlayerString = "DELETE FROM "+table+" WHERE name = ?";
    	this.deletePlayerServerString = "DELETE FROM "+serversTable+" WHERE name = ?";
    	
		try {
            connection = DriverManager.getConnection(connectionString, user, password);
			PreparedStatement prepStatement = connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS "+table+
				"(name VARCHAR(32),"+
				"gender VARCHAR(16),"+
				"status VARCHAR(32),"+
				"partner VARCHAR(32),"+
				"trusts_partner BOOLEAN,"+
				"PRIMARY KEY (name))");
			prepStatement.executeUpdate();
			prepStatement = connection.prepareStatement(
	            "CREATE TABLE IF NOT EXISTS "+serversTable+
	            "(name VARCHAR(32) NOT NULL,"+
	            "server VARCHAR(64),"+
	            "home_set BOOLEAN,"+
                "home_world VARCHAR(32),"+
	            "home_x INTEGER,"+
	            "home_y INTEGER,"+
	            "home_z INTEGER,"+
	            "home_pitch FLOAT,"+
	            "home_yaw FLOAT,"+
	            "last_seen LONG,"+
	            "balance DOUBLE,"+
		        "PRIMARY KEY (name, server))");
			prepStatement.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}    	
	}

    public void closeDB()
    {
        if (connection != null)
            try {
                if (!connection.isClosed())
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

	public void savePlayer(PlayerData player)
	{
		Messages.sendDebugInfo("DBDebug: saving " + player.getName());
		PlayerData p = getPlayer(player.getName());
		if (p != null) {
			updatePlayer(player);
		}
		else {
			insertPlayer(player);
		}
	}
	
	public void deletePlayer(String name)
	{
        Messages.sendDebugInfo("DBDebug: Deleting " + name);
		executeQuery(deletePlayerString, new Object[]{name.toLowerCase()});
		executeQuery(deletePlayerServerString, new Object[]{ name.toLowerCase()});
	}
	
	public void insertPlayer(PlayerData player)
	{
        Messages.sendDebugInfo("DBDebug: inserting as a new player");
		ResultSet data = executeQuery(selectPlayerString, new Object[]{player.getName().toLowerCase()});
		try {
			if (data != null && !data.next())
			{
                Messages.sendDebugInfo("DBDebug: was already registered globaly");
				Object[] temp = {
						player.getName().toLowerCase(), 
						player.getGender().toString(), 
						player.getStatus().toString(),
						player.getPartner(),
                        player.trustsPartner()};
				executeQuery(insertPlayerString, temp);
			}
			else
			{
                Messages.sendDebugInfo("DBDebug: was not registered globaly");
				player.setGender(Gender.fromString(data.getString("gender")));
				player.setStatus(Status.fromString(data.getString("status")));
				player.setPartner(data.getString("partner"));
				player.setTrustsPartner(data.getBoolean("trusts_partner"));
			}
		} catch (SQLException e) { e.printStackTrace(); }
		finally
		{
			try {
				if (data != null)
				{
					data.close();
				}
			} catch (SQLException e) { e.printStackTrace(); }
		}
		
		data = executeQuery(selectPlayerServerString, new Object[]{player.getName().toLowerCase(), this.serverName});

		try {
			if (data == null || !data.next())
			{//"INSERT INTO "+serversTable+" SET name = ?, server = ?, home_set = ?, home_world = ?, home_x = ?, home_y = ?, home_z = ?, home_pitch = ?, home_yaw = ?, last_seen = ?, balance = ?";

                Messages.sendDebugInfo("DBDebug: is new to this server");
				Object[] temp2 = {
                        player.getName().toLowerCase(),
                        this.serverName,
						player.isHomeSet(),
                        player.getHomeLoc().getWorld() == null ? "world" : player.getHomeLoc().getWorld().getName(),
						player.getHomeLoc().getBlockX(),
						player.getHomeLoc().getBlockY(),
						player.getHomeLoc().getBlockZ(),
						player.getHomeLoc().getPitch(), player.getHomeLoc().getYaw(),
						player.getLastSeen(),
						player.getBalance()};
				executeQuery(insertPlayerServerString, temp2);
			}
		} catch (SQLException e) { e.printStackTrace(); }
		finally
		{
			try {
				if (data != null)
				{
					data.close();
				}
			} catch (SQLException e) { e.printStackTrace(); }
		}
		
	}
	
	public void updatePlayer(PlayerData player)
	{
        Messages.sendDebugInfo("DBDebug: updating an existing player");
		Object[] temp = { 
				player.getGender().toString(), 
				player.getStatus().toString(),
				player.getPartner(),
				player.trustsPartner(),
				player.getName().toLowerCase()};
		executeQuery(updatePlayerString, temp);

        Object[] selectServer = {
                player.getName().toLowerCase(),
                this.serverName
        };

        boolean doServerInsert = false;
        ResultSet data = executeQuery(selectPlayerServerString, selectServer);
        try {
            if (data == null || !data.next()) {
                doServerInsert = true;
            }
        } catch (SQLException e) {
            doServerInsert = true;
            e.printStackTrace();
        }finally
        {
            try
            {
                data.close();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        if (doServerInsert) {
            Messages.sendDebugInfo("DBDebug: is new to this server");
            Object[] temp2 = {
                    player.getName().toLowerCase(),
                    this.serverName,
                    player.isHomeSet(),
                    player.getHomeLoc().getWorld() == null ? "world" : player.getHomeLoc().getWorld().getName(),
                    player.getHomeLoc().getBlockX(),
                    player.getHomeLoc().getBlockY(),
                    player.getHomeLoc().getBlockZ(),
                    player.getHomeLoc().getPitch(), player.getHomeLoc().getYaw(),
                    player.getLastSeen(),
                    player.getBalance()};
            executeQuery(insertPlayerServerString, temp2);
        }else{
            Object[] temp2 = {
                    player.isHomeSet(),
                    player.getHomeLoc().getWorld() == null ? "world" : player.getHomeLoc().getWorld().getName(),
                    player.getHomeLoc().getBlockX(),
                    player.getHomeLoc().getBlockY(),
                    player.getHomeLoc().getBlockZ(),
                    player.getHomeLoc().getPitch(),
                    player.getHomeLoc().getYaw(),
                    player.getLastSeen(),
                    player.getBalance(),
                    player.getName().toLowerCase(),
                    this.serverName};
            executeQuery(updatePlayerServerString, temp2);
        }
	}

	public void divorcePlayer(PlayerData player)
    {
        PlayerData playerReset = new PlayerData(player.getName(), player.getGender(), Status.DIVORCED, "", false, false, null, System.currentTimeMillis(), 0D);
        Object[] o = new Object[]{
                playerReset.isHomeSet(),
                playerReset.getHomeLoc().getWorld().getName(),
                playerReset.getHomeLoc().getBlockX(),
                playerReset.getHomeLoc().getBlockY(),
                playerReset.getHomeLoc().getBlockZ(),
                playerReset.getHomeLoc().getPitch(),
                playerReset.getHomeLoc().getYaw(),
                playerReset.getBalance(),
                playerReset.getName()
        };
        executeQuery(updatePlayerEveryServerString, o);

        o[o.length-1] = player.getPartner();
        executeQuery(updatePlayerEveryServerString, o);
    }
	
	public PlayerData getPlayer(String name)
	{
        Messages.sendDebugInfo("DBDebug: getting player info of " +name);
		Object[] temp = {name.toLowerCase(), this.serverName};
		ResultSet data = executeQuery(selectPlayerDataString, temp);
		try {
			if (data != null && data.next())
			{
                Messages.sendDebugInfo("DBDebug: player found");
				return new PlayerData(
						data.getString("name"), 
						Gender.fromString(data.getString("gender")), 
						Status.fromString(data.getString("status")), 
						data.getString("partner"),
						data.getBoolean("trusts_partner"),
						data.getBoolean("home_set"),
                        Bukkit.getWorld(data.getString("home_world")),
						data.getInt("home_x"), 
						data.getInt("home_y"), 
						data.getInt("home_z"),
						data.getFloat("home_pitch"),
						data.getFloat("home_yaw"),
						data.getLong("last_seen"),
						data.getDouble("balance"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				if (data != null) {
					data.close();
				}
			} catch (SQLException e) { e.printStackTrace(); }
		}
		return null;
	}
	
	private ResultSet executeQuery(String query, Object[] args)
	{
		try {
			PreparedStatement prepStatement = connection.prepareStatement(query);
            Messages.sendDebugInfo(query);
			for (int i = 0; i < args.length; i++)
			{
				if (args[i] instanceof String)
				{
					prepStatement.setString(i+1, (String)args[i]);
				}
				else if (args[i] instanceof Boolean)
				{
					prepStatement.setBoolean(i+1, (Boolean)args[i]);
				}
				else if (args[i] instanceof Integer)
				{
					prepStatement.setInt(i+1, (Integer)args[i]);
				}
				else if (args[i] instanceof Long)
				{
					prepStatement.setLong(i+1, (Long)args[i]);
				}
				else if (args[i] instanceof Double)
				{
					prepStatement.setDouble(i+1, (Double)args[i]);
				}
				else if (args[i] instanceof Float)
				{
					prepStatement.setFloat(i+1, (Float)args[i]);
				}
			}

            Messages.sendDebugInfo(prepStatement.toString());
			
			ResultSet data;
			if (query.equals(selectPlayerString) || query.equals(selectPlayerDataString) || query.equals(selectPlayerServerString))
			{
				data = prepStatement.executeQuery();
			}
			else
			{
				prepStatement.executeUpdate();
				data = null;
			}
			
			return data;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
}
