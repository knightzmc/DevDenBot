package net.developerden.devdenbot

import com.google.inject.Guice
import com.jagrosh.jdautilities.command.CommandClient
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.misfitlabs.kotlinguice4.getInstance
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.developerden.devdenbot.commands.CommandsModule
import net.developerden.devdenbot.commands.DevDenCommand
import net.developerden.devdenbot.commands.slash.DevDenSlashCommand
import net.developerden.devdenbot.data.Users
import net.developerden.devdenbot.events.EventsTables
import net.developerden.devdenbot.extensions.await
import net.developerden.devdenbot.faq.FAQs
import net.developerden.devdenbot.graphics.GraphicsContext
import net.developerden.devdenbot.inject.DevDenModule
import net.developerden.devdenbot.leaderboard.Leaderboards
import net.developerden.devdenbot.listener.ListenersModule
import net.developerden.devdenbot.listener.RoleChangeListener
import net.developerden.devdenbot.serialization.DDBConfig
import net.developerden.devdenbot.util.log
import net.developerden.devdenbot.util.scope
import net.developerden.devdenbot.xp.VoiceChatXPTask
import net.dv8tion.jda.api.JDA
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class DevDen {
    private val log by log()

    private fun load() {
        val config = Json.decodeFromString(DDBConfig.serializer(), javaClass.getResource("/config.json")!!.readText())
            .let {
                it.copy(token = System.getenv("DDB_TOKEN") ?: it.token)
            }

        val loadStatsTime = measureTimeMillis {
            runBlocking {
                loadDatabase()
                Leaderboards.initializeLeaderboards()
            }
        }

        log.debug { "Loading stats and connecting to database took $loadStatsTime ms." }

        val injector = Guice.createInjector(DevDenModule(config), CommandsModule(), ListenersModule())

        val jda = injector.getInstance<JDA>()

        val commandClient = injector.getInstance<CommandClient>()
        val commands = injector.getInstance<Set<DevDenCommand>>()

        commands.forEach {
            commandClient.addCommand(it)
            log.debug("Registered ${it.javaClass.name}!")
        }
        jda.awaitReady()
        GraphicsContext.init()
        startTasks(jda)
        updateRoles(jda)

        val slashCommands = injector.getInstance<Set<DevDenSlashCommand>>()
        scope.launch {
            slashCommands.forEach {
                it.register(jda)
                log.debug("Registered ${it.javaClass.name}!")
            }
        }
    }

    fun start() {
        Sentry.init() // This will get the DSN from the SENTRY_DSN environment variable
        try {
            load()
        } catch (e: Exception) {
            Sentry.captureException(e)
            e.printStackTrace()
        }
    }


    private suspend fun loadDatabase() {
        val hikariConfig = if (System.getenv("DDB_MOCK_DB") != null) {
            HikariConfig().apply {
                jdbcUrl = "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"
                driverClassName = "org.h2.Driver"
            }
        } else {
            val host = System.getenv("DDB_DB_HOST") ?: "localhost"
            val db = System.getenv("DDB_DB_NAME")
            val dbUsername = System.getenv("DDB_DB_USERNAME") ?: "root"
            val dbPassword = System.getenv("DDB_DB_PASSWORD")
            HikariConfig().apply {
                jdbcUrl = "jdbc:mysql://$host:3306/$db"
                driverClassName = "com.mysql.jdbc.Driver"
                username = dbUsername
                password = dbPassword
            }
        }
        Database.connect(HikariDataSource(hikariConfig))
        newSuspendedTransaction {
            SchemaUtils.create(Users, FAQs, *EventsTables)
        }
    }

    private fun startTasks(jda: JDA) {
        log.info { "Starting tasks..." }
        val timer = Timer()
        timer.schedule(VoiceChatXPTask(jda), 0L, TimeUnit.SECONDS.toMillis(60))
    }

    private fun updateRoles(jda: JDA) {
        jda.guilds.forEach { guild ->
            scope.launch {
                guild.loadMembers().await().forEach {
                    RoleChangeListener.update(it)
                }
            }
        }
    }
}
