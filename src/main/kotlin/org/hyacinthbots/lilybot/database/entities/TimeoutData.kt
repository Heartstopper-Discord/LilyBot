package org.hyacinthbots.lilybot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * The data for timeout logs in guilds.
 *.
 * @property userId The ID of the user with timeout history
 * @property guildId The ID of the guild they received the timeout in
 * @property reason The given reason for a timeout, if any
 * @property datetime Exactly when the timeout was given
 * @property duration The duration of the timeout
 * @since 3.0.0
 */
@Serializable
data class TimeoutData(
	val userId: Snowflake,
	val guildId: Snowflake,
	val reason: String?,
	val datetime: Instant,
	val duration: Instant
)
