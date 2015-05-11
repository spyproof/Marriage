package be.spyproof.marriage;

/**
 * Created by Nils on 3/04/2015.
 */
public enum Status
{
    SINGLE ("single"),
    MARRIED_TO_PERSON ("married to a person"),
    MARRIED_TO_LEFT_HAND ("married to its left hand"),
    MARRIED_TO_RIGHT_HAND ("married to its right hand"),
    NOT_INTERESTED ("not interested"),
    DIVORCED ("divorced");

    private final String name;

    private Status(String s){
        name = s;
    }

    public boolean equalsName(String otherName){
        return name.equals(otherName);
    }

    public String toString(){
        return name;
    }

    public static Status fromString(String statusString) {
        if (statusString != null) {
            for (Status gender : Status.values()) {
                if (gender.equalsName(statusString)) {
                    return gender;
                }
            }
        }
        return Status.NOT_INTERESTED; // Default value
    }
}
