package net.developerden.devdenbot.listener

import net.developerden.devdenbot.discord.BOT_COMMANDS_CHANNEL_ID
import net.developerden.devdenbot.extensions.await
import net.developerden.devdenbot.serialization.DDBConfig
import net.developerden.devdenbot.util.handleEachIn
import net.developerden.devdenbot.util.listenFlow
import net.developerden.devdenbot.util.scope
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import javax.inject.Inject

class DiscordTokenMessageListener @Inject constructor(
    private val ddbConfig: DDBConfig,
    ) : EventListener {

    companion object {
        private val log by log()

        internal val tokenRegex = Regex("[MN][A-Za-z\\d]{23}\\.[\\w-]{6}\\.[\\w-]{27}\n")
    }

    private suspend fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) {
            return
        }
        if (event.channel.idLong == BOT_COMMANDS_CHANNEL_ID) {
            return
        }
        val rawText = event.message.contentRaw
        if (rawText.startsWith("${ddbConfig.prefix}keep")) {
            return
        }
        if (rawText.startsWith("/run")){
            return
        }
        if (!rawText.contains(tokenRegex)) {
            return
        }
        if (rawText.contains(tokenRegex)) {
            event.message.delete()
            event.channel.sendMessage("The previous message has been deleted due to").await()
            event.channel.sendMessage("it containing a discord bot token!").await()
            log.debug {
                "Deleted message " + event.messageId + " due to it containing a bot token"
            }
        }
    }

    override fun register(jda: JDA) {
        jda.listenFlow<GuildMessageReceivedEvent>().handleEachIn(scope, this::onGuildMessageReceived)
    }
}