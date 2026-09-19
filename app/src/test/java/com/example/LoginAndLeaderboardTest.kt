package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MainViewModel
import com.example.ui.StudyNavDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginAndLeaderboardTest {

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = MainViewModel(app)
    }

    @Test
    fun testMandatoryLoginFlow() {
        // App starts with login required
        assertFalse(viewModel.isLoggedIn.value)

        // Student logs in with Maharashtra SSC credentials
        viewModel.login(
            name = "Ishan Mishra",
            school = "Balmohan Vidyamandir, Dadar",
            district = "Mumbai Suburban",
            divisionRoll = "10th-B / Roll #24",
            seatNo = "M092144",
            medium = "English Medium",
            pin = "1234",
            targetPercent = 96
        )

        // Session is authenticated
        assertTrue(viewModel.isLoggedIn.value)
        val profile = viewModel.studentProfile.value
        assertEquals("Ishan Mishra", profile.name)
        assertEquals("Mumbai Suburban", profile.district)
        assertEquals("Balmohan Vidyamandir, Dadar", profile.school)
        assertEquals("English Medium", profile.medium)
        assertEquals(96, profile.targetPercentage)
        assertEquals("Maharashtra SSC Class 10 (MSBSHSE)", profile.targetExam)

        // Logout works cleanly
        viewModel.logout()
        assertFalse(viewModel.isLoggedIn.value)
    }

    @Test
    fun testStateWidePeerLeaderboard() {
        val leaderboard = viewModel.leaderboard
        assertTrue(leaderboard.isNotEmpty())

        // Verify toppers from Maharashtra districts
        val rank1 = leaderboard.find { it.rank == 1 }
        assertNotNull(rank1)
        assertEquals("Pune", rank1?.district)
        assertTrue(rank1?.weeklyHours ?: 0f > 40f)

        val rank2 = leaderboard.find { it.rank == 2 }
        assertNotNull(rank2)
        assertEquals("Thane", rank2?.district)

        val currentUser = leaderboard.find { it.isCurrentUser }
        assertNotNull(currentUser)
        assertEquals("Mumbai Suburban", currentUser?.district)
    }

    @Test
    fun testMaharashtraSscNavigation() {
        viewModel.navigateTo(StudyNavDestination.LEADERBOARD)
        assertEquals(StudyNavDestination.LEADERBOARD, viewModel.currentDestination.value)

        viewModel.navigateTo(StudyNavDestination.PRACTICE)
        assertEquals(StudyNavDestination.PRACTICE, viewModel.currentDestination.value)
    }
}
