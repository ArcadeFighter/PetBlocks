package com.github.shynixn.petblocks.core.logic.business.command

import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.api.business.service.*
import com.google.inject.Inject

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class EditPetInventoryCommand @Inject
constructor(
    private val proxyService: ProxyService,
    private val petService: PersistencePetMetaService,
    private val commandService: CommandService,
    private val messageService: MessageService,
    private val guiService: GUIPetStorageService
) : SourceCommand {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        if (args.size < 3 || !args[0].equals("inv", true) || args[1].toIntOrNull() == null || args[2].toIntOrNull() == null) {
            return false
        }

        val from = args[1].toInt()
        val to = args[2].toInt()

        val result = commandService.parseCommand<Any?>(source as Any, args, 3)

        if (result.first == null) {
            return false
        }

        val player = result.first
        val playerName = proxyService.getPlayerName(player)
        val petMeta = petService.getPetMetaFromPlayer(player)

        val target = if (proxyService.isPlayer(source)) {
            source
        } else {
            player
        }

        guiService.openStorage(target, petMeta, from, to)
        messageService.sendSourceMessage(source, "Opened the pet inventory of player $playerName in read-only mode.")

        return true
    }
}