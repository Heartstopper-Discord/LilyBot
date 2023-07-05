package org.hyacinthbots.lilybot.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import org.hyacinthbots.lilybot.database.Database
import org.hyacinthbots.lilybot.database.entities.TimeoutData
import org.koin.core.component.inject
import org.litote.kmongo.eq

/**
 * This class stores all the functions for interacting with the [Timeout Database][TimeoutData]. The class contains the
 * functions for querying, adding and removal of timeouts for a user.
 *
 * @since 4.0.0
 * @see getTimeouts
 * @see setTimeout
 * @see clearTimeouts
 */
class TimeoutCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mainDatabase.getCollection<TimeoutData>()

	/**
	 * Gets a list of timeouts for a provided [inputUserId] in the provided [inputGuildId] from the database.
	 *
	 * @param inputUserId The ID of the user to get timeouts for
	 * @param inputGuildId The ID of the guild the command was run in
	 * @return null or the result from the database
=	 */
	suspend inline fun getTimeouts(inputUserId: Snowflake, inputGuildId: Snowflake): List<TimeoutData> =
		collection.find(
			TimeoutData::userId eq inputUserId,
			TimeoutData::guildId eq inputGuildId
		).toList()

	suspend fun setTimeout(
        inputUserId: Snowflake,
        inputGuildId: Snowflake,
        reason: String?,
        datetime: Instant,
        duration: Instant
    ) =
		collection.insertOne(
			TimeoutData(
				inputUserId,
				inputGuildId,
				reason,
				datetime,
				duration
			)
		)

	/**
	 * Clears all warnings for the provided [inputGuildId].
	 *
	 * @param inputGuildId The ID of the guild the command was run in
	 * @author tempest15
	 * @since 3.0.0
	 */
	suspend inline fun clearTimeouts(inputGuildId: Snowflake) =
		collection.deleteMany(TimeoutData::guildId eq inputGuildId)
}
