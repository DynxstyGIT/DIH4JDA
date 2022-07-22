package com.dynxsty.dih4jda;

import com.dynxsty.dih4jda.interactions.commands.model.UnqueuedCommandData;
import com.dynxsty.dih4jda.interactions.commands.model.UnqueuedSlashCommandData;
import com.dynxsty.dih4jda.util.CommandUtils;
import com.dynxsty.dih4jda.util.Pair;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <h1>Smart Queue</h1>
 * This Class handles all the SmartQueue functionality which can be disabled using
 * {@link DIH4JDABuilder#setGlobalSmartQueue(boolean)} & {@link DIH4JDABuilder#setGuildSmartQueue(boolean)}.
 * <br><br>
 * <p>
 *     This basically retrieves all existing commands and compares them with the local ones, thus, only queuing
 *     "new" (commands, which yet do not exist) or edited commands , which reduces the amount of total command updates.
 *
 * </p>
 * <br>
 * <p>
 *     In addition to that, this will also delete ALL unknown commands, which were not found locally. This can be disabled
 *     using {@link DIH4JDABuilder#disableUnknownCommandDeletion()}.</p>
 * @since v1.5
 */
public class SmartQueue {
	private final Set<UnqueuedSlashCommandData> slashData;
	private final Set<UnqueuedCommandData> commandData;
	private final boolean deleteUnknown;

	protected SmartQueue(Set<UnqueuedSlashCommandData> slashData, Set<UnqueuedCommandData> commandData, boolean deleteUnknown) {
		this.slashData = slashData;
		this.commandData = commandData;
		this.deleteUnknown = deleteUnknown;
	}

	/**
	 * Compares CommandData with already existing Commands, removed duplicates and, if enabled, deleted unknown commands.
	 *
	 * @param jda The {@link JDA} instance which is used to retrieve the already existing commands.
	 * @return A {@link Pair} with the remaining {@link SlashCommandData} and {@link CommandData}.
	 * @since v1.5
	 */
	protected @NotNull Pair<Set<UnqueuedSlashCommandData>, Set<UnqueuedCommandData>> checkGlobal(@NotNull JDA jda) {
		List<Command> existing;
		try {
			existing = jda.retrieveCommands().complete();
		} catch (ErrorResponseException e) {
			return new Pair<>(Set.of(), Set.of());
		}
		if (!existing.isEmpty()) {
			return removeDuplicates(jda, existing, null);
		}
		return new Pair<>(slashData, commandData);
	}

	/**
	 * Compares CommandData with already existing Commands, removed duplicates and, if enabled, deletes unknown commands.
	 *
	 * @param guild The {@link Guild} which is used to retrieve the already existing commands.
	 * @return A {@link Pair} with the remaining {@link SlashCommandData} and {@link CommandData}.
	 * @since v1.5
	 */
	protected @NotNull Pair<Set<UnqueuedSlashCommandData>, Set<UnqueuedCommandData>> checkGuild(@NotNull Guild guild) {
		List<Command> existing;
		try {
			existing = guild.retrieveCommands().complete();
		} catch (ErrorResponseException e) {
			DIH4JDALogger.error("Could not retrieve Commands from Guild " + guild.getName() + "!" +
					" Please make sure that the bot was invited with the application.commands scope!");
			return new Pair<>(Set.of(), Set.of());
		}
		if (!existing.isEmpty()) {
			return removeDuplicates(guild.getJDA(), existing, guild);
		}
		return new Pair<>(slashData, commandData);
	}

	/**
	 * Removes all duplicate {@link CommandData} and, if enabled, deletes unknown commands.
	 *
	 * @param jda      The {@link JDA} instance.
	 * @param existing A List of all existing {@link Command}s.
	 * @param guild    An optional guild parameter which is used with {@link SmartQueue#checkGuild(Guild)}.
	 * @return A {@link Pair} with the remaining {@link SlashCommandData} & {@link CommandData}.
	 * @since v1.5
	 */
	private @NotNull Pair<Set<UnqueuedSlashCommandData>, Set<UnqueuedCommandData>> removeDuplicates(JDA jda, final List<Command> existing, @Nullable Guild guild) {
		List<Command> commands = new ArrayList<>(existing);
		boolean global = guild == null;
		String prefix = String.format("[%s] ", global ? "Global" : guild.getName());
		DIH4JDALogger.info(String.format(prefix + "Found %s existing command(s)", existing.size()), DIH4JDALogger.Type.SMART_QUEUE);
		// remove already-existing commands
		commands.removeIf(cmd -> {
			if (commandData.stream().anyMatch(data -> CommandUtils.isEqual(cmd, data.getData(), global)) ||
					slashData.stream().anyMatch(data -> CommandUtils.isEqual(cmd, data.getData(), global))) {
				// check for command in blacklisted guilds
				// this may be refactored soonTM, as its kinda clunky
				if (!global) {
					for (UnqueuedSlashCommandData d : slashData) {
						if (CommandUtils.isEqual(cmd, d.getData(), false) && !d.getGuilds().contains(guild)) {
							DIH4JDALogger.info("Deleting /" + cmd.getName() + " in Guild: " + guild.getName());
							cmd.delete().queue();
							return true;
						}
					}
					for (UnqueuedCommandData d : commandData) {
						if (CommandUtils.isEqual(cmd, d.getData(), false) && !d.getGuilds().contains(guild)) {
							DIH4JDALogger.info("Deleting " + cmd.getName() + " in Guild: " + guild.getName());
							cmd.delete().queue();
							return true;
						}
					}
				}
				DIH4JDALogger.info(String.format(prefix + "Found duplicate %s command, which will be ignored: %s", cmd.getType(), cmd.getName()), DIH4JDALogger.Type.SMART_QUEUE);
				return true;
			}
			return false;
		});
		commandData.removeIf(data -> existing.stream().anyMatch(p -> CommandUtils.isEqual(p, data.getData(), global)));
		slashData.removeIf(data -> existing.stream().anyMatch(p -> CommandUtils.isEqual(p, data.getData(), global)));
		// remove unknown commands, if enabled
		if (!commands.isEmpty()) {
			for (Command command : commands) {
				if (existing.contains(command)) {
					if (deleteUnknown) {
						DIH4JDALogger.info(String.format(prefix + "Deleting unknown %s command: %s", command.getType(), command.getName()), DIH4JDALogger.Type.SMART_QUEUE);
						if (global) {
							jda.deleteCommandById(command.getId()).queue();
						} else {
							guild.deleteCommandById(command.getId()).queue();
						}
					} else {
						DIH4JDALogger.info(String.format(prefix + "Ignored unknown %s command: %s", command.getType(), command.getName()), DIH4JDALogger.Type.SMART_QUEUE);
					}
				}
			}
		}
		return new Pair<>(slashData, commandData);
	}
}
