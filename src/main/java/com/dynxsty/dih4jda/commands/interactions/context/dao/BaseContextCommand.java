package com.dynxsty.dih4jda.commands.interactions.context.dao;

import com.dynxsty.dih4jda.DIH4JDA;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class BaseContextCommand {
	private CommandData commandData;

	public CommandData getCommandData() {
		return commandData;
	}

	public void setCommandData(CommandData commandData) {
		if (commandData.getType() == Command.Type.MESSAGE || commandData.getType() == Command.Type.USER) {
			this.commandData = commandData;
		} else {
			DIH4JDA.log.error(
					String.format("Invalid Command Type \"%s\" for Context Command! This command will be ignored.", commandData.getType())
			);
		}
	}
}
