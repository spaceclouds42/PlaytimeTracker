package us.spaceclouds42.playtime_tracker

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.server.command.ServerCommandSource

typealias Node = LiteralCommandNode<ServerCommandSource>
typealias Context = CommandContext<ServerCommandSource>
typealias Dispatcher = CommandDispatcher<ServerCommandSource>