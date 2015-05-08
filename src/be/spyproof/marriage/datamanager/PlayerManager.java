package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Messages;
import be.spyproof.marriage.Status;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Spyproof on 1/05/2015.
 */
public class PlayerManager
{
    private static ArrayList<PlayerData> playerData = new ArrayList<PlayerData>();
    private static LocalStorage localStorage = new LocalStorage(new File("plugins/Marriage"), "players.yml");

    /**
     * TODO redo storing stuff
     * @ Player file
     * Name:        Local & Global
     * Gender:      Global
     * Status:      Global
     * Partner:     Global
     * Trusts:      Global
     * Chat:        Not stored
     * Last seen:   Local
     * socialspy:   Local or permission?
     *
     *
     * @ Partner file
     * Money:       Local
     * Home:        Local
     *
     */

    public static void addPlayer(String name)
    {
        if (!isPlayerLoaded(name))
            playerData.add(new PlayerData(name, Gender.HIDDEN, Status.SINGLE, "", false, false, 0, 0, 0));
    }

    public static void addPlayer(String name, Gender gender, Status status, String partner, boolean trustsPartner, boolean homeSet, int homeX, int homeY, int homeZ)
    {
        if (!isPlayerLoaded(name))
            playerData.add(new PlayerData(name, gender, status, partner, trustsPartner, homeSet, homeX, homeY, homeZ));
    }

    public static ArrayList<PlayerData> getLoadedPlayers()
    {
        return playerData;
    }

    /**
     * Saving and loading
     */

    public static void savePlayer(String name)
    {
        try{
            int i = getPlayerIndex(name);
            if (i == -1)
                return;
            localStorage.setGender(name, playerData.get(i).getGender());
            localStorage.setStatus(name, playerData.get(i).getStatus());
            localStorage.setPartner(name, playerData.get(i).getPartner());
            localStorage.setTrustsPartner(name, playerData.get(i).trustsPartner());
            localStorage.setHomeSet(name, playerData.get(i).isHomeSet());
            localStorage.setHomeX(name, playerData.get(i).getHomeX());
            localStorage.setHomeY(name, playerData.get(i).getHomeY());
            localStorage.setHomeZ(name, playerData.get(i).getHomeZ());
        } catch (IllegalArgumentException ignored){}
    }

    public static void saveAllPlayers()
    {
        for (PlayerData aPlayerData : playerData)
        {
            localStorage.setGender(aPlayerData.getName(), aPlayerData.getGender());
            localStorage.setStatus(aPlayerData.getName(), aPlayerData.getStatus());
            localStorage.setPartner(aPlayerData.getName(), aPlayerData.getPartner());
            localStorage.setTrustsPartner(aPlayerData.getName(), aPlayerData.trustsPartner());
            localStorage.setHomeSet(aPlayerData.getName(), aPlayerData.isHomeSet());
            localStorage.setHomeX(aPlayerData.getName(), aPlayerData.getHomeX());
            localStorage.setHomeY(aPlayerData.getName(), aPlayerData.getHomeY());
            localStorage.setHomeZ(aPlayerData.getName(), aPlayerData.getHomeZ());
            localStorage.saveConfig();
        }
    }

    public static void loadPlayer(String name)
    {
        name = name.toLowerCase();
        if (!isPlayerLoaded(name))
        {
            Gender gender = null;
            try {
                gender = localStorage.getGender(name);
            } catch (Exception e) {// = player not found
                addPlayer(name);
                return;
            }
            Status status = localStorage.getStatus(name);
            String partner = localStorage.getPartner(name);
            boolean trust = localStorage.trustsPartner(name);
            boolean homeSet = localStorage.isHomeSet(name);
            int homeX = localStorage.getHomeX(name);
            int homeY = localStorage.getHomeY(name);
            int homeZ = localStorage.getHomeZ(name);

            addPlayer(name, gender, status, partner, trust, homeSet, homeX, homeY, homeZ);
        }
    }

    public static void unloadPlayer(String name)
    {
        try {
            int i = getPlayerIndex(name);
            savePlayer(name);
            playerData.remove(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reload(String[] names)
    {
        playerData.clear();
        localStorage.loadConfig();
        for (int i = 0; i < names.length; i++)
            loadPlayer(names[i]);
    }

    public static void reload()
    {
        String[] players = new String[playerData.size()];
        for (int i = 0; i < playerData.size(); i++)
            players[i] = playerData.get(i).getName();
        reload(players);
    }

    /**
     * Read player data
     */

    public static Gender getGender(String name) throws IllegalArgumentException
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.getGender(name);

        return playerData.get(i).getGender();
    }

    public static Status getStatus(String name) throws IllegalArgumentException
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.getStatus(name);

        return playerData.get(i).getStatus();
    }

    public static String getPartner(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
        {
            try {
                return localStorage.getPartner(name);
            } catch (Exception ignored) {}
        }

        return playerData.get(i).getPartner();
    }

    public static boolean trustsPartner(String name) throws IllegalArgumentException
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.trustsPartner(name);

        return playerData.get(i).trustsPartner();
    }

