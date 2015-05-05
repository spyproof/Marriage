package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Status;
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

    public static void addPlayer(String name)
    {
        if (!isPlayerLoaded(name))
            playerData.add(new PlayerData(name, Gender.HIDDEN, Status.SINGLE, null, false));
    }

    public static void addPlayer(String name, Gender gender, Status status, String partner, boolean trustsPartner)
    {
        if (!isPlayerLoaded(name))
            playerData.add(new PlayerData(name, gender, status, partner, trustsPartner));
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
            localStorage.setGender(name, playerData.get(i).getGender());
            localStorage.setStatus(name, playerData.get(i).getStatus());
            localStorage.setPartner(name, playerData.get(i).getPartner());
            localStorage.setTrustsPartner(name, playerData.get(i).trustsPartner());
            localStorage.saveConfig();
        } catch (IllegalArgumentException e){
            return;
        }
    }

    public static void saveAllPlayers()
    {
        for (int i = 0; i < playerData.size(); i++)
        {
            localStorage.setGender(playerData.get(i).getName(), playerData.get(i).getGender());
            localStorage.setStatus(playerData.get(i).getName(), playerData.get(i).getStatus());
            localStorage.setPartner(playerData.get(i).getName(), playerData.get(i).getPartner());
            localStorage.setTrustsPartner(playerData.get(i).getName(), playerData.get(i).trustsPartner());
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

            addPlayer(name, gender, status, partner, trust);
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

    public static String getPrefix(String name)
    {
        name = name.toLowerCase();
        Status status = null;
        try {
            status = getStatus(name);
        } catch (IllegalArgumentException e) {
            return null;
        }

        //TODO http://en.wikipedia.org/wiki/Gender_symbol

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
            localStorage.saveConfig();
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
            localStorage.saveConfig();
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
            localStorage.saveConfig();
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
            localStorage.saveConfig();
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
            localStorage.saveConfig();
            return;
        }
        playerData.get(i).setTrustsPartner(trustsPartner);
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
