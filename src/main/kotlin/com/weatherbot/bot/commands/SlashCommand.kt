package com.weatherbot.bot.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

/** 슬래시 커맨드 하나를 나타낸다. 새 커맨드 추가 시 이 인터페이스를 구현하고 [CommandListener]에 등록한다. */
interface SlashCommand {
    val name: String
    fun data(): CommandData
    fun execute(event: SlashCommandInteractionEvent)
}
