package org.hyacinthbots.lilybot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * The data for warnings in guilds.
 *.
 * @property id The ID of the warning
 * @property userId The ID of the user with warnings
 * @property guildId The ID of the guild they received the warning in
 * @property reason The given reason for a warning, if any
 * @property datetime Exactly when the warning was given
 * @since 3.0.0
 */
@Serializable
data class WarnData(
	val id: Long,
	val userId: Snowflake,
	val guildId: Snowflake,
	val reason: String?,
	val datetime: Instant
)
