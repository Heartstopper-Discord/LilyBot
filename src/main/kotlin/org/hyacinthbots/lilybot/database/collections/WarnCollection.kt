package org.hyacinthbots.lilybot.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import org.hyacinthbots.lilybot.database.Database
import org.hyacinthbots.lilybot.database.entities.WarnData
import org.koin.core.component.inject
import org.litote.kmongo.eq

/**
 * This class stores all the functions for interacting with the [Warn Database][WarnData]. The class contains the
 * functions for querying, adding and removal of warnings for a user.
 *
 * @since 4.0.0
 * @see getWarns
 * @see setWarn
 * @see clearWarns
 */
class WarnCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mainDatabase.getCollection<WarnData>()

	/**
	 * Gets a list of warnings for a provided [inputUserId] in the provided [inputGuildId] from the database.
	 *
	 * @param inputUserId The ID of the user to get warnings for
	 * @param inputGuildId The ID of the guild the command was run in
	 * @return null or the result from the database
	 * @author tempest15
	 * @since 3.0.0
	 */
	suspend inline fun getWarns(inputUserId: Snowflake, inputGuildId: Snowflake): List<WarnData> =
		collection.find(
			WarnData::userId eq inputUserId,
			WarnData::guildId eq inputGuildId
		).toList()

	suspend fun setWarn(inputUserId: Snowflake, inputGuildId: Snowflake, reason: String?) =
		collection.insertOne(
			WarnData(
				(getWarns(inputUserId, inputGuildId).maxByOrNull { it.id }?.id ?: 0) + 1,
				inputUserId,
				inputGuildId,
				reason,
				Clock.System.now()
			)
		)

	/*
	suspend fun loadWarn(inputUserId: Snowflake, inputGuildId: Snowflake, reason: String?, time: Instant) =
		collection.insertOne(
			WarnData(
				(getWarns(inputUserId, inputGuildId).maxByOrNull { it.id }?.id ?: 0) + 1,
				inputUserId,
				inputGuildId,
				reason,
				time
			)
		)
*/
	suspend fun removeWarn(inputUserId: Snowflake, inputGuildId: Snowflake, id: Long) =
		collection.deleteOne(WarnData::userId eq inputUserId, WarnData::guildId eq inputGuildId, WarnData::id eq id)

	/**
	 * Clears all warnings for the provided [inputGuildId].
	 *
	 * @param inputGuildId The ID of the guild the command was run in
	 * @author tempest15
	 * @since 3.0.0
	 */
	suspend inline fun clearWarns(inputGuildId: Snowflake) =
		collection.deleteMany(WarnData::guildId eq inputGuildId)
}
