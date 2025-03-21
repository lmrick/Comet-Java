package com.cometproject.api.utilities.events;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IEventListener {
	
	Class<? extends Event<?>> onEvent();
	
}

