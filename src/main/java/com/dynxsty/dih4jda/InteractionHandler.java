package com.dynxsty.dih4jda;

import com.dynxsty.dih4jda.exceptions.CommandNotRegisteredException;
import com.dynxsty.dih4jda.interactions.autocomplete.AutoCompleteHandler;
import com.dynxsty.dih4jda.interactions.commands.context_command.MessageContextCommand;
import com.dynxsty.dih4jda.interactions.commands.context_command.MessageContextInteraction;
import com.dynxsty.dih4jda.interactions.commands.context_command.UserContextCommand;
import com.dynxsty.dih4jda.interactions.commands.context_command.UserContextInteraction;
import com.dynxsty.dih4jda.interactions.commands.context_command.dao.BaseContextCommand;
import com.dynxsty.dih4jda.interactions.commands.context_command.dao.GlobalContextCommand;
import com.dynxsty.dih4jda.interactions.commands.context_command.dao.GuildContextCommand;
import com.dynxsty.dih4jda.interactions.commands.slash_command.SlashCommand;
import com.dynxsty.dih4jda.interactions.commands.slash_command.SlashCommandInteraction;
import com.dynxsty.dih4jda.interactions.commands.slash_command.dao.*;
import com.dynxsty.dih4jda.interactions.components.ComponentIdBuilder;
import com.dynxsty.dih4jda.interactions.components.button.ButtonHandler;
import com.dynxsty.dih4jda.interactions.components.modal.ModalHandler;
import com.dynxsty.dih4jda.interactions.components.select_menu.SelectMenuHandler;
import com.dynxsty.dih4jda.util.CommandUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

//TODO-v1.4: Documentation
public class InteractionHandler extends ListenerAdapter {

	//TODO-v1.4: Documentation
	private final DIH4JDA dih4jda;

	//TODO-v1.4: Documentation
	private final Map<String, SlashCommandInteraction> slashCommandIndex;

	//TODO-v1.4: Documentation
	private final Map<String, MessageContextInteraction> messageContextIndex;

	//TODO-v1.4: Documentation
	private final Map<String, UserContextInteraction> userContextIndex;

	//TODO-v1.4: Documentation
	private final Map<String, AutoCompleteHandler> autoCompleteIndex;

	//TODO-v1.4: Documentation
	private final Map<String, ButtonHandler> buttonIndex;

	//TODO-v1.4: Documentation
	private final Map<String, SelectMenuHandler> selectMenuIndex;

	//TODO-v1.4: Documentation
	private final Map<String, ModalHandler> modalIndex;

	private final Set<Class<? extends GuildSlashCommand>> guildCommands;
	private final Set<Class<? extends GlobalSlashCommand>> globalCommands;
	private final Set<Class<? extends GuildContextCommand>> guildContexts;
	private final Set<Class<? extends GlobalContextCommand>> globalContexts;

	/**
	 * Constructs a new {@link InteractionHandler} from the supplied commands package.
	 *
	 * @param dih4jda The {@link DIH4JDA} instance.
	 */
	protected InteractionHandler(DIH4JDA dih4jda) {
		this.guildCommands = new HashSet<>();
		this.globalCommands = new HashSet<>();
		this.guildContexts = new HashSet<>();
		this.globalContexts = new HashSet<>();
		this.slashCommandIndex = new HashMap<>();
		this.messageContextIndex = new HashMap<>();
		this.userContextIndex = new HashMap<>();
		this.autoCompleteIndex = new HashMap<>();
		this.buttonIndex = new HashMap<>();
		this.selectMenuIndex = new HashMap<>();
		this.modalIndex = new HashMap<>();
		this.dih4jda = dih4jda;
	}

