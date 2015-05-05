package be.spyproof.marriage;

/**
 * Created by Nils on 2/04/2015.
 */
public enum Gender
{
    MALE("man"),
    FEMALE("woman"),
    HIDDEN ("HIDDEN");

    private final String name;

    private Gender(String s){
        name = s;
    }

    public boolean equalsName(String otherName){
        return name.equals(otherName);
    }

    public String toString(){
        return name;
    }

    public static Gender fromString(String genderString) {
        if (genderString != null) {
            for (Gender gender : Gender.values()) {
                if (gender.equalsName(genderString)) {
                    return gender;
                }
            }
        }
        return null;
    }

}
