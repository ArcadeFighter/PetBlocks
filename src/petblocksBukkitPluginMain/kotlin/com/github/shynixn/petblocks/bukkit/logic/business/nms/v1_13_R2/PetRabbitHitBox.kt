package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_13_R2

import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.business.extension.removeFinalModifier
import com.github.shynixn.petblocks.bukkit.logic.business.goals.PathfinderBaseGoal
import com.github.shynixn.petblocks.bukkit.logic.business.goals.PathfinderStickToBackGoalImpl
import com.google.common.collect.Sets
import net.minecraft.server.v1_13_R2.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
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
class PetRabbitHitBox(world: World) : EntityRabbit(world) {
    private var petDesign: PetDesign? = null

    /**
     * Additional override constructor.
     */
    constructor(player: Player, petDesign: PetDesign, location: Location) : this((location.world as CraftWorld).handle) {
        this.petDesign = petDesign
        this.isSilent = true

        val bField = PathfinderGoalSelector::class.java.getDeclaredField("b")
        val cField = PathfinderGoalSelector::class.java.getDeclaredField("c")

        bField.removeFinalModifier()
        cField.removeFinalModifier()

        bField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        bField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())

        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).value = 0.30000001192092896 * 0.75
        this.Q = petDesign.petMeta.modifier.climbingHeight.toFloat()

        val mcWorld = (location.world as CraftWorld).handle
        this.setPosition(location.x, location.y + 1, location.z)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        val pathfinders =
            arrayListOf(Pathfinder(PathfinderStickToBackGoalImpl(player, getBukkitEntity() as LivingEntity), JavaPlugin.getPlugin(PetBlocksPlugin::class.java)))

        val pathfinderBaseGoal : PathfinderBaseGoal<*>

        pathfinderBaseGoal.onStartExecutingFunction = { petPRoxy, ai ->
            service.playOnWaterService.
        }


        for (i in 0 until pathfinders.size) {
            this.goalSelector.a(i, pathfinders[i])
        }
    }

    /**
     * Overrides the default moving sound.
     */
    override fun dz(): SoundEffect {
        if (petDesign == null) {
            return super.dz()
        }

        try {
            petDesign!!.proxy.playMovingSound()
        } catch (e: Exception) {
            petDesign!!.proxy.logger.error("Failed to play moving sound.", e)
        }

        return super.dz()
    }
}