	//TODO-v1.4: Documentation
	public void registerInteractions(JDA jda) throws Exception {
		// find all commands
		findSlashCommands();
		findContextCommands();
		// register commands for each guild
		for (Guild guild : jda.getGuilds()) {
			Set<CommandData> commands = new HashSet<>();
			commands.addAll(getGuildSlashCommandData(guild));
			commands.addAll(getGuildContextCommandData(guild));
			guild.updateCommands().addCommands(commands).queue();
			DIH4JDALogger.info(String.format("Queued %s command(s) in guild %s: %s", commands.size(), guild.getName(), commands.stream().map(CommandData::getName).collect(Collectors.joining(", "))), DIH4JDALogger.Type.COMMANDS_QUEUED);
		}
		final List<Command> existingData = jda.retrieveCommands().complete();
		List<Command> allCommands = new ArrayList<>(existingData);
		Set<SlashCommandData> slashData = new HashSet<>(getGlobalSlashCommandData());
		Set<CommandData> commandData = new HashSet<>(getGlobalContextCommandData());
		// check if smart queuing was disabled
		if (dih4jda.isSmartQueuing() && allCommands.size() > 0) {
			DIH4JDALogger.info(String.format("Found %s existing global command(s). Trying to just queue edited commands...", allCommands.size()), DIH4JDALogger.Type.SMART_QUEUE);
			commandData.removeIf(command -> existingData.stream().anyMatch(data -> isCommandData(allCommands, data, command)));
			slashData.removeIf(command -> existingData.stream().anyMatch(data -> isCommandData(allCommands, data, command)));
			// remove unknown commands
			if (allCommands.size() > 0) {
				DIH4JDALogger.info(String.format("Found %s unknown command(s). Attempting deletion.", allCommands.size()), DIH4JDALogger.Type.SMART_QUEUE);
				for (Command command : allCommands) {
					DIH4JDALogger.info(String.format("Deleting unknown %s command: %s", command.getType(), command.getName()), DIH4JDALogger.Type.SMART_QUEUE);
					jda.deleteCommandById(command.getId()).queue();
				}
			}
		}
		commandData.addAll(slashData);
		// queue all global commands
		if (commandData.size() > 0) {
			jda.updateCommands().addCommands(commandData).queue();
			DIH4JDALogger.info(String.format("Queued %s global command(s): %s", commandData.size(), commandData.stream().map(CommandData::getName).collect(Collectors.joining(", "))), DIH4JDALogger.Type.COMMANDS_QUEUED);
		}
		// register command privileges
		registerCommandPrivileges(jda);
	}

	private boolean isCommandData(List<Command> allCommands, Command command, Object data) {
		boolean equals = false;
		if (data instanceof CommandData) equals = CommandUtils.equals((CommandData) data, command);
		if (data instanceof SlashCommandData) equals = CommandUtils.equals((SlashCommandData) data, command);
		if (equals) {
			allCommands.remove(command);
			DIH4JDALogger.info(String.format("Found duplicate %s command, which will be ignored: %s", command.getType(), command.getName()), DIH4JDALogger.Type.SMART_QUEUE);
		}
		return equals;
	}

	/**
	 * Registers all slash commands. Loops through all classes found in the commands package that is either a subclass of {@link GuildSlashCommand} or {@link GlobalSlashCommand}.
	 */
	private void findSlashCommands() {
		Reflections commands = new Reflections(dih4jda.getCommandsPackage());
		guildCommands.addAll(commands.getSubTypesOf(GuildSlashCommand.class));
		globalCommands.addAll(commands.getSubTypesOf(GlobalSlashCommand.class));
	}

	/**
	 * Registers all context commands. Loops through all classes found in the commands package that is either a subclass of {@link GuildContextCommand} or {@link GlobalContextCommand}.
	 */
	private void findContextCommands() {
		Reflections commands = new Reflections(dih4jda.getCommandsPackage());
		guildContexts.addAll(commands.getSubTypesOf(GuildContextCommand.class));
		globalContexts.addAll(commands.getSubTypesOf(GlobalContextCommand.class));
	}

	/**
	 * Registers all Command Privileges.
	 *
	 * @param jda The {@link JDA} instance.
	 */
	private void registerCommandPrivileges(JDA jda) {
		for (Guild guild : jda.getGuilds()) {
			Map<String, Set<CommandPrivilege>> privileges = new HashMap<>();
			guild.retrieveCommands().queue(commands -> {
				for (Command command : commands) {
					if (privileges.containsKey(command.getId())) continue;
					Optional<SlashCommandInteraction> interactionOptional = slashCommandIndex
							.keySet()
							.stream()
							.filter(p -> p.equals(command.getName()) || p.split("/")[0].equals(command.getName()))
							.map(slashCommandIndex::get)
							.filter(p -> p.getPrivileges() != null && p.getPrivileges().length > 0)
							.findFirst();
					if (interactionOptional.isPresent()) {
						SlashCommandInteraction interaction = interactionOptional.get();
						if (interaction.getBaseClass().getSuperclass().equals(GlobalSlashCommand.class)) {
							DIH4JDALogger.error(String.format("Can not register command privileges for global command %s (%s).", command.getName(), interaction.getBaseClass().getSimpleName()));
							continue;
						}
						privileges.put(command.getId(), new HashSet<>(Arrays.asList(interaction.getPrivileges())));
						DIH4JDALogger.info(String.format("[%s] Registered privileges for command %s: %s", guild.getName(), command.getName(), Arrays.stream(interaction.getPrivileges()).map(CommandPrivilege::toData).collect(Collectors.toList())), DIH4JDALogger.Type.COMMAND_PRIVILEGE_REGISTERED);
					}
					if (privileges.isEmpty()) continue;
					guild.updateCommandPrivileges(privileges).queue();
				}
			});
		}
	}

