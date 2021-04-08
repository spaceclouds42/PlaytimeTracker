package us.spaceclouds42.playtime_tracker.command

import com.github.p03w.aegis.aegisCommand
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.basiqueevangelist.nevseti.OfflineAdvancementUtils
import me.basiqueevangelist.nevseti.OfflineDataCache
import me.basiqueevangelist.nevseti.OfflineNameCache
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import us.spaceclouds42.ekho.ekho
import us.spaceclouds42.playtime_tracker.Context
import us.spaceclouds42.playtime_tracker.Node
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer
import us.spaceclouds42.playtime_tracker.extension.prettyPrint
import us.spaceclouds42.playtime_tracker.extension.toPlayer
import us.spaceclouds42.playtime_tracker.extension.toTime
import us.spaceclouds42.playtime_tracker.mixin.access.IAccessPlayerManager
import us.spaceclouds42.playtime_tracker.util.AdvancementHelper

class PlaytimeCommand {
    fun register(): Node {
        return aegisCommand("playtime") {
            requires {
                it.hasPermissionLevel(2)
            }

            literal("help") {
                executes {
                    helpCommand(
                        it,
                        null,
                    )
                }

                literal("alltime") {
                    executes {
                        helpCommand(
                            it,
                            null,
                            temp=false,
                        )
                    }

                    literal("player") {
                        executes {
                            helpCommand(
                                it,
                                "player",
                            )
                        }
                    }

                    literal("top") {
                        executes {
                            helpCommand(
                                it,
                                "top",
                            )
                        }
                    }

                    literal("reset") {
                        executes {
                            helpCommand(
                                it,
                                "reset",
                            )
                        }
                    }
                }

                literal("temp") {
                    executes {
                        helpCommand(
                            it,
                            null,
                            temp=true,
                        )
                    }

                    literal("player") {
                        executes {
                            helpCommand(
                                it,
                                "player",
                                temp=true,
                            )
                        }
                    }

                    literal("top") {
                        executes {
                            helpCommand(
                                it,
                                "top",
                                temp=true,
                            )
                        }
                    }

                    literal("reset") {
                        executes {
                            helpCommand(
                                it,
                                "reset",
                                temp=true,
                            )
                        }
                    }
                }
            }

            literal("alltime") {
                literal("player") {
                    gameProfile("targets") {
                        executes {
                            getTimeCommand(
                                it,
                                GameProfileArgumentType.getProfileArgument(it, "targets").iterator(),
                            )
                        }

                        literal("set") {
                            greedyString("time") {
                                executes {
                                    setTimeCommand(
                                        it,
                                        GameProfileArgumentType.getProfileArgument(it, "targets").iterator(),
                                        StringArgumentType.getString(it, "time").toTime(),
                                    )
                                }
                            }
                        }

                        literal("add") {
                            greedyString("time") {
                                executes {
                                    addTimeCommand(
                                        it,
                                        GameProfileArgumentType.getProfileArgument(it, "targets").iterator(),
                                        StringArgumentType.getString(it, "time").toTime(),
                                    )
                                }
                            }
                        }
                    }
                }

                literal("top") {
                    executes {
                        topCommand(
                            it,
                        )
                    }

                    integer("count", min = 1) {
                        executes {
                            topCommand(
                                it,
                                IntegerArgumentType.getInteger(it, "count")
                            )
                        }
                    }
                }

                literal("reset") {
                    executes { GlobalScope.launch {
                        resetCommand(
                            it,
                        )
                    } }

                    bool("confirm") {
                        requires {
                            it.hasPermissionLevel(4)
                        }

                        executes { GlobalScope.launch {
                            resetCommand(
                                it,
                                BoolArgumentType.getBool(it, "confirm"),
                            )
                        } }

                        literal("revoke") {
                            executes { GlobalScope.launch {
                                resetCommand(
                                    it,
                                    BoolArgumentType.getBool(it, "confirm"),
                                    true,
                                )
                            } }
                        }
                    }
                }
            }

            literal("temp") {
                literal("player") {
                    gameProfile("targets") {
                        executes {
                            getTimeCommand(
                                it,
                                GameProfileArgumentType.getProfileArgument(it, "targets").iterator(),
                                temp=true,
                            )
                        }

                        literal("set") {
                            greedyString("time") {
                                executes {
                                    setTimeCommand(
                                        it,
                                        GameProfileArgumentType.getProfileArgument(it, "targets").iterator(),
                                        StringArgumentType.getString(it, "time").toTime(),
                                        temp=true,
                                    )
                                }
                            }
                        }

                        literal("add") {
                            greedyString("time") {
                                executes {
                                    addTimeCommand(
                                        it,
                                        GameProfileArgumentType.getProfileArgument(it, "targets").iterator(),
                                        StringArgumentType.getString(it, "time").toTime(),
                                        temp=true,
                                    )
                                }
                            }
                        }
                    }
                }

                literal("top") {
                    executes {
                        topCommand(
                            it,
                            temp=true,
                        )
                    }

                    integer("count", min = 1) {
                        executes {
                            topCommand(
                                it,
                                IntegerArgumentType.getInteger(it, "count"),
                                temp=true,
                            )
                        }
                    }
                }

                literal("reset") {
                    executes { GlobalScope.launch {
                        resetCommand(
                            it,
                            temp=true,
                        )
                    } }

                    bool("confirm") {
                        requires {
                            it.hasPermissionLevel(4)
                        }

                        executes { GlobalScope.launch {
                            resetCommand(
                                it,
                                confirm=BoolArgumentType.getBool(it, "confirm"),
                                temp=true,
                            )
                        } }
                    }
                }
            }
        }
            .build()
    }

