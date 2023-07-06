package org.hyacinthbots.lilybot.extensions.util

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.botHasPermissions
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.rest.request.KtorRequestException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.hyacinthbots.lilybot.database.collections.TaskCollection
import org.hyacinthbots.lilybot.database.collections.UtilityConfigCollection
import org.hyacinthbots.lilybot.database.entities.TaskData

class Tasks : Extension() {
	override val name = "task"

	/** The scheduler that will track the time for running tasks. */
	private val taskScheduler = Scheduler()

	/** The task that will run the [taskScheduler]. */
	private lateinit var taskTask: Task

	override suspend fun setup() {
		taskTask = taskScheduler.schedule(30, repeat = true, callback = ::performTasks)

		event<MemberJoinEvent> {
			check {
				anyGuild()
				failIf { event.member.id == kord.selfId }
			}
			action {
				val utilConfig = UtilityConfigCollection().getConfig(event.guildId)
				if (utilConfig?.welcomeRole == null) {
					return@action
				}
				val delay = utilConfig.welcomeRoleDelay

				val triggerTime = if (delay == null) Clock.System.now() else Clock.System.now().plus(delay, TimeZone.UTC)

				TaskCollection().setTask(
					TaskData(
						event.guildId,
						event.member.id,
						triggerTime
					)
				)
			}
		}
	}

	/**
	 * Checks the database to see if reminders need posting and posts them if necessary.
	 *
	 * @author NoComment1105
	 * @since 4.2.0
	 */
	private suspend fun performTasks() {
		val tasks = TaskCollection().getAllTasks()
		val dueTasks =
			tasks.filter { it.runTime.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds() <= 0 }

		for (it in dueTasks) {
			var guild: Guild?
			try {
				guild = kord.getGuildOrNull(it.guildId)
			} catch (_: KtorRequestException) {
				continue
			}
			if (guild == null) {
				TaskCollection().removeTask(it.userId, it.guildId)
				continue
			}

			val member = guild.getMemberOrNull(it.userId)
			if (member == null) {
				TaskCollection().removeTask(it.userId, guild.id)
				continue
			}

			val utilConfig = UtilityConfigCollection().getConfig(guild.id)

			if (utilConfig?.welcomeRole == null) {
				TaskCollection().removeTask(it.userId, guild.id)
				continue
			}

			val role = guild.getRoleOrNull(utilConfig.welcomeRole)
			if (role == null) {
				TaskCollection().removeTask(it.userId, guild.id)
				continue
			}

			val hasPerms =
				guild.botHasPermissions(Permission.ManageRoles)

			if (hasPerms) {
				member.edit {
					this@edit.roles = member.roleIds.toMutableSet()
					this@edit.roles!!.add(role.id)
				}
				TaskCollection().removeTask(it.userId, guild.id)
			} else {
				TaskCollection().removeTask(it.userId, guild.id)
			}
		}
	}
}