	/**
	 * Gets all Guild commands registered in {@link InteractionHandler#findSlashCommands()} and adds
	 * them to the {@link InteractionHandler#slashCommandIndex}.
	 *
	 * @param guild The command's guild.
	 * @throws Exception If an error occurs.
	 */
	private Set<SlashCommandData> getGuildSlashCommandData(@NotNull Guild guild) throws Exception {
		Set<SlashCommandData> commands = new HashSet<>();
		for (Class<? extends GuildSlashCommand> c : guildCommands) {
			GuildSlashCommand instance = (GuildSlashCommand) getClassInstance(guild, c);
			if (!instance.getGuilds(guild.getJDA()).contains(guild)) {
				DIH4JDALogger.info("Skipping Registration of " + c.getSimpleName() + " for Guild: " + guild.getName(), DIH4JDALogger.Type.SLASH_COMMAND_SKIPPED);
				continue;
			}
			commands.add(getBaseCommandData(instance, c, guild));
		}
		return commands;
	}

	/**
	 * Gets all Global commands registered in {@link InteractionHandler#findSlashCommands()} and adds
	 * them to the {@link InteractionHandler#slashCommandIndex}.
	 *
	 * @throws Exception If an error occurs.
	 */
	private Set<SlashCommandData> getGlobalSlashCommandData() throws Exception {
		Set<SlashCommandData> commands = new HashSet<>();
		for (Class<? extends GlobalSlashCommand> slashCommandClass : globalCommands) {
			GlobalSlashCommand instance = (GlobalSlashCommand) getClassInstance(null, slashCommandClass);
			commands.add(getBaseCommandData(instance, slashCommandClass, null));
		}
		return commands;
	}

	/**
	 * Gets the complete {@link SlashCommandData} (including Subcommands & Subcommand Groups) of a single {@link BaseSlashCommand}.
	 *
	 * @param command      The base command's instance.
	 * @param commandClass The base command's class.
	 * @param guild        The current guild (if available)
	 * @return The new {@link CommandListUpdateAction}.
	 * @throws Exception If an error occurs.
	 */
	private SlashCommandData getBaseCommandData(@NotNull BaseSlashCommand command, Class<? extends BaseSlashCommand> commandClass, @Nullable Guild guild) throws Exception {
		// find component (and modal) handlers
		this.findComponentAndModalHandlers(command);
		if (command.getCommandData() == null) {
			DIH4JDALogger.warn(String.format("Class %s is missing CommandData. It will be ignored.", commandClass.getName()));
			return null;
		}
		SlashCommandData commandData = command.getCommandData();
		if (command.getSubcommandGroups() != null) {
			commandData.addSubcommandGroups(this.getSubcommandGroupData(command, guild));
		}
		if (command.getSubcommands() != null) {
			commandData.addSubcommands(this.getSubcommandData(command, command.getSubcommands(), null, guild));
		}
		if (command.getSubcommandGroups() == null && command.getSubcommands() == null) {
			slashCommandIndex.put(buildCommandPath(commandData.getName()), new SlashCommandInteraction((SlashCommand) command, commandClass, command.getCommandPrivileges()));
			DIH4JDALogger.info(String.format("\t[*] Registered command: /%s", command.getCommandData().getName()), DIH4JDALogger.Type.SLASH_COMMAND_REGISTERED);
			if (command.shouldHandleAutoComplete()) {
				autoCompleteIndex.put(commandData.getName(), (AutoCompleteHandler) command);
				DIH4JDALogger.info("\t\t[^] Enabled AutoComplete Handling", DIH4JDALogger.Type.HANDLE_AUTOCOMPLETE);
			}
		}
		return commandData;
	}

