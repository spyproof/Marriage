package be.spyproof.marriage.annotations;

import be.spyproof.marriage.handlers.Messages;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Spyproof on 8/05/2015.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Beta {
    String value() default Messages.betaCommand;
}
