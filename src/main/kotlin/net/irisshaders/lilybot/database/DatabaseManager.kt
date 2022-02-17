package net.irisshaders.lilybot.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.utils.io.errors.*
import mu.KotlinLogging
import net.irisshaders.lilybot.utils.JDBC_URL
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path

/**
 * The Database system within the bot
 * @author chalkyjeans
 */
object DatabaseManager {
	private val logger = KotlinLogging.logger { }
	private val config = HikariConfig()
	private var dataSource: HikariDataSource

	init {
		config.jdbcUrl = JDBC_URL
		config.connectionTestQuery = "SELECT 1"
		config.addDataSourceProperty("cachePrepStmts", "true")
		config.addDataSourceProperty("prepStmtCacheSize", "250")
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

		dataSource = HikariDataSource(config)
		Database.connect(dataSource)

		logger.info("Connected to database.")
	}

	object Warn : Table("warn") {
		val id = text("id")
		val points = integer("points").nullable()

		override val primaryKey = PrimaryKey(id)
	}

	object Config : Table("config") {
		val guildId = text("guildId")
		val moderatorsPing = text("moderatorsPing")
		val modActionLog = text("modActionLog")
		val messageLogs = text("messageLogs")
		val joinChannel = text("joinChannel")
		val supportChanel = text("supportChannel").nullable()
		val supportTeam = text("supportTeam").nullable()

		override val primaryKey = PrimaryKey(guildId)
	}

	object Components : Table("components") {
		val componentId = text("componentId")
		val roleId = text("roleId")
		val addOrRemove = text("addOrRemove")

		override val primaryKey = PrimaryKey(componentId)
	}

	object Utilities : Table("utilities") {
		val status = text("status")
		val statusMessage = text("statusMessage")

		override val primaryKey = PrimaryKey(status)
	}

	fun startDatabase() {
		try {
			val database = Path.of("database.db")

			if (Files.notExists(database)) {
				Files.createFile(database)

				logger.info("Created database file.")
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}

		transaction {
			SchemaUtils.createMissingTablesAndColumns(Warn, Config, Components, Utilities)
		}
	}
}
