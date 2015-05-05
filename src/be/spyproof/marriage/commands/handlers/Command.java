package be.spyproof.marriage.commands.handlers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Spyproof on 4/05/2015.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command
{
    //Hide a tab completion with "{}" -> "{whatever}" //TODO redo hiding
    String command();

    String trigger();

    String[] args();

    boolean playersOnly() default false;

    String permission() default "none";

    String desc() default "";

    String usage() default "";

    boolean helpHidden() default false;

}
