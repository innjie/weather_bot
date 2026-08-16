package com.weatherbot.bot

import com.weatherbot.bot.commands.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

/** 등록된 [SlashCommand] 목록을 이름으로 라우팅한다. 새 커맨드는 [commands]에 추가하면 된다. */
class CommandListener(commands: List<SlashCommand>) : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(CommandListener::class.java)
    private val commandsByName = commands.associateBy { it.name }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = commandsByName[event.name]
        if (command == null) {
            logger.warn("등록되지 않은 커맨드 호출: ${event.name}")
            event.reply("알 수 없는 커맨드입니다.").setEphemeral(true).queue()
            return
        }

        try {
            command.execute(event)
        } catch (e: Exception) {
            logger.error("커맨드 처리 중 오류 발생: ${event.name}", e)
            val message = "커맨드 처리 중 오류가 발생했습니다: ${e.message}"
            if (event.isAcknowledged) {
                event.hook.sendMessage(message).setEphemeral(true).queue()
            } else {
                event.reply(message).setEphemeral(true).queue()
            }
        }
    }
}
