package com.dynxsty.dih4jda.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * An interface for DIH4JDA's Slash Commands.
 */
public interface ISlashCommand {
    void handleSlashCommandInteraction(SlashCommandInteractionEvent event);
}