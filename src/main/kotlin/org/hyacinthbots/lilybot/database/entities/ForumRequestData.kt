package org.hyacinthbots.lilybot.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class ForumRequestData(
	val guildId: Snowflake,
	val userId: Snowflake,
	val title: String,
	val id: Long
)
