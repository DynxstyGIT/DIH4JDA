package com.dynxsty.dih4jda.interactions.commands;

import com.dynxsty.dih4jda.DIH4JDA;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a single, executable command.
 *
 * @see ContextCommand
 * @see SlashCommand
 * @since v1.5
 */
public abstract class BaseCommandRequirements extends CommandRequirements {
	private Set<Long> whitelistedGuilds = Set.of();
	private Set<Long> blacklistedGuilds = Set.of();
	private RegistrationType type = DIH4JDA.defaultCommandType;

	/**
	 * Allows a set of {@link Guild}s to update their Slash Commands.
	 *
	 * @param whitelisted An array of {@link Long}s.
	 */
	public final void whitelistGuilds(Long... whitelisted) {
		if (type != RegistrationType.GUILD) {
			throw new UnsupportedOperationException("Cannot whitelist Guilds for Global Commands!");
		}
		whitelistedGuilds = Arrays.stream(whitelisted).collect(Collectors.toSet());
	}

	/**
	 * Prevents the given set of {@link Guild}s from updating their Slash Commands.
	 *
	 * @param blacklisted An array of {@link Long}s.
	 */
	public final void blacklistGuilds(Long... blacklisted) {
		if (type != RegistrationType.GUILD) {
			throw new UnsupportedOperationException("Cannot blacklist Guilds for Global Commands!");
		}
		blacklistedGuilds = Arrays.stream(blacklisted).collect(Collectors.toSet());
	}

	/**
	 * Gets all Guilds whose Slash Commands should be updated.
	 *
	 * @param jda The {@link JDA} instance.
	 * @return A {@link List} with all Guilds.
	 */
	public final Set<Guild> getGuilds(JDA jda) {
		Set<Guild> guilds = new HashSet<>(jda.getGuilds());
		guilds.removeIf(g -> blacklistedGuilds.contains(g.getIdLong()));
		if (!whitelistedGuilds.isEmpty()) {
			guilds = whitelistedGuilds.stream().map(jda::getGuildById).collect(Collectors.toSet());
		}
		return guilds;
	}

	public final RegistrationType getRegistrationType() {
		return type;
	}

	/**
	 * @param type How the command should be queued.
	 */
	public final void setRegistrationType(RegistrationType type) {
		this.type = type;
	}
}
