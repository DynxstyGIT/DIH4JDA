package com.dynxsty.dih4jda.events;

import com.dynxsty.dih4jda.DIH4JDALogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Set;

public enum DIH4JDAEvent {
	COMMAND_EXCEPTION("onCommandException"),
	COMPONENT_EXCEPTION("onComponentException"),
	AUTOCOMPLETE_EXCEPTION("onAutoCompleteException"),
	MODAL_EXCEPTION("onModalException"),
	INSUFFICIENT_PERMISSIONS("onInsufficientPermissions"),
	INVALID_USER("onInvalidUser"),
	INVALID_ROLE("onInvalidRole");

	private final String eventName;

	DIH4JDAEvent(String eventName) {
		this.eventName = eventName;
	}

	@Override
	public String toString() {
		return this.eventName;
	}

	/**
	 * Fires an event from the {@link DIH4JDAEventListener}.
	 *
	 * @param listeners A set of all classes that extend the {@link DIH4JDAEventListener}.
	 * @param args      The event's arguments.
	 * @since v1.5
	 */
	public void fire(@NotNull Set<DIH4JDAEventListener> listeners, Object... args) {
		if (listeners.isEmpty()) {
			DIH4JDALogger.warn(String.format("%s was fired, but not handled (No listener registered)", this), DIH4JDALogger.Type.EVENT_FIRED);
		}
		for (DIH4JDAEventListener listener : listeners) {
			try {
				for (Method method : listener.getClass().getMethods()) {
					if (method.getName().equals(toString())) {
						method.invoke(listener.getClass().getConstructor().newInstance(), args);
					}
				}
			} catch (ReflectiveOperationException e) {
				DIH4JDALogger.error(e.getMessage());
			}
		}
	}
}
