package com.dynxsty.dih4jda;

import com.dynxsty.dih4jda.config.DIH4JDAConfig;
import com.dynxsty.dih4jda.events.DIH4JDAEventListener;
import com.dynxsty.dih4jda.interactions.commands.RegistrationType;
import com.dynxsty.dih4jda.interactions.components.ButtonHandler;
import com.dynxsty.dih4jda.interactions.components.ModalHandler;
import com.dynxsty.dih4jda.interactions.components.SelectMenuHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <h1>DIH4JDA</h1>
 * <h2>Creating a new {@link DIH4JDA} instance:</h2>
 * <pre>{@code
 * DIH4JDA dih4JDA = DIH4JDABuilder
 *         .setJDA(jda) // Your JDA instance
 *         .setCommandsPackage("com.dynxsty.superawesomebot.commands") // The main package where all your commands are in.
 *         .build();
 * }</pre>
 * Upon calling .build();, the bot will automatically register all Commands that are in the given commandsPackage.
 * (if not disabled)
 */
public class DIH4JDA extends ListenerAdapter {

	/**
	 * The default {@link RegistrationType} which is used for queuing new commands.
	 * This can be overridden using {@link com.dynxsty.dih4jda.interactions.commands.BaseCommandRequirements#setRegistrationType(RegistrationType)}
	 */
	public static RegistrationType defaultCommandType;

	// Component Handler
	private static Map<List<String>, ButtonHandler> buttonHandlers;
	private static Map<List<String>, SelectMenuHandler> selectMenuHandlers;
	private static Map<List<String>, ModalHandler> modalHandlers;

	static {
		buttonHandlers = new HashMap<>();
		selectMenuHandlers = new HashMap<>();
		modalHandlers = new HashMap<>();
	}

	private final DIH4JDAConfig config;
	private final Set<DIH4JDAEventListener> listeners;
	private InteractionHandler handler;

	/**
	 * Constructs a new DIH4JDA instance
	 *
	 * @param config The instance's configuration.
	 */
	protected DIH4JDA(DIH4JDAConfig config) {
		if (defaultCommandType == null) defaultCommandType = RegistrationType.GUILD;
		this.config = config;
		listeners = new HashSet<>();
		DIH4JDALogger.blockedLogTypes = config.getBlockedLogTypes();
		this.handler = new InteractionHandler(this);
		config.getJDA().addEventListener(this, handler);
	}

	/**
	 * Ran once the {@link JDA} instance fires the {@link ReadyEvent}.
	 *
	 * @param event The {@link ReadyEvent} that was fired.
	 */
	@Override
	public void onReady(@NotNull ReadyEvent event) {
		if (config.getCommandsPackage() == null) return;
		try {
			if (config.isRegisterOnReady() && handler != null) {
				handler.registerInteractions();
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			DIH4JDALogger.error("Could not register commands: " + e.getMessage());
		}
	}

	/**
	 * Registers all Interactions and replaces the old ones.
	 * Please note that global commands may need up to an hour before they're fully registered.
	 */
	public void registerInteractions() throws ReflectiveOperationException {
		if (handler != null) {
			handler.registerInteractions();
		}
	}

	/**
	 * @return The instance's configuration.
	 */
	public DIH4JDAConfig getConfig() {
		return config;
	}

	/**
	 * Allows to add Listener classes (that extend {@link DIH4JDAEventListener}).
	 *
	 * @since v1.5
	 */
	public void addListener(Object... classes) {
		for (Object o : classes) {
			try {
				// check if class extends the ListenerAdapter
				DIH4JDAEventListener adapter = (DIH4JDAEventListener) o;
				listeners.add(adapter);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Listener classes must extend DIH4JDAListenerAdapter!");
			}
		}
	}

	/**
	 * @return A set of all Listener classes.
	 * @see DIH4JDA#addListener(Object...)
	 */
	protected Set<DIH4JDAEventListener> getListeners() {
		return listeners;
	}

	/**
	 * Convenience method which replaces {@link DIH4JDA#getConfig()#getJDA()}
	 *
	 * @return The {@link JDA} instance.
	 */
	public JDA getJDA() {
		return config.getJDA();
	}

	/**
	 * Binds all {@link ButtonHandler}s to their id.
	 * <br>
	 * <pre>{@code
	 * dih4jda.addButtonHandlers(Map.of(
	 * 	"apple", new AppleButtonHandler,
	 * 	"banana", new BananaButtonHandler
	 * ));
	 * }</pre>
	 * <br>
	 * This is best used in combination with {@link com.dynxsty.dih4jda.interactions.ComponentIdBuilder#build(String, Object...)}.
	 *
	 * @param handlers All {@link ButtonHandler}, as a immutable {@link Map}.
	 */
	public void addButtonHandlers(Map<List<String>, ButtonHandler> handlers) {
		buttonHandlers.putAll(handlers);
	}

	/**
	 * Gets all {@link ButtonHandler}s.
	 *
	 * @return An immutable {@link Map} which stores the id as the <strong>Key</strong> and
	 * the {@link ButtonHandler} as the <strong>Value</strong>.
	 */
	public Map<List<String>, ButtonHandler> getButtonHandlers() {
		return buttonHandlers;
	}

	/**
	 * Binds all {@link SelectMenuHandler}s to their id.
	 * <br>
	 * <pre>{@code
	 * dih4jda.addSelectMenuHandlers(Map.of(
	 * 	"apple", new AppleSelectMenuHandler,
	 * 	"banana", new BananaSelectMenuHandler
	 * ));
	 * }</pre>
	 * <br>
	 * This is best used in combination with {@link com.dynxsty.dih4jda.interactions.ComponentIdBuilder#build(String, Object...)}.
	 *
	 * @param handlers All {@link SelectMenuHandler}, as a immutable {@link Map}.
	 */
	public void addSelectMenuHandlers(Map<List<String>, SelectMenuHandler> handlers) {
		selectMenuHandlers.putAll(handlers);
	}

	/**
	 * Gets all {@link SelectMenuHandler}s.
	 *
	 * @return An immutable {@link Map} which stores the id as the <strong>Key</strong> and
	 * the {@link SelectMenuHandler} as the <strong>Value</strong>.
	 */
	public Map<List<String>, SelectMenuHandler> getSelectMenuHandlers() {
		return selectMenuHandlers;
	}

	/**
	 * Binds all {@link ModalHandler}s to their id.
	 * <br>
	 * <pre>{@code
	 * dih4jda.addModalHandlers(Map.of(
	 * 	"apple", new AppleModalHandler,
	 * 	"banana", new BananaModalHandler
	 * ));
	 * }</pre>
	 * <br>
	 * This is best used in combination with {@link com.dynxsty.dih4jda.interactions.ComponentIdBuilder#build(String, Object...)}.
	 *
	 * @param handlers All {@link ModalHandler}, as a immutable {@link Map}.
	 */
	public void addModalHandlers(Map<List<String>, ModalHandler> handlers) {
		modalHandlers.putAll(handlers);
	}

	/**
	 * Gets all {@link ModalHandler}s.
	 *
	 * @return An immutable {@link Map} which stores the id as the <strong>Key</strong> and
	 * the {@link ModalHandler} as the <strong>Value</strong>.
	 */
	public Map<List<String>, ModalHandler> getModalHandlers() {
		return modalHandlers;
	}
}
