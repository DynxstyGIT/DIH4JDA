package com.dynxsty.dih4jda.interactions.components;

import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface SelectMenuHandler {
	/**
	 * Method that must be overridden for all Classes that handle Select Menu Interactions.
	 * All select menus that match the identifier will execute this class's implementation of the handleSelectMenu method.
	 *
	 * <pre>{@code
	 * public class TestCommand extends SlashCommand implements SelectMenuHandler {
	 *
	 *     public TestCommand() {
	 *         setSlashCommandData(Commands.slash("test", "test description"));
	 *     }
	 *
	 *    @Override
	 *    public void execute(SlashCommandInteractionEvent event) {
	 * 		List<Role> roles = [...]
	 * 		SelectMenu.Builder menu = SelectMenu.create("test-select-menu");
	 * 		for (Role role : roles) {
	 * 			menu.addOption(role.getName(), role.getId());
	 *        }
	 * 		event.reply("Choose your rank!").addActionRow(menu.build()).queue();
	 *    }
	 *
	 *    @Override
	 *    public void handleSelectMenu(SelectMenuInteractionEvent event, List<String> values) {
	 * 		for (String roleId : values) {
	 * 			event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleId)).queue();
	 *        }
	 * 		event.reply("Successfully added " + String.join(", ", event.getValues())).queue();
	 *    }
	 * }}</pre>
	 *
	 * @see com.dynxsty.dih4jda.DIH4JDA#addSelectMenuHandlers(Map)
	 * @since v1.4
	 */
	void handleSelectMenu(@NotNull SelectMenuInteractionEvent event, @NotNull List<String> values);
}
