@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * If you are using Kotlin, you can use this class to create Brigadier commands
 * Kotlin-style.
 *
 * You should probably implement your own command system if you are making your
 * own addon, rather than using anything from Pylon Core. We recommend checking
 * out Aikar's commands, as it makes adding commands very easy and simple.
 *
 * (Blame Kotlin for not allowing us to hide this class in any way from Java).
 */
class CommandBuilder(val command: ArgumentBuilder<CommandSourceStack, *>) {

    var requirement: CommandContext<CommandSourceStack>.(CommandSender) -> Boolean = { true }

    inline fun literal(name: String, block: CommandBuilder.() -> Unit): CommandNode<CommandSourceStack> {
        val builder = CommandBuilder(Commands.literal(name))
        builder.block()
        val node = builder.build()
        command.then(node)
        return node
    }

    inline fun argument(name: String, type: ArgumentType<*>, block: CommandBuilder.() -> Unit): CommandNode<CommandSourceStack> {
        val builder = CommandBuilder(Commands.argument(name, type))
        builder.block()
        val node = builder.build()
        command.then(node)
        return node
    }

    fun then(node: CommandNode<CommandSourceStack>) {
        command.then(node)
    }

    inline fun requires(errorMessage: Component, crossinline predicate: CommandContext<CommandSourceStack>.(CommandSender) -> Boolean) {
        val currentRequirement = requirement
        requirement = {
            if (!predicate(it)) {
                it.sendMessage(errorMessage)
                false
            } else {
                currentRequirement(it)
            }
        }
    }

    fun permission(permission: String) = requires(Component.translatable("pylon.pyloncore.message.command.error.no_permission")) { it.hasPermission(permission) }

    fun redirect(node: CommandNode<CommandSourceStack>) {
        command.redirect(node)
        // Fix for https://github.com/Mojang/brigadier/issues/46
        if (command.command == null) {
            command.executes(node.command)
        }
    }

    inline fun executes(crossinline handler: CommandContext<CommandSourceStack>.(CommandSender) -> Unit) {
        val currentRequirement = requirement
        command.executes { context ->
            if (context.currentRequirement(context.source.sender)) {
                context.handler(context.source.sender)
            }
            Command.SINGLE_SUCCESS
        }
    }

    inline fun executesWithPlayer(crossinline handler: CommandContext<CommandSourceStack>.(Player) -> Unit) {
        requires(Component.translatable("pylon.pyloncore.message.command.error.must_be_player")) { it is Player }
        executes {
            handler(source.sender as Player)
        }
    }

    fun build(): CommandNode<CommandSourceStack> {
        return command.build()
    }
}

@JvmSynthetic
internal inline fun buildCommand(name: String, block: CommandBuilder.() -> Unit): LiteralCommandNode<CommandSourceStack> {
    val builder = CommandBuilder(Commands.literal(name))
    builder.block()
    return builder.build() as LiteralCommandNode<CommandSourceStack>
}