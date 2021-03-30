package us.spaceclouds42.playtime_tracker.command

import com.github.p03w.aegis.aegisCommand
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.GameProfileArgumentType
import us.spaceclouds42.playtime_tracker.Context
import us.spaceclouds42.playtime_tracker.Node
import us.spaceclouds42.playtime_tracker.toTime

class PlaytimeCommand {
    fun register(): Node {
        return aegisCommand("playtime") {
            requires {
                it.hasPermissionLevel(2)
            }

            executes {
                helpCommand(
                    it,
                )
            }

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

    private fun helpCommand(context: Context) {
        println("help message")
    }

    private fun getTimeCommand(context: Context, targets: Iterator<GameProfile>) {
        targets.forEach { target ->
            println("getting playtime of ${target.name}")
        }
    }

    private fun setTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long) {
        targets.forEach { target ->
            println("setting ${target.name} to $time")
        }
    }

    private fun addTimeCommand(context: Context, targets: Iterator<GameProfile>, time: Long) {
        targets.forEach { target ->
            println("adding $time to ${target.name}")
        }
    }

    private fun topCommand(context: Context, count: Int = 3) {
        println("printing top $count players")
    }

    private fun resetCommand(context: Context, confirm: Boolean = false) {
        if (confirm) {
            println("resetting all playtime")
        } else {
            println("need confirmation before resetting all playtime. click here")
        }
    }
}