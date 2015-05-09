package be.spyproof.marriage;

/**
 * Created by Nils on 2/04/2015.
 */
public enum Gender
{
    MALE("male"),
    FEMALE("female"),
    HIDDEN ("HIDDEN");

    private final String name;

    private Gender(String s){
        name = s;
    }

    public boolean equalsName(String otherName){
        return name.equalsIgnoreCase(otherName);
    }

    public String toString(){
        return name;
    }
}
