package org.hyacinthbots.lilybot.extensions.util

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.boolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.long
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import dev.kord.common.entity.ArchiveDuration
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOfOrNull
import dev.kord.core.entity.channel.ForumChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.request.KtorRequestException
import kotlinx.datetime.Clock
import org.hyacinthbots.lilybot.database.collections.ForumRequestCollection
import org.hyacinthbots.lilybot.database.collections.UtilityConfigCollection
import org.hyacinthbots.lilybot.database.entities.ForumRequestData

class ForumRequests : Extension() {
	override val name = "forum-requests"

	override suspend fun setup() {
		ephemeralSlashCommand(::ForumRequestArgs) {
			name = "forum-request"
			description = "Lets you request that a new forum thread get made"

			action {
				val utilConfig = UtilityConfigCollection().getConfig(guild!!.id)

				if (utilConfig?.forumId == null) {
					return@action
				}

				val id = ForumRequestCollection().getAllForumRequests().count() + 1
				ForumRequestCollection().setForumRequest(
					ForumRequestData(
						guild!!.id,
						user.id,
						arguments.title,
						id.toLong()
					)
				)

				val utilityLog = guild?.getChannelOfOrNull<GuildMessageChannel>(utilConfig.utilityLogChannel!!)
				val requester = user.asUserOrNull()

				respond { content = "Forum thread request sent!" }

				try {
					utilityLog?.createMessage {
						embed {
							color = DISCORD_YELLOW
							title = "Forum Thread Request"
							timestamp = Clock.System.now()

							field {
								name = "User:"
								value =
									"${requester?.mention}\n${requester?.asUserOrNull()?.username}\n${requester?.id}"
								inline = false
							}

							field {
								name = "Requested thread title:"
								value = "`${arguments.title}`"
								inline = false
							}

							field {
								name = "Stated reason:"
								value = "`${arguments.description}`"
								inline = false
							}

							field {
								name = "Forum Request ID:"
								value = "$id"
								inline = false
							}
						}
					}
					return@action
				} catch (e: KtorRequestException) {
					respond {
						content = "Error sending message to moderators. Please ask the moderators to check" +
								"the `UTILITY` config."
					}
					return@action
				}
			}
		}

		ephemeralSlashCommand(::ForumResponseArgs) {
			name = "forum-response"
			description = "Lets moderators accept/reject thread requests"

			requirePermission(Permission.ModerateMembers)

			check {
				anyGuild()
				hasPermission(Permission.ModerateMembers)
				requireBotPermissions(Permission.ManageChannels)
			}

			action {
				val req = ForumRequestCollection().getForumRequest(guild!!.id, arguments.id)
				if (req == null) {
					respond { content = "No forum thread request by that ID found." }
					return@action
				}

				val requester = guild!!.getMemberOrNull(req.userId)
				if (requester == null) {
					respond { content = "Requesting user not found." }
					return@action
				}
				val utilConfig = UtilityConfigCollection().getConfig(guild!!.id)
				val utilityLog = guild?.getChannelOfOrNull<GuildMessageChannel>(utilConfig?.utilityLogChannel!!)

				if (arguments.approve) {
					val parentChannel = guild?.getChannelOfOrNull<ForumChannel>(utilConfig?.forumId!!)

					if (parentChannel == null) {
						respond { content = "Failed to find thread channel." }
						return@action
					}

					try {
						parentChannel.startPublicThread(req.title) {
							autoArchiveDuration =
								parentChannel.data.defaultAutoArchiveDuration.value ?: ArchiveDuration.Day
							message(requester.mention)
						}
						ForumRequestCollection().removeForumRequest(req.userId, req.guildId, req.id)
					} catch (e: KtorRequestException) {
						return@action
					}
					// create channel
					try {
						utilityLog?.createMessage {
							embed {
								color = DISCORD_GREEN
								title = "Forum Thread Approved"
								timestamp = Clock.System.now()

								field {
									name = "By:"
									value =
										"${user.mention}\n${user.asUser().username}\n${user.id}"
									inline = false
								}

								field {
									name = "Approved thread title:"
									value = "`${req.title}`"
									inline = false
								}
							}
						}
						ForumRequestCollection().removeForumRequest(req.userId, req.guildId, req.id)
						respond { content = "Forum request approved." }
						return@action
					} catch (e: KtorRequestException) {
						return@action
					}
				} else {
					try {
						requester.dm {
							embed {
								title = "Forum Thread Request Denied"
								description = "Moderators have reviewed your thread request (`${
									req.title
								}`) and rejected it for the following reason: \n ${if (arguments.reason == null) "N/A" else arguments.reason}"
							}
						}

						utilityLog?.createMessage {
							embed {
								color = DISCORD_RED
								title = "Forum Thread Denied"
								timestamp = Clock.System.now()

								field {
									name = "By:"
									value =
										"${user.mention}\n${user.asUser().username}\n${user.id}"
									inline = false
								}

								field {
									name = "Denied thread title:"
									value = "`${req.title}`"
									inline = false
								}

								field {
									name = "Reason:"
									value = "${arguments.reason}"
									inline = false
								}
							}
						}

						ForumRequestCollection().removeForumRequest(req.userId, req.guildId, req.id)

						respond { content = "Forum request denied." }

						return@action
					} catch (e: KtorRequestException) {
						respond { content = "User could not be DMed." }
						return@action
					}
				}
			}
		}
	}
	inner class ForumRequestArgs : Arguments() {
		/** The user to ban. */
		val title by string {
			name = "title"
			description = "Title of the forum thread you want"
		}

		/** The number of days worth of messages to delete. */
		val description by string {
			name = "description"
			description = "Explanation of the purpose of the forum thread"
		}
	}

	inner class ForumResponseArgs : Arguments() {

		val id by long {
			name = "forum-request-id"
			description = "ID of the forum request to accept/reject"
		}

		val approve by boolean {
			name = "approve"
			description = "Whether or not the forum thread should be accepted"
		}

		val reason by optionalString {
			name = "reason"
			description = "Explanation of why forum thread was rejected"
		}
	}
}
