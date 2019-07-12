@file:Suppress("UNCHECKED_CAST")

package integrationtest

import ch.vorburger.mariadb4j.DB
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.ItemStackProxy
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.core.logic.business.service.AIServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.LoggingUtilServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.YamlSerializationServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIMovementEntity
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import com.github.shynixn.petblocks.sponge.logic.business.proxy.ItemStackProxyImpl
import com.github.shynixn.petblocks.sponge.logic.business.proxy.PlayerProxyImpl
import com.github.shynixn.petblocks.sponge.logic.business.service.ConfigurationServiceImpl
import com.github.shynixn.petblocks.sponge.logic.business.service.EntityServiceImpl
import com.github.shynixn.petblocks.sponge.logic.business.service.ItemServiceImpl
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.asset.Asset
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.io.File
import java.nio.file.Paths
import java.sql.DriverManager
import java.util.*
import java.util.logging.Logger

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
class PersistenceMySQLIT {
    /**
     * Given
     *      initial empty database and production configuration in config.yml
     * When
     *      getAll is called the database should still be empty and when getOrCreateFromPlayerUUID with a new uuid is called
     * Then
     *     the default pet with the default production configuration from the config.yml should be generated.
     */
    @Test
    fun getOrCreateFromPlayerUUID_ProductionConfiguration_ShouldGenerateCorrectPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val uuid = UUID.fromString("c7d21810-d2a0-407d-a389-14efd3eb79d2")
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(player.uniqueId).thenReturn(uuid)

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val actual = classUnderTest.getPetMetaFromPlayer(player)

        // Assert
        Assertions.assertEquals(0, initialSize)
        Assertions.assertEquals(1, actual.id)
        Assertions.assertEquals(false, actual.enabled)
        Assertions.assertEquals("Kenny's Pet", actual.displayName)
        Assertions.assertEquals(true, actual.soundEnabled)
        Assertions.assertEquals(true, actual.particleEnabled)
        Assertions.assertEquals(1, actual.skin.id)
        Assertions.assertEquals("GRASS", actual.skin.typeName)
        Assertions.assertEquals(0, actual.skin.dataValue)
        Assertions.assertEquals(false, actual.skin.unbreakable)
        Assertions.assertEquals("", actual.skin.owner)
        Assertions.assertEquals(1, actual.playerMeta.id)
        Assertions.assertEquals("Kenny", actual.playerMeta.name)
        Assertions.assertEquals(5, actual.aiGoals.size)

