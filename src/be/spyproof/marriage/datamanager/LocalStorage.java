package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.Gender;
import be.spyproof.marriage.Status;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by Spyproof on 4/05/2015.
 */
public class LocalStorage
{

    private FileConfiguration config;
    private File playerConfigFile;

    public LocalStorage(File file, String fileName)
    {
        this.playerConfigFile = new File(file, fileName);
        this.config = YamlConfiguration.loadConfiguration(this.playerConfigFile);
    }

    /**
     * Saving
     */

    public void saveConfig()
    {
        try {
            this.config.save(this.playerConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read player data
     */

    public Gender getGender(String name)
    {
        name = name.toLowerCase();

        String gender = this.config.getString(name + ".gender");
        if (gender != null)
            return Gender.fromString(gender);
        else
            throw new IllegalArgumentException("Could not find " + name);
    }

    public Status getStatus(String name)
    {
        name = name.toLowerCase();

        String status = this.config.getString(name + ".status");
        if (status != null)
            return Status.fromString(status);
        else
            throw new IllegalArgumentException("Could not find " + name);

    }

    public String getPartner(String name)
    {
        name = name.toLowerCase();

        String partner = this.config.getString(name + ".partner");
        if (partner != null)
            return partner;
        else
            throw new IllegalArgumentException("Could not find " + name);
    }

    public boolean trustsPartner(String name)
    {
        name = name.toLowerCase();
        return this.config.getBoolean(name + ".trusts partner");
    }

    /**
     * Edit player data
     */

    public void removePlayer(String name)
    {
        name = name.toLowerCase();
        this.config.set(name, null);
    }

    public void setGender(String name, Gender gender)
    {
        name = name.toLowerCase();
        this.config.set(name + ".gender", gender.toString());
    }

    public void setStatus(String name, Status status)
    {
        name = name.toLowerCase();
        this.config.set(name + ".status", status.toString());
    }

    public void setPartner(String name, String partner)
    {
        name = name.toLowerCase();
        this.config.set(name + ".partner", partner);
    }

    public void setTrustsPartner(String name, boolean trustsPartner)
    {
        name = name.toLowerCase();
        this.config.set(name + ".trusts partner", trustsPartner);
    }
}
