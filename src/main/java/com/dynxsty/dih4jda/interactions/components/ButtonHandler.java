package com.dynxsty.dih4jda.interactions.components;

import com.dynxsty.dih4jda.interactions.ComponentIdBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ButtonHandler {
	/**
	 * Method that must be overridden for all Commands that handle Button Interactions.
	 * All buttons that match the identifier will execute this class's implementation of the handleButton method.
	 * This is best used with the {@link ComponentIdBuilder}.
	 *
	 * <pre>{@code
	 * public class TestCommand extends SlashCommand implements ButtonHandler {
	 *
	 *     public TestCommand() {
	 *         setCommandData(Commands.slash("test", "test description"));
	 *     }
	 *
	 *    @Override
	 *    public void execute(SlashCommandInteractionEvent event) {
	 * 		event.reply("test")
	 * 				.addActionRow(
	 * 						Button.secondary(ComponentIdBuilder.build("test-button", 1), "Click me!"),
	 * 						Button.secondary(ComponentIdBuilder.build("test-button", 2), "NO! Click me!")
	 * 				).queue();
	 *    }
	 *
	 *     @Override
	 *     public void handleButton(ButtonInteractionEvent event, Button button) {
	 * 		String[] id = ComponentIdBuilder.split(button.getId());
	 * 		String content = "";
	 * 		switch (id[1]) {
	 * 			case "1": content = "Thanks for not clicking the other button! :)"; break;
	 * 			case "2": content = "Phew, thanks for clicking me..."; break;
	 *         }
	 * 		event.reply(content).queue();
	 *    }
	 * }}</pre>
	 *
	 * @see com.dynxsty.dih4jda.DIH4JDA#addButtonHandlers(Map)
	 * @since v1.4
	 */
	void handleButton(@NotNull ButtonInteractionEvent event, @NotNull Button button);
}