	/**
	 * Gets all {@link SubcommandGroupData} (including Subcommands) of a single {@link BaseSlashCommand}.
	 *
	 * @param command The base command's instance.
	 * @param guild   The current guild (if available)
	 * @return All {@link SubcommandGroupData} stored in a List.
	 * @throws Exception If an error occurs.
	 */
	private Set<SubcommandGroupData> getSubcommandGroupData(@NotNull BaseSlashCommand command, @Nullable Guild guild) throws Exception {
		Set<SubcommandGroupData> groupDataList = new HashSet<>();
		for (Class<? extends SubcommandGroup> group : command.getSubcommandGroups()) {
			SubcommandGroup instance = (SubcommandGroup) this.getClassInstance(guild, group);
			if (instance.getSubcommandGroupData() == null) {
				DIH4JDALogger.warn(String.format("Class %s is missing SubcommandGroupData. It will be ignored.", group.getName()));
				continue;
			}
			if (instance.getSubcommands() == null) {
				DIH4JDALogger.warn(String.format("SubcommandGroup %s is missing Subcommands. It will be ignored.", instance.getSubcommandGroupData().getName()));
				continue;
			}
			SubcommandGroupData groupData = instance.getSubcommandGroupData();
			groupData.addSubcommands(this.getSubcommandData(command, instance.getSubcommands(), groupData.getName(), guild));
			groupDataList.add(groupData);
		}
		return groupDataList;
	}

	/**
	 * Gets all {@link SubcommandData} from the given array of {@link Subcommand} classes.
	 *
	 * @param command      The base command's instance.
	 * @param subClasses   All sub command classes.
	 * @param subGroupName The Subcommand Group's name. (if available)
	 * @param guild        The current guild (if available)
	 * @return The new {@link CommandListUpdateAction}.
	 * @throws Exception If an error occurs.
	 */
	private Set<SubcommandData> getSubcommandData(BaseSlashCommand command, Class<? extends Subcommand>[] subClasses, @Nullable String subGroupName, @Nullable Guild guild) throws Exception {
		Set<SubcommandData> subDataList = new HashSet<>();
		for (Class<? extends Subcommand> sub : subClasses) {
			Subcommand instance = (Subcommand) this.getClassInstance(guild, sub);
			if (instance.getSubcommandData() == null) {
				DIH4JDALogger.warn(String.format("Class %s is missing SubcommandData. It will be ignored.", sub.getName()));
				continue;
			}
			this.findComponentAndModalHandlers(instance);
			String commandPath;
			if (subGroupName == null) {
				commandPath = buildCommandPath(command.getCommandData().getName(), instance.getSubcommandData().getName());
			} else {
				commandPath = buildCommandPath(command.getCommandData().getName(), subGroupName, instance.getSubcommandData().getName());
			}
			slashCommandIndex.put(commandPath, new SlashCommandInteraction((SlashCommand) instance, sub, command.getCommandPrivileges()));
			DIH4JDALogger.info(String.format("\t[*] Registered command: /%s", commandPath), DIH4JDALogger.Type.SLASH_COMMAND_REGISTERED);
			if (instance.shouldHandleAutoComplete()) {
				autoCompleteIndex.put(commandPath, (AutoCompleteHandler) instance);
				DIH4JDALogger.info("\t\t[^] Enabled AutoComplete Handling", DIH4JDALogger.Type.HANDLE_AUTOCOMPLETE);
			}
			subDataList.add(instance.getSubcommandData());
		}
		return subDataList;
	}

	//TODO-v1.4: Documentation
	private void findComponentAndModalHandlers(ExecutableCommand command) {
		command.getHandledButtonIds().forEach(s -> this.buttonIndex.put(s, (ButtonHandler) command));
		command.getHandledSelectMenuIds().forEach(s -> this.selectMenuIndex.put(s, (SelectMenuHandler) command));
		command.getHandledModalIds().forEach(s -> this.modalIndex.put(s, (ModalHandler) command));
	}

	/**
	 * Gets all Guild Context commands registered in {@link InteractionHandler#findContextCommands()} and
	 * returns their {@link CommandData} as a List.
	 *
	 * @param guild The context command's guild.
	 * @throws Exception If an error occurs.
	 */
	private Set<CommandData> getGuildContextCommandData(@NotNull Guild guild) throws Exception {
		Set<CommandData> commands = new HashSet<>();
		for (Class<? extends GuildContextCommand> c : this.guildContexts) {
			GuildContextCommand instance = (GuildContextCommand) this.getClassInstance(guild, c);
			if (!instance.getGuilds(guild.getJDA()).contains(guild)) {
				DIH4JDALogger.info("Skipping Registration of " + c.getSimpleName() + " for Guild: " + guild.getName(), DIH4JDALogger.Type.CONTEXT_COMMAND_SKIPPED);
				continue;
			}
			commands.add(this.getContextCommandData(instance, c));
		}
		return commands;
	}