    private fun helpCommand(context: Context, command: String?, temp: Boolean = false) {
        val tempArg = if (temp) { "temp" } else { "alltime" }
        val text = when (command) {
            "player" -> {
                ekho {
                    style { gold }
                    "["()
                    "PlaytimeTracker" {
                        style { yellow }
                    }
                    "] "()
                    "Help:" {
                        style { darkGreen }
                    }

                    newLine
                    " Command: " {
                        style { darkAqua }
                    }
                    "/playtime $tempArg player <targets> [(add|set)] [<time>]" {
                        style { gray }
                    }

                    newLine
                    newLine
                    " Parameters: " {
                        style { darkAqua }
                    }

                    newLine
                    "  targets" {
                        style { green; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "required, specifies the player(s) to run the command on" {
                            style { noBold }
                        }
                    }

                    newLine
                    "  add" {
                        style { darkGreen; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "optional, will add the input time to the playtime of the targets" {
                            style { noBold }
                        }
                    }

                    newLine
                    "  set" {
                        style { green; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "optional, will set the playtime of the targets to the input time" {
                            style { noBold }
                        }
                    }

                    newLine
                    "  time" {
                        style { darkGreen; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "required, if using add/set. format: \"7d 2m 4s\"" {
                            style { noBold }
                        }
                    }

                    newLine
                    "  if no parameter is use after targets, it will get the targets' playtime"() {
                        style { green }
                    }
                }
            }

            "top" -> {
                ekho {
                    style { gold }
                    "["()
                    "PlaytimeTracker" {
                        style { yellow }
                    }
                    "] "()
                    "Help:" {
                        style { darkGreen }
                    }

                    newLine
                    " Command: " {
                        style { darkAqua }
                    }
                    "/playtime $tempArg top [<count>]" {
                        style { gray }
                    }

                    newLine
                    newLine
                    " Parameters: " {
                        style { darkAqua }
                    }

                    newLine
                    "  count" {
                        style { green; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "specifies how many players should be listed, default=3" {
                            style { noBold }
                        }
                    }
                }
            }

            "reset" -> {
                ekho {
                    style { gold }
                    "["()
                    "PlaytimeTracker" {
                        style { yellow }
                    }
                    "] "()
                    "Help:" {
                        style { darkGreen }
                    }

                    newLine
                    " Command: " {
                        style { darkAqua }
                    }
                    "/playtime $tempArg reset [<confirm>] [revoke]" {
                        style { gray }
                    }

                    newLine
                    newLine
                    " Parameters: " {
                        style { darkAqua }
                    }

                    newLine
                    "  confirm" {
                        style { green; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "must be 'true' for playtimes to be reset, because " {
                            style { noBold }
                            "all" {
                                style { bold }
                            }
                            " playtime will be reset"()
                        }
                    }

                    newLine
                    "  revoke" {
                        style { darkGreen; bold }
                        " - "(false) {
                            style { gray }
                        }
                        "optional, when revoke param is used, all playtime advancements will be revoked as well"()
                    }

                    newLine
                    "Note that when resetting temp times, advancements cannot be revoked" { style { green; italics } }
                }
            }

            else -> {
                ekho {
                    style { gold }
                    "["()
                    "PlaytimeTracker" {
                        style { yellow }
                    }
                    "] "()
                    "Help:" {
                        style { darkGreen }
                    }

                    newLine
                    "Welcome to the help menu! For more info about specific commands, click below." {
                        style { darkAqua }
                        newLine
                        "All commands have 2 versions, one for alltime and one for temp." { style { italics } }
                    }

                    newLine
                    "player (get/set/add playtime)"() {
                        style {
                            green
                            clickEvent {
                                runCommand = "/playtime help $tempArg player"
                            }
                        }
                    }
                    newLine
                    "top (playtime leaderboard)"() {
                        style {
                            darkGreen
                            clickEvent {
                                runCommand = "/playtime help $tempArg top"
                            }
                        }
                    }
                    newLine
                    "reset (reset all playtime)"() {
                        style {
                            green
                            clickEvent {
                                runCommand = "/playtime help $tempArg reset"
                            }
                        }
                    }
                }
            }
        }
        
        context.source.sendFeedback(text, false)
    }

    private fun getTimeCommand(context: Context, targets: Iterator<GameProfile>, temp: Boolean = false) {
        targets.forEach { target ->
            val player = target.toPlayer(context.source.minecraftServer.playerManager) as AFKPlayer?
            if (player != null) {
                val time = (if (temp) { player.tempPlaytime } else { player.playtime }).prettyPrint()
                context.source.sendFeedback(
                    LiteralText("§9${target.name} §ehas §9$time §eof playtime."),
                    false
                )
            } else {
                val offlineData = OfflineDataCache.INSTANCE.players[target.id]
                if (offlineData != null && offlineData.contains("Playtime")) {
                    context.source.sendFeedback(
                        LiteralText(
                            "§9${target.name} §ehas §9${offlineData.getLong(if (temp) { "TempPlaytime" } else 
                            { "Playtime" }).prettyPrint()} §eof playtime."
                        ),
                        false
                    )
                } else {
                    context.source.sendFeedback(
                        LiteralText("§cPlaytime data for §9${target.name} §cnot found!"),
                        false
                    )
                }
            }
        }
    }

    private fun setTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long, temp: Boolean = false) {
        val manager = context.source.minecraftServer.playerManager
        val tracker = if (temp) { "temp" } else { "all time" }

        targets.forEach { target ->
            var requestedPlayer = target.toPlayer(manager)

            if (requestedPlayer == null) {
                requestedPlayer = manager.createPlayer(target)
                manager.loadPlayerData(requestedPlayer)
            }

            val player = requestedPlayer as AFKPlayer

            if (temp) { player.tempPlaytime = time } else { player.playtime = time }
            (manager as IAccessPlayerManager).invokeSavePlayerData(player as ServerPlayerEntity)

            context.source.sendFeedback(
                ekho("§eSet §9${target.name} §eto §9${time.prettyPrint()} §eof playtime. ($tracker)"),
                true
            )
        }
    }

    private fun addTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long, temp: Boolean = false) {
        val manager = context.source.minecraftServer.playerManager

        targets.forEach { target ->
            val player = target.toPlayer(manager) as AFKPlayer?

            // Online Player
            if (player != null) {
                setTimeCommand(
                    context,
                    listOf(target).iterator(),
                    if (temp) { player.tempPlaytime } else { player.playtime } + time
                )
            } else {

                // Offline Player
                val offlineData = OfflineDataCache.INSTANCE.players[target.id]
                if (offlineData != null && offlineData.contains("Playtime")) {
                    setTimeCommand(
                        context,
                        listOf(target).iterator(),
                        offlineData.getLong(if (temp) { "TempPlaytime" } else { "Playtime" }) + time)

                // Player not found
                } else {
                    setTimeCommand(context, listOf(target).iterator(), time)
                }
            }
        }
    }

    private fun topCommand(context: Context, count: Int = 3, temp: Boolean = false) {
        val source = context.source

        source.sendFeedback(
            LiteralText("§2====< §aLeaderboard §2>===="),
            false
        )

        val times = mutableMapOf<String, Long?>()
        OfflineDataCache.INSTANCE.players.forEach { (uuid, tag) ->
            val name = OfflineNameCache.INSTANCE.getNameFromUUID(uuid)
            val time = if (tag.contains("Playtime")) {
                tag.getLong(
                    if (temp) { "TempPlaytime" } else { "Playtime" }
                )
            } else {
                null
            }
            times[name] = time
        }

        // Use latest data for any online players
        context.source.minecraftServer.playerManager.playerList.forEach { player ->
            times[player.entityName] = if (temp) {
                    (player as AFKPlayer).tempPlaytime
            } else {
                    (player as AFKPlayer).playtime
            }
        }

        val top = times.toList().sortedBy { (_, value) -> value }.asReversed().toMap()
        var n = 1
        for (entry in top) {
            if (entry.value == null) {
                break
            }

            if (n % 2 == 0) {
                source.sendFeedback(
                    LiteralText("§a${n}. §b${entry.key}: ${entry.value!!.prettyPrint()}"),
                    false
                )
            } else {
                source.sendFeedback(
                    LiteralText("§2${n}. §3${entry.key}: ${entry.value!!.prettyPrint()}"),
                    false
                )
            }

            if (n++ == count) {
                break
            }
        }
    }

    private fun resetCommand(context: Context, confirm: Boolean = false, revoke: Boolean = false, temp: Boolean = false) {
        if (confirm) {
            OfflineDataCache.INSTANCE.players.forEach { (uuid, immutableTag) ->
                val tag = immutableTag.copy()
                if (!temp) {
                    if (tag.contains("Playtime")) {
                        tag.putLong("Playtime", 0L)
                        OfflineDataCache.INSTANCE.save(uuid, tag)
                    }
                }
                if (tag.contains("TempPlaytime")) {
                    tag.putLong("TempPlaytime", 0L)
                    OfflineDataCache.INSTANCE.save(uuid, tag)
                }
                if (revoke) {
                    OfflineAdvancementUtils.revoke(
                        uuid,
                        context.source.minecraftServer.advancementLoader[Identifier("playtime_tracker:end_of_time")]
                    )
                    OfflineAdvancementUtils.revoke(
                        uuid,
                        context.source.minecraftServer.advancementLoader[Identifier("playtime_tracker:ancient_one")]
                    )
                    OfflineAdvancementUtils.revoke(
                        uuid,
                        context.source.minecraftServer.advancementLoader[Identifier("playtime_tracker:time_marches")]
                    )
                    OfflineAdvancementUtils.revoke(
                        uuid,
                        context.source.minecraftServer.advancementLoader[Identifier("playtime_tracker:dedicated")]
                    )
                }
            }

            context.source.minecraftServer.playerManager.playerList.forEach { player ->
                if (!temp) { (player as AFKPlayer).playtime = 0L }
                (player as AFKPlayer).tempPlaytime = 0L
                if (revoke) {
                    AdvancementHelper.revoke(player, "playtime_tracker:end_of_time")
                    AdvancementHelper.revoke(player, "playtime_tracker:ancient_one")
                    AdvancementHelper.revoke(player, "playtime_tracker:time_marches")
                    AdvancementHelper.revoke(player, "playtime_tracker:dedicated")
                }
            }

            if (temp) {
                context.source.sendFeedback(
                    ekho("Reset all temp playtimes") { style { green } },
                    true,
                )
            } else {
                context.source.sendFeedback(
                    ekho("Reset all playtimes") { style { green } },
                    true,
                )
            }
        } else {
            if (temp) {
                context.source.sendError(
                    ekho("§cAre you sure you want to reset all temp playtimes? This action is not reversible! If you are" +
                            " certain you wish to proceed, run:\n§7/playtime temp reset true")
                )
            } else {
                context.source.sendError(
                    ekho("§cAre you sure you want to reset all playtimes? This action is not reversible! If you are" +
                            " certain you wish to proceed, run:\n§7/playtime alltime reset true")
                )
            }
        }
    }
}