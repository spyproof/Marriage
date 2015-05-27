package be.spyproof.marriage.datamanager;

import be.spyproof.marriage.handlers.Messages;
import org.bukkit.Color;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Spyproof on 25/05/2015.
 */
public class MyConfig
{
    private FileConfiguration config;
    private File configPath;
    private Map<String, Object> values; //path - value

    public MyConfig(FileConfiguration config, File path)
    {
        this.config = config;
        this.configPath = path;
        this.values = new HashMap<String, Object>();
        load();
    }

    public MyConfig(FileConfiguration config, File path, Configuration defaultFilePath)
    {
        //TODO initialize file WITH COMMENTS
        this.config = config;
        this.configPath = path;
        this.values = new HashMap<String, Object>();

        //Get all default values
        for (String s : defaultFilePath.getKeys(true))
        {
            this.values.put(s, defaultFilePath.get(s));
        }

        //Load new values
        load();
    }

    /**
     * Saving & loading
     */

    public void save()
    {
        for (String key : this.values.keySet())
            this.config.set(key, this.values.get(key));
        try {
            this.config.save(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load()
    {
        try {
            this.config.load(configPath);
            this.values.clear();
            for (String s : config.getKeys(true))
            {
                this.values.put(s, config.get(s));
                Messages.sendDebugInfo(s + " : " + config.get(s).toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    public boolean setValue(String path, Object value)
    {
        if (!this.values.containsKey(path))
            return false;
        else
        {
            this.values.put(path, value);
            return true;
        }
    }

    /**
     * getting info
     */

    public byte getByte(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Byte)
            return (Byte) o;
        return 0;
    }

    public short getShort(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Short)
            return (Short) o;
        return 0;
    }

    public int getInt(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Integer)
            return (Integer) o;
        return 0;
    }

    public Long getLong(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Long)
            return (Long) o;
        return 0L;
    }

    public float getFloat(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Float)
            return (Float) o;
        return 0F;
    }

    public double getDouble(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Double)
            return (Double) o;
        return 0D;
    }

    public boolean getBoolean(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Boolean)
            return (Boolean) o;
        return false;
    }

    public char getChar(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Character)
            return (Character) o;
        return Character.MIN_VALUE;
    }

    public String getString(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof String)
            return (String) o;
        return null;
    }

    public Color getColor(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof Color)
            return (Color) o;
        return null;
    }

    public ItemStack getItemStack(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof ItemStack)
            return (ItemStack) o;
        return null;
    }

    public List getList(String path)
    {
        Object o = this.values.get(path);
        if (o instanceof List)
            return (List) o;
        return null;
    }
}