	/**
	 * Gets all Global Context commands registered in {@link InteractionHandler#findContextCommands()} and
	 * returns their {@link CommandData} as a List.
	 *
	 * @throws Exception If an error occurs.
	 */
	private Set<CommandData> getGlobalContextCommandData() throws Exception {
		Set<CommandData> commands = new HashSet<>();
		for (Class<? extends GlobalContextCommand> contextCommandClass : this.globalContexts) {
			GlobalContextCommand instance = (GlobalContextCommand) this.getClassInstance(null, contextCommandClass);
			CommandData data = this.getContextCommandData(instance, contextCommandClass);
			if (data != null) {
				commands.add(data);
			}
		}
		return commands;
	}

	/**
	 * Gets the complete {@link CommandData} from a single {@link BaseContextCommand}.
	 *
	 * @param command      The base context command's instance.
	 * @param commandClass The base context command's class.
	 * @return The new {@link CommandListUpdateAction}.
	 */
	private CommandData getContextCommandData(@NotNull BaseContextCommand command, Class<? extends BaseContextCommand> commandClass) {
		if (command.getCommandData() == null) {
			DIH4JDALogger.warn(String.format("Class %s is missing CommandData. It will be ignored.", commandClass.getName()));
			return null;
		}
		CommandData commandData = command.getCommandData();
		if (commandData.getType() == Command.Type.MESSAGE) {
			messageContextIndex.put(commandData.getName(), new MessageContextInteraction((MessageContextCommand) command));
		} else if (commandData.getType() == Command.Type.USER) {
			userContextIndex.put(commandData.getName(), new UserContextInteraction((UserContextCommand) command));
		} else {
			DIH4JDALogger.error(String.format("Invalid Command Type \"%s\" for Context Command! This command will be ignored.", commandData.getType()));
			return null;
		}
		DIH4JDALogger.info(String.format("\t[*] Registered context command: %s", command.getCommandData().getName()), DIH4JDALogger.Type.CONTEXT_COMMAND_REGISTERED);
		return commandData;
	}

