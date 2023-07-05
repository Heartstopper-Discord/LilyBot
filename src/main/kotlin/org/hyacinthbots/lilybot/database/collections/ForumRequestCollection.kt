package org.hyacinthbots.lilybot.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.hyacinthbots.lilybot.database.Database
import org.hyacinthbots.lilybot.database.entities.ForumRequestData
import org.koin.core.component.inject
import org.litote.kmongo.eq

class ForumRequestCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mainDatabase.getCollection<ForumRequestData>()

	suspend fun getAllForumRequests() = collection.find().toList()
	suspend fun setForumRequest(forumRequestData: ForumRequestData) = collection.insertOne(forumRequestData)
	suspend fun getForumRequest(guildId: Snowflake, id: Long) =
		collection.findOne(
	    ForumRequestData::guildId eq guildId,
		ForumRequestData::id eq id
	)
	suspend fun removeForumRequest(userId: Snowflake, guildId: Snowflake, id: Long) =
		collection.deleteOne(
		    ForumRequestData::userId eq userId, ForumRequestData::guildId eq guildId,
			ForumRequestData::id eq id
		)

	suspend inline fun clearForumRequests(inputGuildId: Snowflake) =
		collection.deleteMany(ForumRequestData::guildId eq inputGuildId)
}