        Assertions.assertEquals("hopping", (actual.aiGoals[0] as AIMovementEntity).type)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).climbingHeight)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSpeed)
        Assertions.assertEquals(-1.0, (actual.aiGoals[0] as AIMovementEntity).movementYOffSet)
        Assertions.assertEquals("CHICKEN_WALK", (actual.aiGoals[0] as AIMovementEntity).movementSound.name)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSound.volume)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSound.pitch)
        Assertions.assertEquals(ParticleType.REDSTONE, (actual.aiGoals[0] as AIMovementEntity).movementParticle.type)
        Assertions.assertEquals(20, (actual.aiGoals[0] as AIMovementEntity).movementParticle.amount)

        Assertions.assertEquals("follow-owner", (actual.aiGoals[1] as AIFollowOwner).type)
        Assertions.assertEquals(3.0, (actual.aiGoals[1] as AIFollowOwner).distanceToOwner)
        Assertions.assertEquals(50.0, (actual.aiGoals[1] as AIFollowOwner).maxRange)
        Assertions.assertEquals(1.5, (actual.aiGoals[1] as AIFollowOwner).speed)

        Assertions.assertEquals("float-in-water", (actual.aiGoals[2] as AIFloatInWater).type)

        Assertions.assertEquals("feeding", (actual.aiGoals[3] as AIFeeding).type)
        Assertions.assertEquals("391", (actual.aiGoals[3] as AIFeeding).typeName)
        Assertions.assertEquals(ParticleType.HEART, (actual.aiGoals[3] as AIFeeding).clickParticle.type)
        Assertions.assertEquals("EAT", (actual.aiGoals[3] as AIFeeding).clickSound.name)

        Assertions.assertEquals("ambient-sound", (actual.aiGoals[4] as AIAmbientSound).type)
        Assertions.assertEquals("CHICKEN_IDLE", (actual.aiGoals[4] as AIAmbientSound).sound.name)
    }

    /**
     * Given
     *      initial empty database and production configuration in config.yml
     * When
     *      getAll is called the database should still be empty and when getOrCreateFromPlayerUUID is called
     * Then
     *     the default pet with the default production configuration should be correctly editable and storeAble again.
     */
    @Test
    fun getOrCreateFromPlayerUUID_ProductionConfiguration_ShouldAllowChangingPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val uuid = UUID.fromString("c7d21810-d2a0-407d-a389-14efd3eb79d2")
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(player.uniqueId).thenReturn(uuid)

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val petMeta = classUnderTest.getPetMetaFromPlayer(player)

        petMeta.enabled = true
        petMeta.displayName = "This should be a very long displayname in order to test if very long names can also be stored"
        petMeta.soundEnabled = false
        petMeta.particleEnabled = false
        petMeta.skin.typeName = "DIRT"
        petMeta.skin.dataValue = 2
        petMeta.skin.unbreakable = true
        petMeta.skin.owner = "Pikachu"
        petMeta.playerMeta.name = "Superman"

        (petMeta.aiGoals[0] as AIHopping).climbingHeight = 23.5
        (petMeta.aiGoals[0] as AIHopping).movementSpeed = 105.3
        (petMeta.aiGoals[0] as AIHopping).movementYOffSet = 93.4
        (petMeta.aiGoals[0] as AIHopping).movementParticle.offSetY = 471.2
        (petMeta.aiGoals[0] as AIHopping).movementSound.pitch = 44.2

        (petMeta.aiGoals[1] as AIFollowOwner).maxRange = 100.2
        (petMeta.aiGoals[1] as AIFollowOwner).distanceToOwner = 14.45
        (petMeta.aiGoals[1] as AIFollowOwner).speed = 42.2

        (petMeta.aiGoals[2] as AIFloatInWater).userId = "43"
        (petMeta.aiGoals[3] as AIFeeding).clickParticle.offSetZ = 25.4
        (petMeta.aiGoals[3] as AIFeeding).clickSound.name = "COOKIE_SOUND"
        (petMeta.aiGoals[3] as AIFeeding).dataValue = 4
        (petMeta.aiGoals[3] as AIFeeding).typeName = "POWER_BANK"

        (petMeta.aiGoals[4] as AIAmbientSound).sound.volume = 41.55

        classUnderTest.save(petMeta).get()
        val actual = classUnderTest.getPetMetaFromPlayer(player)

        // Assert
        Assertions.assertEquals(0, initialSize)
        Assertions.assertEquals(1, actual.id)
        Assertions.assertEquals(true, actual.enabled)
        Assertions.assertEquals("This should be a very long displayname in order to test if very long names can also be stored", actual.displayName)
        Assertions.assertEquals(false, actual.soundEnabled)
        Assertions.assertEquals(false, actual.particleEnabled)
        Assertions.assertEquals(1, actual.skin.id)
        Assertions.assertEquals("DIRT", actual.skin.typeName)
        Assertions.assertEquals(2, actual.skin.dataValue)
        Assertions.assertEquals(true, actual.skin.unbreakable)
        Assertions.assertEquals("Pikachu", actual.skin.owner)
        Assertions.assertEquals(1, actual.playerMeta.id)
        Assertions.assertEquals("Superman", actual.playerMeta.name)

        Assertions.assertEquals("hopping", (actual.aiGoals[0] as AIMovementEntity).type)
        Assertions.assertEquals(23.5, (actual.aiGoals[0] as AIMovementEntity).climbingHeight)
        Assertions.assertEquals(105.3, (actual.aiGoals[0] as AIMovementEntity).movementSpeed)
        Assertions.assertEquals(93.4, (actual.aiGoals[0] as AIMovementEntity).movementYOffSet)
        Assertions.assertEquals(44.2, (actual.aiGoals[0] as AIMovementEntity).movementSound.pitch)
        Assertions.assertEquals(471.2, (actual.aiGoals[0] as AIMovementEntity).movementParticle.offSetY)

        Assertions.assertEquals("follow-owner", (actual.aiGoals[1] as AIFollowOwner).type)
        Assertions.assertEquals(14.45, (actual.aiGoals[1] as AIFollowOwner).distanceToOwner)
        Assertions.assertEquals(100.2, (actual.aiGoals[1] as AIFollowOwner).maxRange)
        Assertions.assertEquals(42.2, (actual.aiGoals[1] as AIFollowOwner).speed)

        Assertions.assertEquals("43", (actual.aiGoals[2] as AIFloatInWater).userId!!)

        Assertions.assertEquals("feeding", (actual.aiGoals[3] as AIFeeding).type)
        Assertions.assertEquals("POWER_BANK", (actual.aiGoals[3] as AIFeeding).typeName)
        Assertions.assertEquals(4, (actual.aiGoals[3] as AIFeeding).dataValue)
        Assertions.assertEquals("COOKIE_SOUND", (actual.aiGoals[3] as AIFeeding).clickSound.name)
        Assertions.assertEquals(25.4, (actual.aiGoals[3] as AIFeeding).clickParticle.offSetZ)

        Assertions.assertEquals("ambient-sound", (actual.aiGoals[4] as AIAmbientSound).type)
        Assertions.assertEquals(41.55, (actual.aiGoals[4] as AIAmbientSound).sound.volume)
    }

    companion object {
        private var database: DB? = null
        private var dbContext: SqlDbContext? = null

        fun createWithDependencies(): PersistencePetMetaService {
            if (dbContext != null) {
                dbContext!!.close()
            }

            if (database != null) {
                database!!.stop()
            }

            database = DB.newEmbeddedDB(3306)
            database!!.start()

            val sourceFolder = File("../petblocks-core/src/main/resources")
            val integrationDirectory = File("integration-test")

            if(integrationDirectory.exists()){
                integrationDirectory.deleteRecursively()
                integrationDirectory.mkdir()
            }

            FileUtils.copyDirectory(sourceFolder, integrationDirectory)

            var content = FileUtils.readFileToString(File("integration-test/assets/petblocks", "config.yml"), "UTF-8")
            content = content.replace("type: 'sqlite'", "type: 'mysql'").replace("database: ''","database: 'db'").replace("username: ''", "username: 'root'")
            FileUtils.write(File("integration-test/assets/petblocks", "config.yml"), content,  "UTF-8")

            DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC").use { conn ->
                conn.createStatement().use { statement ->
                    statement.executeUpdate("CREATE DATABASE db")
                }
            }

            val plugin = Mockito.mock(PluginContainer::class.java)

            Mockito.`when`(plugin.getAsset(Mockito.anyString())).then { p ->
                val path = p.getArgument<String>(0)
                val url = File("integration-test/$path").toURI()
                val asset = Mockito.mock(Asset::class.java)
                Mockito.`when`(asset.url).thenReturn(url.toURL())

                Optional.of(asset)
            }

            val aiService = AIServiceImpl(
                LoggingUtilServiceImpl(Logger.getAnonymousLogger()),
                MockedProxyService()
            )

            val configurationService = ConfigurationServiceImpl(
                Paths.get("integration-test/assets/petblocks")
                , LoggingUtilServiceImpl(Logger.getAnonymousLogger()), plugin,
                MockedItemService(), aiService
            )

            val method = PetBlocksApi::class.java.getDeclaredMethod("initializePetBlocks", PluginProxy::class.java)
            method.isAccessible = true
            method.invoke(PetBlocksApi, MockedPluginProxy())

            EntityServiceImpl(
                configurationService,
                MockedProxyService(),
                Mockito.mock(EntityRegistrationService::class.java),
                YamlSerializationServiceImpl(),
                LoggingUtilServiceImpl(Logger.getAnonymousLogger()),
                aiService,
                Mockito.mock(PetService::class.java),
                Mockito.mock(AfraidOfWaterService::class.java),
                Mockito.mock(NavigationService::class.java),
                Mockito.mock(SoundService::class.java),
                Version.VERSION_1_12_R1
            )

            dbContext = SqlDbContextImpl(configurationService, LoggingUtilServiceImpl(Logger.getAnonymousLogger()))

            val sqlite = PetMetaSqlRepository(dbContext!!, aiService, configurationService)

            return PersistencePetMetaServiceImpl(
                MockedProxyService(),
                sqlite,
                MockedConcurrencyService(),
                MockedEventService()
            )
        }
    }

    class MockedItemService : ItemService {
        /**
         * Creates a new itemstack from the given parameters.
         */
        override fun createItemStack(type: Any, dataValue: Int): ItemStackProxy {
            if (type == 2) {
                return ItemStackProxyImpl("GRASS")
            }

            throw IllegalArgumentException()
        }

        /**
         * Gets if the given [itemStack] has got the given [type] and [dataValue].
         */
        override fun <I> hasItemStackProperties(itemStack: I, type: Any, dataValue: Int): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Gets the itemstack in the hand of the player with optional offHand flag.
         */
        override fun <P, I> getItemInHand(player: P, offHand: Boolean): Optional<I> {
            throw IllegalArgumentException()
        }
    }


    class MockedPluginProxy : PluginProxy {
        /**
         * Gets a business logic from the PetBlocks plugin.
         * All types in the service package can be accessed.
         * Throws a [IllegalArgumentException] if the service could not be found.
         * @param S the type of service class.
         */
        override fun <S> resolve(service: Any): S {
            if (service == ItemService::class.java) {
                return ItemServiceImpl() as S
            }

            throw IllegalArgumentException()
        }

        /**
         * Creates a new entity from the given [entity].
         * Throws a [IllegalArgumentException] if the entity could not be found.
         * @param E the type of entity class.
         */
        override fun <E> create(entity: Any): E {
            throw IllegalArgumentException()
        }
    }

    class MockedProxyService : ProxyService {
        /**
         * Returns a player proxy object for the given instance.
         * Throws a [IllegalArgumentException] if the proxy could not be generated.
         */
        override fun <P> findPlayerProxyObject(instance: P): PlayerProxy {
            if (instance !is Player) {
                throw RuntimeException()
            }

            Mockito.`when`(instance.name).thenReturn("Kenny")
            Mockito.`when`(instance.location).thenReturn(Location(Mockito.mock(World::class.java), 20.2, 20.2, 20.2))

            return PlayerProxyImpl(instance as Player)
        }

        /**
         * Gets if the given instance can be converted to a player.
         */
        override fun <P> isPlayer(instance: P): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a player proxy for the given player uuid.
         */
        override fun findPlayerProxyObjectFromUUID(uuid: String): PlayerProxy {
            val playerProxy = Mockito.mock(PlayerProxy::class.java)
            Mockito.`when`(playerProxy.uniqueId).thenReturn(uuid)
            Mockito.`when`(playerProxy.name).thenReturn("Kenny")

            return playerProxy
        }

        /**
         * Clears any resources the given instance has allocated.
         */
        override fun cleanResources(instance: Any) {
            throw IllegalArgumentException()
        }
    }

    class MockedConcurrencyService : ConcurrencyService {
        /**
         * Runs the given [function] synchronised with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskSync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            function.invoke()
        }

        /**
         * Runs the given [function] asynchronous with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskAsync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            function.invoke()
        }
    }

    class MockedEventService : EventService {
        /**
         * Calls a framework event and returns if it was cancelled.
         */
        override fun callEvent(event: Any): Boolean {
            return false
        }
    }
}