	/**
	 * Handles a single {@link SlashCommandInteraction}.
	 * If a {@link SlashCommandInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link SlashCommandInteractionEvent} that was fired.
	 */
	private void handleSlashCommand(SlashCommandInteractionEvent event) {
		try {
			SlashCommandInteraction command = slashCommandIndex.get(event.getCommandPath());
			if (command == null) {
				throw new CommandNotRegisteredException(String.format("Slash Command \"%s\" is not registered.", event.getCommandPath()));
			} else {
				command.getHandler().handleSlashCommand(event);
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling a Slash Command: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Handles a single {@link UserContextInteraction}.
	 * If a {@link UserContextInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link UserContextInteractionEvent} that was fired.
	 */
	private void handleUserContextCommand(UserContextInteractionEvent event) {
		try {
			UserContextInteraction context = userContextIndex.get(event.getCommandPath());
			if (context == null) {
				throw new CommandNotRegisteredException(String.format("Context Command \"%s\" is not registered.", event.getCommandPath()));
			} else {
				context.getHandler().handleUserContextCommand(event);
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling a User Context Command: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Handles a single {@link MessageContextInteraction}.
	 * If a {@link MessageContextInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link MessageContextInteractionEvent} that was fired.
	 */
	private void handleMessageContextCommand(MessageContextInteractionEvent event) {
		try {
			MessageContextInteraction context = messageContextIndex.get(event.getCommandPath());
			if (context == null) {
				throw new CommandNotRegisteredException(String.format("Context Command \"%s\" is not registered.", event.getCommandPath()));
			} else {
				context.getHandler().handleMessageContextCommand(event);
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling a Message Context Command: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Handles a single {@link CommandAutoCompleteInteractionEvent}.
	 * If a {@link CommandAutoCompleteInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link CommandAutoCompleteInteractionEvent} that was fired.
	 */
	private void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
		try {
			AutoCompleteHandler component = autoCompleteIndex.get(event.getCommandPath());
			if (component != null) {
				component.handleAutoComplete(event, event.getFocusedOption());
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling an AutoComplete Interaction: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Handles a single {@link ButtonInteractionEvent}.
	 * If a {@link ButtonInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link ButtonInteractionEvent} that was fired.
	 */
	private void handleButton(ButtonInteractionEvent event) {
		try {
			ButtonHandler component = buttonIndex.get(ComponentIdBuilder.splitBySeparator(event.getComponentId())[0]);
			if (component == null) {
				DIH4JDALogger.warn(String.format("Button with id \"%s\" could not be found.", event.getComponentId()), DIH4JDALogger.Type.BUTTON_NOT_FOUND);
			} else {
				component.handleButton(event, event.getButton());
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling a Button Interaction: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Handles a single {@link SelectMenuInteractionEvent}.
	 * If a {@link SelectMenuInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link SelectMenuInteractionEvent} that was fired.
	 */
	private void handleSelectMenu(SelectMenuInteractionEvent event) {
		try {
			SelectMenuHandler component = selectMenuIndex.get(ComponentIdBuilder.splitBySeparator(event.getComponentId())[0]);
			if (component == null) {
				DIH4JDALogger.warn(String.format("Select Menu with id \"%s\" could not be found.", event.getComponentId()), DIH4JDALogger.Type.SELECT_MENU_NOT_FOUND);
			} else {
				component.handleSelectMenu(event, event.getValues());
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling a Select Menu Interaction: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Handles a single {@link ModalInteractionEvent}.
	 * If a {@link ModalInteractionEvent} is fired the corresponding class is found and the command is executed.
	 *
	 * @param event The {@link ModalInteractionEvent} that was fired.
	 */
	private void handleModal(ModalInteractionEvent event) {
		try {
			ModalHandler modal = modalIndex.get(ComponentIdBuilder.splitBySeparator(event.getModalId())[0]);
			if (modal == null) {
				DIH4JDALogger.warn(String.format("Modal with id \"%s\" could not be found.", event.getModalId()), DIH4JDALogger.Type.MODAL_NOT_FOUND);
			} else {
				modal.handleModal(event, event.getValues());
			}
		} catch (Throwable e) {
			DIH4JDALogger.warn("An Exception was raised while handling a Modal Interaction: " + e.getMessage(), DIH4JDALogger.Type.COMMAND_EXCEPTION);
		}
	}

	/**
	 * Used to create one command name out of the SlashCommand, SlashSubCommandGroup and SlashSubCommand
	 *
	 * @return One combined string.
	 */
	@Contract(pure = true)
	private @NotNull String buildCommandPath(String... args) {
		return String.join("/", args);
	}

	/**
	 * Creates a new Instance of the given class.
	 *
	 * @param guild The slash command's guild. (if available)
	 * @param clazz The slash command's class.
	 * @return The Instance as a generic Object.
	 * @throws Exception If an error occurs.
	 */
	private @NotNull Object getClassInstance(Guild guild, Class<?> clazz) throws ReflectiveOperationException {
		if (guild != null || !clazz.getSuperclass().equals(GlobalSlashCommand.class)) {
			try {
				return clazz.getConstructor(Guild.class).newInstance(guild);
			} catch (NoSuchMethodException ignored) {
			}
		}
		return clazz.getConstructor().newInstance();
	}

	/**
	 * Fired if Discord reports a {@link SlashCommandInteractionEvent}.
	 *
	 * @param event The {@link SlashCommandInteractionEvent} that was fired.
	 */
	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleSlashCommand(event));
	}


	/**
	 * Fired if Discord reports a {@link UserContextInteractionEvent}.
	 *
	 * @param event The {@link UserContextInteractionEvent} that was fired.
	 */
	@Override
	public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleUserContextCommand(event));
	}

	/**
	 * Fired if Discord reports a {@link MessageContextInteractionEvent}.
	 *
	 * @param event The {@link MessageContextInteractionEvent} that was fired.
	 */
	@Override
	public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleMessageContextCommand(event));
	}

	//TODO-v1.4: Documentation
	@Override
	public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleAutoComplete(event));
	}

	//TODO-v1.4: Documentation
	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleButton(event));
	}

	//TODO-v1.4: Documentation
	@Override
	public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleSelectMenu(event));
	}

	//TODO-v1.4: Documentation
	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		CompletableFuture.runAsync(() -> this.handleModal(event));
	}
}
