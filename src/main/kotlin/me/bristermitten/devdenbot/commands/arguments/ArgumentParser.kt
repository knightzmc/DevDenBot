package me.bristermitten.devdenbot.commands.arguments

import me.bristermitten.devdenbot.extensions.Argument
import me.bristermitten.devdenbot.extensions.Arguments

object ArgumentParser {

    /**
     * Compress all the tokens into 1 until we reach another quote
     * If no quote is encountered, an [InvalidCommandSyntaxException] is thrown
     * Otherwise, this will return a single [Argument] holding the quoted message and the leftover tokens
     */
    private fun compressQuotedTokens(tokens: List<Token>): Pair<Argument, List<Token>> {
        val indexOfQuote = tokens.indexOfFirst { it.type == TokenType.QUOTE }
        val compressed = tokens.subList(0, indexOfQuote)
        if (compressed.size == tokens.size) {
            // No quote was found
            throw InvalidCommandSyntaxException("Unmatched quotation in command")
        }

        val compressedArgument = Argument(
            compressed.joinToString(" ") { it.content }
        )

        val remaining = tokens.subList(indexOfQuote, tokens.size - 1)
        return compressedArgument to remaining
    }

    /**
     * Parse a list of tokens into an [Arguments] object
     * @throws NoSuchElementException if the token list is empty
     */
    fun parse(message: List<Token>): Arguments {
        val commandName = message.first()
        val args = mutableListOf<Argument>()
        val flags = mutableListOf<Argument>()
        var tokens = message.drop(1)
        while (tokens.isNotEmpty()) {
            val next = tokens.first()
            when (next.type) {
                TokenType.NORMAL -> args.add(Argument(next.content))
                TokenType.DASH -> {
                    val flagValue = tokens[1]
                    flags.add(Argument("-" + flagValue.content))
                    tokens = tokens.drop(2)
                    continue
                }
                TokenType.QUOTE -> {
                    val (arg, newTokens) = compressQuotedTokens(tokens.drop(1))
                    args.add(arg)
                    tokens = newTokens
                    continue
                }
                TokenType.SPACE -> error("this should never happen")
                TokenType.EOF -> Unit
            }
            tokens = tokens.drop(1) //persistent collections ;-;
        }

        return Arguments(commandName.content, args, flags)
    }
}
