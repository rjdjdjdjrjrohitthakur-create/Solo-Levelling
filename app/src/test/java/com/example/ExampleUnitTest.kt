package com.example

import com.example.domain.LevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testLevelCalculationFormula() {
        // Level 1: 0 to 99 XP
        assertEquals(1, LevelCalculator.getLevelFromLifetimeXp(0L))
        assertEquals(1, LevelCalculator.getLevelFromLifetimeXp(99L))

        // Level 2: 100 to 299 XP (100 * 1 = 100 needed)
        assertEquals(2, LevelCalculator.getLevelFromLifetimeXp(100L))
        assertEquals(2, LevelCalculator.getLevelFromLifetimeXp(299L))

        // Level 3: 300 to 599 XP (100 + 200 = 300 needed)
        assertEquals(3, LevelCalculator.getLevelFromLifetimeXp(300L))
        assertEquals(3, LevelCalculator.getLevelFromLifetimeXp(599L))

        // Level 4: 600 XP (300 + 300 = 600 needed)
        assertEquals(4, LevelCalculator.getLevelFromLifetimeXp(600L))

        // Level 5: 1000 XP (600 + 400 = 1000 needed)
        assertEquals(5, LevelCalculator.getLevelFromLifetimeXp(1000L))
    }

    @Test
    fun testCumulativeXpThresholds() {
        assertEquals(0L, LevelCalculator.cumulativeXpForLevel(1))
        assertEquals(100L, LevelCalculator.cumulativeXpForLevel(2))
        assertEquals(300L, LevelCalculator.cumulativeXpForLevel(3))
        assertEquals(600L, LevelCalculator.cumulativeXpForLevel(4))
        assertEquals(1000L, LevelCalculator.cumulativeXpForLevel(5))
    }

    @Test
    fun testLevelProgressFraction() {
        // Level 2 requires 200 XP to get to Level 3 (from 100 to 300)
        // At 200 lifetime XP: currentLevelXp = 100, required = 200, fraction = 0.5f
        val progress = LevelCalculator.calculateProgress(200L)
        assertEquals(2, progress.level)
        assertEquals(100L, progress.currentLevelXp)
        assertEquals(200L, progress.xpRequiredForNextLevel)
        assertEquals(100L, progress.xpRemainingToNextLevel)
        assertEquals(0.5f, progress.progressFraction, 0.001f)
    }

    @Test
    fun testRanks() {
        assertEquals("Bronze Novice", LevelCalculator.getRank(1).title)
        assertEquals("Iron Adventurer", LevelCalculator.getRank(5).title)
        assertEquals("Silver Specialist", LevelCalculator.getRank(10).title)
        assertEquals("Gold Vanguard", LevelCalculator.getRank(15).title)
        assertEquals("Platinum Paragon", LevelCalculator.getRank(25).title)
        assertEquals("Diamond Sovereign", LevelCalculator.getRank(40).title)
        assertEquals("Apex Legend", LevelCalculator.getRank(60).title)
    }

    @Test
    fun testScreenNavigationItems() {
        val items = com.example.ui.navigation.Screen.items
        assertEquals(5, items.size)
        items.forEach { screen ->
            org.junit.Assert.assertNotNull("Screen must not be null", screen)
            assertTrue("Screen route must not be blank", screen.route.isNotBlank())
            assertTrue("Screen title must not be blank", screen.title.isNotBlank())
        }
    }

    @Test
    fun testPhysiqueTierEvolution() {
        // Level 1: Weak/Frail rookie
        val tierLvl1 = com.example.ui.components.getPhysiqueTier(1)
        assertEquals(1, tierLvl1.stage)
        assertEquals("Novice Silhouette", tierLvl1.title)

        // Level 10: Athletic
        val tierLvl10 = com.example.ui.components.getPhysiqueTier(10)
        assertEquals(2, tierLvl10.stage)
        assertEquals("Athletic Operative", tierLvl10.title)

        // Level 18: Muscular Vanguard
        val tierLvl18 = com.example.ui.components.getPhysiqueTier(18)
        assertEquals(3, tierLvl18.stage)
        assertEquals("Muscular Vanguard", tierLvl18.title)

        // Level 30: Shredded Titan
        val tierLvl30 = com.example.ui.components.getPhysiqueTier(30)
        assertEquals(4, tierLvl30.stage)
        assertEquals("Shredded Titan", tierLvl30.title)

        // Level 45: Ascended Sovereign
        val tierLvl45 = com.example.ui.components.getPhysiqueTier(45)
        assertEquals(5, tierLvl45.stage)
        assertEquals("Ascended Sovereign", tierLvl45.title)
    }

    @Test
    fun testAiQuestServiceFallback() {
        val service = com.example.data.ai.AiQuestService()
        val quests = service.generateOfflineFallbackQuests("Build muscle and lose weight", "30 Days", "Hardcore")
        assertTrue("Quests list must not be empty", quests.isNotEmpty())
        assertTrue("Generated quests must have positive XP", quests.all { it.xpReward > 0 })
        assertEquals(com.example.data.model.QuestCategory.BODY_FITNESS, quests.first().category)
    }
}
