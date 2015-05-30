package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Marriage;
import be.spyproof.marriage.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Created by Spyproof on 1/05/2015.
 */
public class PlayerData
{
    private String name;
    private Gender gender;
    private Status status;
    private String partner;
    private boolean isSharedInvOpen;
    private boolean trustsPartner;
    private boolean homeSet;
    private boolean partnerChat;
    private Location location;
    private Long lastSeen;
    private Double balance;

    /**
     * @param name
     * @param gender
     * @param status
     * @param partner
     * @param isSharedInvOpen
     * @param homeSet
     * @param homeWorld
     * @param homeX
     * @param homeY
     * @param homeZ
     * @param homePitch
     * @param homeYaw
     * @param lastSeen
     * @param balance
     */
    public PlayerData(String name, Gender gender, Status status, String partner, boolean isSharedInvOpen, boolean homeSet, World homeWorld, int homeX, int homeY, int homeZ, Float homePitch, Float homeYaw, Long lastSeen, Double balance)
    {
        this.name = name.toLowerCase();
        this.gender = gender;
        this.status = status;
        this.partner = partner;
        this.isSharedInvOpen = isSharedInvOpen;
        this.partnerChat = false;
        this.homeSet = homeSet;
        if (homeWorld == null)
            homeWorld = Bukkit.getWorld("world");
        this.location = new Location(homeWorld, homeX, homeY, homeZ, homeYaw, homePitch);
        this.lastSeen = lastSeen;
        this.balance = balance;
    }

    /**
     * @param name
     * @param gender
     * @param status
     * @param partner
     * @param trustsPartner
     * @param homeSet
     * @param location
     * @param lastSeen
     * @param balance
     */
    public PlayerData(String name, Gender gender, Status status, String partner, boolean trustsPartner, boolean homeSet, Location location, Long lastSeen, Double balance)
    {
        this.name = name.toLowerCase();
        this.gender = gender;
        this.status = status;
        this.partner = partner.toLowerCase();
        this.trustsPartner = trustsPartner;
        this.homeSet = homeSet;
        this.partnerChat = false;
        if (location == null)
            this.location = new Location(Marriage.plugin.getServer().getWorld("world"), 0, 0, 0, 0F, 0F);
        else
            this.location = location;
        this.lastSeen = lastSeen;
        this.balance = balance;
        this.isSharedInvOpen = false;


    }

    /**
     * Getters
     */

    public String getName()
    {
        return name;
    }

    public Gender getGender()
    {
        return gender;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getPartner()
    {
        return partner;
    }

    public boolean isSharedInvOpen()
    {
        return isSharedInvOpen;
    }

    public boolean trustsPartner()
    {
        return trustsPartner;
    }

    public boolean isPartnerChatOn()
    {
        return this.partnerChat;
    }

    public boolean isHomeSet()
    {
        return homeSet;
    }

    public Location getHomeLoc()
    {
        return location;
    }

    public Long getLastSeen()
    {
        return lastSeen;
    }

    public Double getBalance()
    {
        return balance;
    }

    /**
     * Setters
     */

    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public void setPartner(String partner)
    {
        this.partner = partner.toLowerCase();
    }

    public void setIsSharedInvOpen(boolean trustsPartner)
    {
        this.isSharedInvOpen = trustsPartner;
    }

    public void setTrustsPartner(boolean trustsPartner)
    {
        this.trustsPartner = trustsPartner;
    }

    public void setPartnerChat(boolean partnerChat)
    {
        this.partnerChat = partnerChat;
    }

    public void setHomeSet(boolean homeSet)
    {
        this.homeSet = homeSet;
    }

    public void setHomeLocation(Location location)
    {
        if (location == null)
            this.location = new Location(Marriage.plugin.getServer().getWorld("world"), 0, 0, 0, 0F, 0F);
        else
            this.location = location;
    }

    public void setLastSeen(Long lastSeen)
    {
        this.lastSeen = lastSeen;
    }

    public void setBalance(Double balance)
    {
        this.balance = balance;
    }
}
