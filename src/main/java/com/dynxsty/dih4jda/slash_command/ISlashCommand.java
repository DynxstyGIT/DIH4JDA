package com.dynxsty.dih4jda.slash_command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;

/**
 * An interface for DIH4JDA's Slash Commands.
 */
public interface ISlashCommand {
    void handleSlash(SlashCommandEvent event);
}