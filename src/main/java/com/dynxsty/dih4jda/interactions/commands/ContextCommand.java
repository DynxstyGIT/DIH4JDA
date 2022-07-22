package com.dynxsty.dih4jda.interactions.commands;

import com.dynxsty.dih4jda.DIH4JDALogger;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

/**
 * Model class which represents a single Context Command.
 *
 * @see ContextCommand.User#execute(UserContextInteractionEvent)
 * @see ContextCommand.Message#execute(MessageContextInteractionEvent)
 * @since v1.5
 */
public abstract class ContextCommand extends BaseCommandRequirements {
	private CommandData commandData = null;

	protected ContextCommand() {}

	public final CommandData getCommandData() {
		return commandData;
	}

	/**
	 * Sets this commands' {@link CommandData}.
	 *
	 * @param commandData The corresponding {@link CommandData} which should be used for this context command.
	 * @see net.dv8tion.jda.api.interactions.commands.build.Commands#user(String)
	 * @see net.dv8tion.jda.api.interactions.commands.build.Commands#message(String)
	 */
	public final void setCommandData(@NotNull CommandData commandData) {
		if (commandData.getType() == Command.Type.MESSAGE || commandData.getType() == Command.Type.USER) {
			this.commandData = commandData;
		} else {
			DIH4JDALogger.error(String.format("Invalid Command Type \"%s\" for Context Command! This command will be ignored.", commandData.getType()));
		}
	}

	public abstract static class User extends ContextCommand {
		/**
		 * Abstract method that must be implemented for all User Context Commands.
		 *
		 * <pre>{@code
		 * public class PingContextMenu extends ContextCommand.User {
		 *
		 *    public PingContextMenu() {
		 * 		this.setCommandData(Commands.user("Ping"));
		 *    }
		 *
		 *    @Override
		 *    public void execute(UserContextInteractionEvent event) {
		 * 		event.reply("Pong!").queue();
		 *    }
		 * }}
		 * </pre>
		 */
		public abstract void execute(UserContextInteractionEvent event);
	}

	public abstract static class Message extends ContextCommand {
		/**
		 * Abstract method that must be implemented for all Message Context Commands.
		 *
		 * <pre>{@code
		 * public class PingContextMenu extends GuildContextCommand implements MessageContextCommand {
		 *
		 *    public PingContextMenu() {
		 * 		this.setCommandData(Commands.message("Ping"));
		 *    }
		 *
		 *    @Override
		 *    public void handleMessageContextInteraction(MessageContextInteractionEvent event) {
		 * 		event.reply("Pong!").queue();
		 *    }
		 * }}
		 * </pre>
		 */
		public abstract void execute(MessageContextInteractionEvent event);
	}
}
