package net.developerden.devdenbot.listener

import club.minnced.jda.reactor.onMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import net.developerden.devdenbot.data.StatsUsers
import net.developerden.devdenbot.discord.BUMP_NOTIFICATIONS_ROLE_ID
import net.developerden.devdenbot.extensions.await
import net.developerden.devdenbot.inject.Used
import net.developerden.devdenbot.util.handleEachIn
import net.developerden.devdenbot.util.listenFlow
import net.developerden.devdenbot.util.log
import net.developerden.devdenbot.util.scope
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.concurrent.TimeUnit

@Used
class BumpNotificationListener : EventListener {

    companion object {
        private const val DISBOARD_BOT_ID = 302050872383242240
        private val BUMP_COOLDOWN = TimeUnit.HOURS.toMillis(2)
        private val log by log()
    }

    private suspend fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.message.contentRaw != "!d bump") {
            return
        }

        val nextDisboardMessages = event.channel.onMessage()
            .asFlow()
            .filter { it.author.idLong == DISBOARD_BOT_ID }
            .map { it.message.embeds }
            .filter { it.isNotEmpty() }
            .firstOrNull() ?: return

        val nextDisboardMessage = nextDisboardMessages.firstOrNull() ?: return

        if (nextDisboardMessage.description?.contains(":thumbsup:") != true) {
            return
        }

        val user = StatsUsers.get(event.author.idLong)

        user.setBumps(user.bumps + 1)
        log.info {
            "Increased bump stat for user ${event.author.name} from ${
                user.bumps - 1
            } to ${user.bumps}."
        }

        delay(BUMP_COOLDOWN)
        val bumpNotificationRole = requireNotNull(event.jda.getRoleById(BUMP_NOTIFICATIONS_ROLE_ID)) {
            "Bump Notifications role not found"
        }
        log.trace { "Sending bump ready notification to users with the bump notification role" }

        event.channel.sendMessage(
            "${bumpNotificationRole.asMention}, the server is ready to be bumped! **!d bump**"
        ).await()
        return
    }

    override fun register(jda: JDA) {
        jda.listenFlow<GuildMessageReceivedEvent>().handleEachIn(scope, this::onGuildMessageReceived)
    }
}
