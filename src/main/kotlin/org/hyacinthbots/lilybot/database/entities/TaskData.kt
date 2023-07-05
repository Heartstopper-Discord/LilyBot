package org.hyacinthbots.lilybot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * THe data for tasks in a guild.
 *
 * @property guildId The ID of the guild the reminder was set in
 * @property runTime The time the reminder was set
 * @property userId The user that set the reminder
 * @since 4.2.0
 */

// Currently only handles "New Role" tasks
@Serializable
data class TaskData(
	val guildId: Snowflake,
	val userId: Snowflake,
	val runTime: Instant
)
