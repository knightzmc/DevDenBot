package net.developerden.devdenbot.commands.info

import net.developerden.devdenbot.commands.slash.DevDenSlashCommand
import net.developerden.devdenbot.extensions.await
import net.developerden.devdenbot.inject.Used
import net.developerden.devdenbot.pasting.HasteClient
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction

@Used
class PasteCommand : DevDenSlashCommand(
    name = "paste",
    description = "Show the paste link",
) {
    override suspend fun load(action: CommandCreateAction) = Unit

    override suspend fun SlashCommandEvent.execute() {
        reply(HasteClient.baseUrl).await()
    }
}
