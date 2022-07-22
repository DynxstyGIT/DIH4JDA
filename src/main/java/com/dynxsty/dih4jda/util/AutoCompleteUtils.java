package com.dynxsty.dih4jda.util;

import com.dynxsty.dih4jda.DIH4JDALogger;
import com.dynxsty.dih4jda.interactions.ComponentIdBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class that contains some useful methods regarding the AutoComplete system.
 *
 * @since v1.4
 */
public class AutoCompleteUtils {
	private static final Map<String, List<Command.Choice>> CHOICE_CACHE;

	static {
		CHOICE_CACHE = new HashMap<>();
	}

	private AutoCompleteUtils() {
	}

	/**
	 * Filters all AutoComplete choices based on the user's current input.
	 *
	 * <pre>{@code
	 * return event.replyChoices(AutoCompleteUtils.filterChoices(event, choices));
	 * }</pre>
	 *
	 * @param event   The {@link CommandAutoCompleteInteractionEvent} that was fired.
	 * @param choices A {@link List} of {@link Command.Choice}s.
	 * @return The filtered {@link List} of {@link Command.Choice}s.
	 * @since v1.4
	 */
	@Contract("_, _ -> param2")
	public static @NotNull List<Command.Choice> filterChoices(@NotNull CommandAutoCompleteInteractionEvent event, List<Command.Choice> choices) {
		return AutoCompleteUtils.filterChoices(event.getFocusedOption().getValue().toLowerCase(), choices);
	}

	/**
	 * Filters all AutoComplete choices based on the user's current input.
	 *
	 * <pre>{@code
	 * return event.replyChoices(AutoCompleteUtils.filterChoices("abc", choices));
	 * }</pre>
	 *
	 * @param filter  The filter.
	 * @param choices A {@link List} of {@link Command.Choice}s.
	 * @return The filtered {@link List} of {@link Command.Choice}s.
	 * @since v1.4
	 */
	@Contract("_, _ -> param2")
	public static @NotNull List<Command.Choice> filterChoices(String filter, @NotNull List<Command.Choice> choices) {
		choices.removeIf(choice -> !choice.getName().toLowerCase().contains(filter.toLowerCase()));
		return choices;
	}

	/**
	 * A simple shortcut for caching AutoComplete Choices.
	 * This will try to get, based on the specified {@link CommandAutoCompleteInteractionEvent}, a {@link List} of {@link Command.Choice}s.
	 * If no mapping is found, this will then simply cache the choices for later usage.
	 *
	 * @param event           The {@link CommandAutoCompleteInteractionEvent} which was fired.
	 * @param choicesFunction The function to use if no mapping is found and the choices need to be cached.
	 * @return An unmodifiable {@link List} of {@link Command.Choice}s.
	 */
	public static @NotNull List<Command.Choice> handleChoices(@NotNull CommandAutoCompleteInteractionEvent event,
	                                                          Function<CommandAutoCompleteInteractionEvent, List<Command.Choice>> choicesFunction) {
		String id = buildCacheId(event);
		List<Command.Choice> choices = getFromCache(id);
		if (choices == null) {
			choices = choicesFunction.apply(event);
			cacheChoices(event, choices);
		}
		return filterChoices(event, choices);
	}

	/**
	 * Caches the given list of choices.
	 *
	 * @param event   The {@link CommandAutoCompleteInteractionEvent} which was fired.
	 * @param choices The {@link List} of {@link Command.Choice}s to cache.
	 */
	public static void cacheChoices(@NotNull CommandAutoCompleteInteractionEvent event, List<Command.Choice> choices) {
		String id = buildCacheId(event);
		CHOICE_CACHE.put(id, choices);
		DIH4JDALogger.debug(String.format("Cached %s choices for %s", choices.size(), id));
	}

	/**
	 * Attempts to get a {@link List} of {@link Command.Choice}s, based on the specified id.
	 * If there is no mapping with the specfied id, this will return null.
	 *
	 * @param id The choices' id.
	 * @return An unmodifiable {@link List} of {@link Command.Choice}s, or null.
	 */
	public static @Nullable List<Command.Choice> getFromCache(String id) {
		if (CHOICE_CACHE.containsKey(id)) {
			return CHOICE_CACHE.get(id);
		}
		return null;
	}

	/**
	 * Removes all cached choices for the given command, user & guild.
	 * This method gets called every time the specified user executes the specified command in the specified guild.
	 * This is needed in order to retain the dynamic aspect of autocomplete choices.
	 *
	 * @param commandPath The command's path.
	 * @param userId      The user's id.
	 * @param guildId     The guild's id.
	 */
	public static void removeFromCache(String commandPath, String userId, String guildId) {
		if (CHOICE_CACHE != null) {
			List<String> keys = CHOICE_CACHE.keySet().stream()
					.filter(f -> f.contains(ComponentIdBuilder.build(commandPath, userId, guildId)))
					.collect(Collectors.toList());
			for (String key : keys) {
				CHOICE_CACHE.remove(key);
				DIH4JDALogger.debug(String.format("Removed cached choices for %s", key));
			}
		}
	}

	/**
	 * Builds an identifier from the specified {@link CommandAutoCompleteInteractionEvent} by combining the
	 * command path, the user's id, the guild's id (or simply "0" if the guild is null) and the option's name.
	 *
	 * @param event The {@link CommandAutoCompleteInteractionEvent} which was fired.
	 * @return The identifier which is used in combination with the {@link AutoCompleteUtils#CHOICE_CACHE}
	 */
	public static @NotNull String buildCacheId(@NotNull CommandAutoCompleteInteractionEvent event) {
		return ComponentIdBuilder.build(
				event.getCommandPath(),
				event.getUser().getId(),
				event.getGuild() == null ? "0" : event.getGuild().getId(),
				event.getFocusedOption().getName()
		);
	}
}
