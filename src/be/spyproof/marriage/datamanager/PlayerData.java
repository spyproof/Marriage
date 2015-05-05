package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Status;
import org.bukkit.ChatColor;

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

    public PlayerData(String name, Gender gender, Status status, String partner, boolean trustsPartner)
    {
        this.name = name.toLowerCase();
        this.gender = gender;
        this.status = status;
        this.partner = partner;
        this.trustsPartner = trustsPartner;
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
}
