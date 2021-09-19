package net.developerden.devdenbot.commands.info

import net.developerden.devdenbot.commands.slash.DevDenSlashCommand
import net.developerden.devdenbot.extensions.await
import net.developerden.devdenbot.inject.Used
import net.developerden.devdenbot.serialization.DDBConfig
import net.developerden.devdenbot.trait.HasConfig
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction
import java.net.URL

/**
 * @author Kyle h
 */

@Used
class StackOverflowCommand constructor(
    override val ddbConfig: DDBConfig,
) : DevDenSlashCommand(
    name = "stackoverflow",
    description = "A helpful way to find answers using stackoverflow"
), HasConfig {
    override suspend fun load(action: CommandCreateAction) = Unit

    override suspend fun SlashCommandEvent.execute() {
        val question = getOption("question")?.asString ?: error("Invalid question")
        val response = URL(question.replace("https://api.stackexchange.com/2.3/search/advanced?order=desc&sort=activity&q=$question",
            "%20")).readText()
        reply("Searching stackoverflow for $question").await()
        reply("Response: $response").await()
    }
}