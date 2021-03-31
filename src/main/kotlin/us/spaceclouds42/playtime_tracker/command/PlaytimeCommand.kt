package us.spaceclouds42.playtime_tracker.command

import com.github.p03w.aegis.aegisCommand
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.basiqueevangelist.nevseti.OfflineDataCache
import me.basiqueevangelist.nevseti.OfflineNameCache
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import us.spaceclouds42.playtime_tracker.Context
import us.spaceclouds42.playtime_tracker.Node
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer
import us.spaceclouds42.playtime_tracker.extension.prettyPrint
import us.spaceclouds42.playtime_tracker.extension.toPlayer
import us.spaceclouds42.playtime_tracker.extension.toTime
import us.spaceclouds42.playtime_tracker.mixin.access.IAccessPlayerManager

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

                integer("count", min=1) {
                    executes {
                        topCommand(
                            it,
                            IntegerArgumentType.getInteger(it, "count")
                        )
                    }
                }
            }

            literal("reset") {
                executes {
                    resetCommand(
                        it,
                    )
                }

                bool("confirm") {
                    requires {
                        it.hasPermissionLevel(4)
                    }

                    executes {
                        resetCommand(
                            it,
                            BoolArgumentType.getBool(it, "confirm"),
                        )
                    }
                }
            }
        }
            .build()
    }

    private fun helpCommand(context: Context, command: String?) {
        if (command != null) {
            println("help message about $command command")
        } else {
            println("general help message")
        }
    }

    private fun getTimeCommand(context: Context, targets: Iterator<GameProfile>) {
        targets.forEach { target ->
            val player = target.toPlayer(context.source.minecraftServer.playerManager) as AFKPlayer?
            if (player != null) {
                val time = player.playtime.prettyPrint()
                context.source.sendFeedback(
                    LiteralText("§9${target.name} §ehas §9$time §eof playtime."),
                    false
                )
            } else {
                val offlineData = OfflineDataCache.INSTANCE.players[target.id]
                if (offlineData != null && offlineData.contains("Playtime")) {
                    context.source.sendFeedback(
                        LiteralText(
                            "§9${target.name} §ehas §9${offlineData.getLong("Playtime").prettyPrint()}" +
                                    " §eof playtime."
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

    private fun setTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long) {
        val manager = context.source.minecraftServer.playerManager

        targets.forEach { target ->
            var requestedPlayer = target.toPlayer(manager)

            if (requestedPlayer == null) {
                requestedPlayer = manager.createPlayer(target)
                manager.loadPlayerData(requestedPlayer)
            }

            val player = requestedPlayer as AFKPlayer

            player.playtime = time
            (manager as IAccessPlayerManager).invokeSavePlayerData(player as ServerPlayerEntity)

            context.source.sendFeedback(
                LiteralText("§eSet §9${target.name} §eto §9${time.prettyPrint()} §eof playtime."),
                true
            )
        }
    }

    private fun addTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long) {
        val manager = context.source.minecraftServer.playerManager

        targets.forEach { target ->
            val player = target.toPlayer(manager) as AFKPlayer?

            // Online Player
            if (player != null) {
                setTimeCommand(context, listOf(target).iterator(), player.playtime + time)
            } else {

                // Offline Player
                val offlineData = OfflineDataCache.INSTANCE.players[target.id]
                if (offlineData != null && offlineData.contains("Playtime")) {
                    setTimeCommand(context, listOf(target).iterator(), offlineData.getLong("Playtime") + time)

                // Player not found
                } else {
                    setTimeCommand(context, listOf(target).iterator(), time)
                }
            }
        }
    }

    private fun topCommand(context: Context, count: Int = 3) {
        val source = context.source

        source.sendFeedback(
            LiteralText("§2====< §aLeaderboard §2>===="),
            false
        )

        val times = mutableMapOf<String, Long?>()
        OfflineDataCache.INSTANCE.players.forEach { (uuid, tag) ->
            val name = OfflineNameCache.INSTANCE.getNameFromUUID(uuid)
            val time = if (tag.contains("Playtime")) { tag.getLong("Playtime") } else { null }
            times[name] = time
        }

        // Use latest data for any online players
        context.source.minecraftServer.playerManager.playerList.forEach { player ->
            times[player.entityName] = (player as AFKPlayer).playtime
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

    private fun resetCommand(context: Context, confirm: Boolean = false) {
        if (confirm) {
            OfflineDataCache.INSTANCE.players.forEach { (uuid, immutableTag) ->
                val tag = immutableTag.copy()
                if (tag.contains("Playtime")) {
                    tag.putLong("Playtime", 0L)
                    OfflineDataCache.INSTANCE.save(uuid, tag)
                }
            }

            context.source.minecraftServer.playerManager.playerList.forEach { player ->
                (player as AFKPlayer).playtime = 0L
            }

            context.source.sendFeedback(
                LiteralText("§aReset all playtime."),
                true
            )
        } else {
            context.source.sendError(
                LiteralText(
                    "§cAre you sure you want to reset all playtimes? This action is not reversible! If you are" +
                            " certain you wish to proceed, run:\n§7/playtime reset true"
                )
            )
        }
    }
}