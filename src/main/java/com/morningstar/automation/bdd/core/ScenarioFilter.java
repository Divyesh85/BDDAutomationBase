package com.morningstar.automation.bdd.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD}) 
public @interface ScenarioFilter {
	String[] tags() default {};
	String[] name() default {};
}
