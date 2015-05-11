package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Status;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Spyproof on 1/05/2015.
 */
public class PlayerManager
{
	private DatabaseHandler database;
    private Map<String, PlayerData> playerData = new HashMap<String, PlayerData>();
    private Map<String, PlayerData> partnerData = new HashMap<String, PlayerData>();
    
    /**
     * EVERYTHING STORED IN MYSQL (except for partnerchat per request)
     */

    public PlayerManager()
    {
    	database = new DatabaseHandler();
    }
    
    public void addPlayer(String name)
    {    	
    	name = name.toLowerCase();
    	if (partnerData.containsKey(name))
    	{
    		unloadPlayer(name);
    	}
    	
    	PlayerData player = database.getPlayer(name);
    	if (player != null)
    	{
    		playerData.put(name, player);
    		String partnerName = playerData.get(name).getPartner();
    		if (!partnerName.equals("") && !playerData.containsKey(partnerName))
    		{
    	    	PlayerData partner = database.getPlayer(partnerName);
    	    	if (partner != null)
    	    	{
    	    		partnerData.put(partnerName, partner);
    	    	}
    		}
    	}
    	else if (!playerData.containsKey(name))
    	{
    		player = new PlayerData(name, Gender.HIDDEN, Status.SINGLE, "", false, false, 0, 0, 0, 0.0F, 0.0F, System.currentTimeMillis()/1000L, 0.0);
            playerData.put(name, player);
    		database.insertPlayer(player);
    	}
    }

    public ArrayList<PlayerData> getLoadedPlayers()
    {
        return (ArrayList<PlayerData>) playerData.values();
    }
    
    public ArrayList<PlayerData> getLoadedPartners()
    {
    	return (ArrayList<PlayerData>) partnerData.values();
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
    	
    	if (partnerData.containsKey(name))
    	{
    		database.savePlayer(partnerData.get(name));
    	}    	
    }

    public void saveAllPlayers()
    {
        for (PlayerData aPlayerData : playerData.values())
        {
            database.savePlayer(aPlayerData);
        }
        for (PlayerData aPartnerData : partnerData.values())
        {
            database.savePlayer(aPartnerData);
        }
    }

    public void unloadPlayer(String name)
    {
    	name = name.toLowerCase();
    	if (partnerData.containsKey(name))
    	{
    		savePlayer(name);
    		partnerData.remove(name);
    	}
    	else
    	{
	    	if (partnerData.containsKey(playerData.get(name).getPartner()))
	    	{
	    		savePlayer(playerData.get(name).getPartner());
	    		partnerData.remove(playerData.get(name).getPartner());
	    	}
	    	
	        playerData.get(name).setLastSeen(System.currentTimeMillis() / 1000L);
	        savePlayer(name);
	        playerData.remove(name);
    	}
    }

    public void reload(String[] names)
    {
        playerData.clear();
        partnerData.clear();
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
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getBalance();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getBalance();
    	}
    	else
    	{
    		return null;
    	}
    }
    
    public Gender getGender(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getGender();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getGender();
    	}
    	else
    	{
    		return null;
    	}
    }

    public Status getStatus(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getStatus();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getStatus();
    	}
    	else
    	{
    		return null;
    	}
    }

    public String getPartner(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getPartner();
    	}
    	else
    	{
    		return "";
    	}
    }

    public boolean trustsPartner(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).trustsPartner();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).trustsPartner();
    	}
    	else
    	{
    		return false;
    	}
    }

    public boolean isHomeSet(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).isHomeSet();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).isHomeSet();
    	}
    	else
    	{
    		return false;
    	}
    }

    public int getHomeX(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getHomeX();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getHomeX();
    	}
    	else
    	{
    		return 0;
    	}
    }

    public int getHomeY(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getHomeY();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getHomeY();
    	}
    	else
    	{
    		return 0;
    	}
    }

    public int getHomeZ(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getHomeZ();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getHomeZ();
    	}
    	else
    	{
    		return 0;
    	}
    }

    public Float getHomePitch(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getHomePitch();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getHomePitch();
    	}
    	else
    	{
    		return 0.0F;
    	}
    }

    public Float getHomeYaw(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getHomeYaw();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getHomeYaw();
    	}
    	else
    	{
    		return 0.0F;
    	}
    }

    public long getLastOnline(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		return playerData.get(name).getLastSeen();
    	}
    	else if (partnerData.containsKey(name))
    	{
    		return partnerData.get(name).getLastSeen();
    	}
    	else
    	{
    		Messages.notOnline.replace("{player}", name);
    		return 0;
    	}
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

    /**
     * Edit player data
     */

    public void resetPlayer(String name)
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
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setGender(gender);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setGender(gender);
    	}
    }

    public void setStatus(String name, Status status)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setStatus(status);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setStatus(status);
    	}
    }

    public void setPartner(String name, String partner)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setPartner(partner);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setPartner(partner);
    	}
    }

    public void setTrustsPartner(String name, boolean trustsPartner)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setTrustsPartner(trustsPartner);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setTrustsPartner(trustsPartner);
    	}
    }

    public void setBalance(String name, Double balance)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setBalance(balance);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setBalance(balance);
    	}
    }

    public void removeHome(String name)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setHomeSet(false);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setHomeSet(false);
    	}
    }

    public void setHome(String name, Location loc)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setHomeSet(true);
    		playerData.get(name).setHomeX(loc.getBlockX());
    		playerData.get(name).setHomeY(loc.getBlockY());
    		playerData.get(name).setHomeZ(loc.getBlockZ());
    		playerData.get(name).setHomePitch(loc.getPitch());
    		playerData.get(name).setHomeYaw(loc.getYaw());
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setHomeSet(true);
    		partnerData.get(name).setHomeX(loc.getBlockX());
    		partnerData.get(name).setHomeY(loc.getBlockY());
    		partnerData.get(name).setHomeZ(loc.getBlockZ());
    		partnerData.get(name).setHomePitch(loc.getPitch());
    		partnerData.get(name).setHomeYaw(loc.getYaw());
    	}
    }

    public void setLastOnline(String name, long time)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setLastSeen(time);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setLastSeen(time);
    	}
    }

    public void setPartnerChat(String name, boolean chat)
    {
    	name = name.toLowerCase();
    	if (playerData.containsKey(name))
    	{
    		playerData.get(name).setPartnerChat(chat);
    	}
    	else if (partnerData.containsKey(name))
    	{
    		partnerData.get(name).setPartnerChat(chat);
    	}
    }
}
