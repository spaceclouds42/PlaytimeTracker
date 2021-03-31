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
            val player = target.toPlayer(context.source.minecraftServer.playerManager) as AFKPlayer
            val time = player.playtime.prettyPrint()

            context.source.sendFeedback(
                LiteralText("§9${target.name} §ehas §9$time §eof playtime."),
                false
            )
        }
    }

    private fun setTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long) {
        val manager = context.source.minecraftServer.playerManager

        targets.forEach { target ->
            val player = target.toPlayer(manager) as AFKPlayer

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
            val newTime = (target.toPlayer(manager) as AFKPlayer).playtime + time
            setTimeCommand(context, listOf(target).iterator(), newTime)
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

        val top = times.toList().sortedBy { (_, value) -> value }.asReversed().toMap()
        var n = 1
        for (entry in top) {
            if (entry.value == null) {
                break
            }

            source.sendFeedback(
                LiteralText("§a${n}. §b${entry.key}: ${entry.value!!.prettyPrint()}"),
                false
            )

            if (n++ > count) {
                break
            }
        }
    }

    private fun resetCommand(context: Context, confirm: Boolean = false) {
        if (confirm) {
            var count = 0
            OfflineDataCache.INSTANCE.players.forEach { (uuid, immutableTag) ->
                val tag = immutableTag.copy()
                if (tag.contains("Playtime")) {
                    tag.putLong("Playtime", 0L)
                    OfflineDataCache.INSTANCE.save(uuid, tag)
                }
                count++
            }
            context.source.sendFeedback(
                LiteralText("§aReset §e$count §aplaytime(s)."),
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