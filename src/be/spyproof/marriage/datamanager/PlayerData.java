package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Status;

/**
 * Created by Spyproof on 1/05/2015.
 */
public class PlayerData
{
    private String name;
    private Gender gender;
    private Status status;
    private String partner;
    private boolean trustsPartner;
    private boolean homeSet;
    private boolean partnerChat;
    private int homeX;
    private int homeY;
    private int homeZ;
    private Long lastSeen;
    private Double balance;

    public PlayerData(String name, Gender gender, Status status, String partner, boolean trustsPartner, boolean homeSet, int homeX, int homeY, int homeZ, Long lastSeen, Double balance)
    {
        this.name = name.toLowerCase();
        this.gender = gender;
        this.status = status;
        this.partner = partner;
        this.trustsPartner = trustsPartner;
        this.partnerChat = false;
        this.homeSet = homeSet;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.lastSeen = lastSeen;
        this.balance = balance;
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

    public int getHomeX()
    {
        return homeX;
    }

    public int getHomeY()
    {
        return homeY;
    }

    public int getHomeZ()
    {
        return homeZ;
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
        this.partner = partner;
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

    public void setHomeX(int homeX)
    {
        this.homeX = homeX;
    }

    public void setHomeY(int homeY)
    {
        this.homeY = homeY;
    }

    public void setHomeZ(int homeZ)
    {
        this.homeZ = homeZ;
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
