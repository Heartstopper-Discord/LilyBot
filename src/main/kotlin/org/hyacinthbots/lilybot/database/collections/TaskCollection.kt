package org.hyacinthbots.lilybot.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.hyacinthbots.lilybot.database.Database
import org.hyacinthbots.lilybot.database.entities.TaskData
import org.koin.core.component.inject
import org.litote.kmongo.eq

class TaskCollection : KordExKoinComponent {
	private val db: Database by inject()

	@PublishedApi
	internal val collection = db.mainDatabase.getCollection<TaskData>()

	/**
	 * Gets all the tasks currently in the database.
	 *
	 * @return A list of tasks in the database
	 */
	suspend fun getAllTasks(): List<TaskData> = collection.find().toList()

	suspend fun setTask(taskData: TaskData) = collection.insertOne(taskData)

	/**
	 * Removes a task from the database.
	 *
	 * @param userId The ID of the user the task belongs too
	 * @author NoComment1105
	 * @since 4.2.0
	 */
	suspend fun removeTask(userId: Snowflake, guildId: Snowflake) =
		collection.deleteOne(TaskData::userId eq userId, TaskData::guildId eq guildId)

	suspend inline fun clearTasks(inputGuildId: Snowflake) =
		collection.deleteMany(TaskData::guildId eq inputGuildId)
}
