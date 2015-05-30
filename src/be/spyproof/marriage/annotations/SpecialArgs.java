package be.spyproof.marriage.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Spyproof on 29/05/2015.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialArgs
{
    /**
     * Methods with this annotation will return a List<String>
     * The value string will define a argument (ex. {player})
     * The List will define options to replace the original string
     */

    String value();
}
