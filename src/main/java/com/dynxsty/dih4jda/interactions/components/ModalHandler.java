package com.dynxsty.dih4jda.interactions.components;

import com.dynxsty.dih4jda.interactions.ComponentIdBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface ModalHandler {
	/**
	 * Method that must be overridden for all Commands that handle Modal Interactions.
	 * All modals that match the identifier will execute this class's implementation of the handleModal method.
	 * This is best used with the {@link ComponentIdBuilder}.
	 *
	 * <pre>{@code
	 * public class TestCommand extends SlashCommand implements ModalHandler {
	 *
	 *     public TestCommand() {
	 *         setSlashCommandData(Commands.slash("test", "test description"));
	 *     }
	 *
	 *     @Override
	 *     public void execute(SlashCommandInteractionEvent event) {
	 * 		Role applied = [...]
	 * 		TextInput email = TextInput.create("email", "Email", TextInputStyle.SHORT)
	 * 				.setPlaceholder("Enter your E-mail")
	 * 				.setMinLength(10)
	 * 				.setMaxLength(100)
	 * 				.build();
	 *
	 * 		TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
	 * 				.setPlaceholder("Your application goes here")
	 * 				.setMinLength(30)
	 * 				.setMaxLength(1000)
	 * 				.build();
	 *
	 * 		Modal modal = Modal.create(ComponentIdBuilder.build("test-modal", applied.getIdLong()), "Apply for " + applied.getName())
	 * 				.addActionRows(ActionRow.of(email), ActionRow.of(body))
	 * 				.build();
	 * 		event.replyModal(modal).queue();
	 *    }
	 *
	 *    @Override
	 *    public void handleModal(ModalInteractionEvent event, List<ModalMapping> values) {
	 *      Role role = event.getGuild().getRoleById(ComponentIdBuilder.split(event.getModalId())[1]);
	 * 		String email = event.getValue("email").getAsString();
	 * 		String body = event.getValue("body").getAsString();
	 *
	 * 		createApplication(role, email, body);
	 *
	 * 		event.reply("Thanks for your application!").queue();
	 *    }
	 * }}</pre>
	 *
	 * @see com.dynxsty.dih4jda.DIH4JDA#addModalHandlers(Map)
	 * @since v1.4
	 */
	void handleModal(@NotNull ModalInteractionEvent event, @NotNull List<ModalMapping> values);
}