    public static boolean isHomeSet(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.isHomeSet(name);

        return playerData.get(i).isHomeSet();
    }

    public static int getHomeX(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.getHomeX(name);

        return playerData.get(i).getHomeX();
    }

    public static int getHomeY(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.getHomeY(name);

        return playerData.get(i).getHomeY();
    }

    public static int getHomeZ(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);

        if (i == -1)
            return localStorage.getHomeZ(name);

        return playerData.get(i).getHomeZ();
    }

    public static long getLastOnline(String name)
    {
        Long time = localStorage.getLastOnline(name);

        if (time == 0)
            throw new IllegalArgumentException(Messages.notOnline.replace("{player}", name));

        return time;
    }

    public static boolean isMarried(String name)
    {
        Status status = null;
        try {
            status = getStatus(name);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (status.equals(Status.MARRIED_TO_PERSON))
            return true;
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            return true;
        else
            return status.equals(Status.MARRIED_TO_RIGHT_HAND);
    }

    public static boolean isPartnerChatOn(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i != -1)
            return playerData.get(i).isPartnerChatOn();

        return false;
    }

    public static String getPrefix(String name)
    {
        name = name.toLowerCase();
        Status status = null;
        try {
            status = getStatus(name);
        } catch (IllegalArgumentException e) {
            return null;
        }

        //TODO Make this a config?

        if (status.equals(Status.MARRIED_TO_PERSON))
            return "&d\u2665 &r";
        else if (status.equals(Status.MARRIED_TO_LEFT_HAND))
            return "&d\u2666 &r";
        else if (status.equals(Status.MARRIED_TO_RIGHT_HAND))
            return "&d\u2666 &r";
        else
            return null;

    }

    /**
     * Edit player data
     */

    public static void resetPlayer(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i == -1)
        {
            localStorage.removePlayer(name);
            return;
        }
        playerData.remove(i);
        addPlayer(name);
    }

    public static void setGender(String name, Gender gender)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i == -1)
        {
            localStorage.setGender(name, gender);
            return;
        }
        playerData.get(i).setGender(gender);
    }

    public static void setStatus(String name, Status status)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i == -1)
        {
            localStorage.setStatus(name, status);
            return;
        }
        playerData.get(i).setStatus(status);
    }

    public static void setPartner(String name, String partner)
    {
        name = name.toLowerCase();
        partner = partner.toLowerCase();
        int i = getPlayerIndex(name);
        if (i == -1)
        {
            localStorage.setPartner(name, partner);
            return;
        }
        playerData.get(i).setPartner(partner);
    }

    public static void setTrustsPartner(String name, boolean trustsPartner)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i == -1)
        {
            localStorage.setTrustsPartner(name, trustsPartner);
            return;
        }
        playerData.get(i).setTrustsPartner(trustsPartner);
    }

    public static void removeHome(String name)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i == -1)
        {
            localStorage.setHomeSet(name, false);
            return;
        }
        playerData.get(i).setHomeSet(false);
    }

    public static void setHome(String name, Location loc)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name); //TODO world, yawn, pitch?
        if (i == -1)
        {
            localStorage.setHomeSet(name, true);
            localStorage.setHomeX(name, loc.getBlockX());
            localStorage.setHomeY(name, loc.getBlockY());
            localStorage.setHomeZ(name, loc.getBlockZ());
            return;
        }
        playerData.get(i).setHomeSet(true);
        playerData.get(i).setHomeX(loc.getBlockX());
        playerData.get(i).setHomeY(loc.getBlockY());
        playerData.get(i).setHomeZ(loc.getBlockZ());
    }

    public static void setLastOnline(String name, long time)
    {
        localStorage.setLastOnline(name, time);
    }

    public static void setPartnerChat(String name, boolean chat)
    {
        name = name.toLowerCase();
        int i = getPlayerIndex(name);
        if (i != -1)
            playerData.get(i).setPartnerChat(chat);
    }

    /**
     * Private
     */

    private static int getPlayerIndex(String name)
    {
        name = name.toLowerCase();
        for (int i = 0; i < playerData.size(); i++)
            if (playerData.get(i).getName().equals(name))
                return i;

        return -1;
    }

    private static boolean isPlayerLoaded(String name)
    {
        name = name.toLowerCase();
        for (int i = 0; i < playerData.size(); i++)
            if (playerData.get(i).getName().equals(name))
                return true;

        return false;
    }
}
