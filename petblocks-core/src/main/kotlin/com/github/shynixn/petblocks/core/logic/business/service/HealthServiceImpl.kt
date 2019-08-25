package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIHealth
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.extension.sync
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
class HealthServiceImpl @Inject constructor(
    concurrencyService: ConcurrencyService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val loggingService: LoggingService,
    private val proxyService: ProxyService,
    private val petService: PetService
) : HealthService, Runnable {
    /**
     * Initialize.
     */
    init {
        sync(concurrencyService, 0L, 20L) {
            this.run()
        }
    }

    /**
     * Damages the given [PetMeta] with the given [damage].
     * The pet needs a health ai otherwise this operation gets ignored.
     */
    override fun damagePet(petMeta: PetMeta, damage: Double) {
        val count = petMeta.aiGoals.count { p -> p is AIHealth }

        if (count == 0) {
            return
        }

        if (count > 1) {
            loggingService.warn("Player ${petMeta.playerMeta.name} has registered multiple ${AIType.HEALTH.type}. Please check your configuration.")
        }

        val aiHealth = petMeta.aiGoals.first { a -> a is AIHealth } as AIHealth
        aiHealth.health = aiHealth.health - damage

        if (aiHealth.health <= 0) {
            val player = proxyService.getPlayerFromUUID<Any>(petMeta.playerMeta.uuid)
            aiHealth.currentRespawningDelay = aiHealth.respawningDelay
            aiHealth.health = 0.0

            if (petService.hasPet(player)) {
                petService.getOrSpawnPetFromPlayer(player).get().remove()
            }
        }
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        for (petMeta in persistencePetMetaService.cache) {
            val aiBase = petMeta.aiGoals.firstOrNull { a -> a is AIHealth } as AIHealth? ?: continue

            if (aiBase.currentRespawningDelay > 0) {
                aiBase.currentRespawningDelay--

                if (aiBase.currentRespawningDelay == 0) {
                    val player = proxyService.getPlayerFromUUID<Any>(petMeta.playerMeta.uuid)
                    petService.getOrSpawnPetFromPlayer(player)
                }
            }

            if (aiBase.health.toInt() < aiBase.maxHealth.toInt()) {
                aiBase.health = aiBase.health + 1.0

                if (aiBase.health > aiBase.maxHealth) {
                    aiBase.health = aiBase.maxHealth
                }
            }
        }
    }
}