package com.github.fukkitmc.fukkit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * represents an <b>access</b> class for a nms class
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BukkitBridge {
	Class<?> value();
}
