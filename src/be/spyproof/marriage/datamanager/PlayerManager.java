package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Status;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Spyproof on 31/03/2015.
 * Contributors: Idlehumor
 */
public class PlayerManager
{
	private DatabaseHandler database;
    private Map<String, PlayerData> playerData = new HashMap<String, PlayerData>();

    public PlayerManager()
    {
    	database = new DatabaseHandler();
    }

    public void addPlayer(String name)
    {    	
    	name = name.toLowerCase();
    	
    	PlayerData player = database.getPlayer(name);
    	if (player != null)
    	{
    		playerData.put(name, player);
    	}
    	else if (!playerData.containsKey(name))
    	{
    		player = new PlayerData(name, Gender.HIDDEN, Status.SINGLE, "", false, false, null, System.currentTimeMillis(), 0.0);
            playerData.put(name, player);
    		database.insertPlayer(player);
    	}
    }

    public ArrayList<PlayerData> getLoadedPlayers()
    {
        return (ArrayList<PlayerData>) playerData.values();
    }

    /**
     * Saving and loading
     */

    public void savePlayer(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		database.savePlayer(playerData.get(name));
    	}
    }

    public void saveAllPlayers()
    {
        for (PlayerData aPlayerData : playerData.values())
        {
            database.savePlayer(aPlayerData);
        }
    }

    public void unloadPlayer(String name)
    {
        savePlayer(name);
        playerData.remove(name);
    }

    public void closeDB()
    {
        this.database.closeDB();
    }

    public void reload(String[] names)
    {
        playerData.clear();
        for (int i = 0; i < names.length; i++)
        {
            addPlayer(names[i].toLowerCase());  
        }
    }

    public void reload()
    {
        reload(playerData.keySet().toArray(new String[playerData.size()]));
    }

    /**
     * Read player data
     */

    public Double getBalance(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return null;
        else
            return playerData.getBalance();
    }
    
    public Gender getGender(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return null;
        else
            return playerData.getGender();
    }

    public Status getStatus(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return null;
        else
            return playerData.getStatus();
    }

    public String getPartner(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return null;
        else
            return playerData.getPartner();
    }

    public boolean trustsPartner(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return false;
        else
            return playerData.trustsPartner();
    }

    public boolean isHomeSet(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return false;
        else
            return playerData.isHomeSet();
    }

    public Location getHomeLoc(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return null;
        else
            return playerData.getHomeLoc();
    }

    public long getLastOnline(String name)
    {
        PlayerData playerData = getPlayerData(name);
        if (playerData == null)
            return 0L;
        else
            return playerData.getLastSeen();
    }

    public boolean isMarried(String name)
    {    	
    	name = name.toLowerCase();
        Status status = getStatus(name);

        if (status.equals(Status.MARRIED_TO_PERSON))
            return true;
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            return true;
        else
            return status.equals(Status.MARRIED_TO_RIGHT_HAND);
    }

    public boolean isPartnerChatOn(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).isPartnerChatOn();
    	}
    	else
    	{
    		return false;
    	}
    }

    private PlayerData getPlayerData(String name)
    {
        name = name.toLowerCase();
        if (playerData.containsKey(name))
        {
            return playerData.get(name);
        }else
        {
            return database.getPlayer(name);
        }

    }

    /**
     * Edit player data
     */

    public void resetPlayer(String name) //TODO what about the partner?
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		database.deletePlayer(name);
            playerData.remove(name);
            addPlayer(name);
    	}
    }

    public void setGender(String name, Gender gender)
    {
        PlayerData player = getPlayerData(name);
        player.setGender(gender);
        updatePlayerData(player);
    }

    public void setStatus(String name, Status status)
    {
        PlayerData player = getPlayerData(name);
        player.setStatus(status);
        updatePlayerData(player);
    }

    public void setPartner(String name, String partner)
    {
        PlayerData player = getPlayerData(name);
        player.setPartner(partner);
        updatePlayerData(player);
    }

    public void setTrustsPartner(String name, boolean trustsPartner)
    {
        PlayerData player = getPlayerData(name);
        player.setTrustsPartner(trustsPartner);
        updatePlayerData(player);
    }

    public void setBalance(String name, Double balance)
    {
        PlayerData player = getPlayerData(name);
        player.setBalance(balance);
        updatePlayerData(player);

        if (player.getStatus().equals(Status.MARRIED_TO_PERSON))
        {
            PlayerData partner = getPlayerData(player.getPartner());
            partner.setBalance(balance);
            updatePlayerData(partner);
        }
    }

    public void removeHome(String name)
    {
        PlayerData player = getPlayerData(name);
        player.setHomeSet(false);
        player.setHomeLocation(null);
        updatePlayerData(player);

        if (player.getStatus().equals(Status.MARRIED_TO_PERSON))
        {
            PlayerData partner = getPlayerData(player.getPartner());
            partner.setHomeSet(false);
            partner.setHomeLocation(null);
            updatePlayerData(partner);
        }
    }

    public void setHome(String name, Location loc)
    {
        PlayerData player = getPlayerData(name);
        player.setHomeSet(true);
        player.setHomeLocation(loc);
        updatePlayerData(player);

        if (player.getStatus().equals(Status.MARRIED_TO_PERSON))
        {
            PlayerData partner = getPlayerData(player.getPartner());
            partner.setHomeSet(true);
            partner.setHomeLocation(loc);
            updatePlayerData(partner);
        }
    }

    public void setLastOnline(String name, long time)
    {
        PlayerData player = getPlayerData(name);
        player.setLastSeen(time);
        updatePlayerData(player);
    }

    public void setPartnerChat(String name, boolean chat)
    {
        PlayerData player = getPlayerData(name);
        player.setPartnerChat(chat);
        updatePlayerData(player);
    }

    private void updatePlayerData(PlayerData player)
    {
        if (playerData.containsKey(player.getName()))
            playerData.remove(player.getName());
        playerData.put(player.getName(), player);
    }
}
