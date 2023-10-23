package com.fruse.dogedex

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.GrantPermissionRule
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.auth.auth.AuthTasks
import com.fruse.dogedex.auth.auth.LoginActivity
import com.fruse.dogedex.auth.di.AuthTasksModule
import com.fruse.dogedex.core.model.User
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@UninstallModules(AuthTasksModule::class)
@HiltAndroidTest
class LoginActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val runtimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    class FakeAuthRepository @Inject constructor() : AuthTasks {
        override suspend fun login(email: String, password: String): ApiResponseStatus<User> {
            return ApiResponseStatus.Success(
                User(1L, "fruse.experiencias@gmail.com", "lala")
            )

        }

        override suspend fun signUp(
            email: String,
            password: String,
            passwordConfirmation: String
        ): ApiResponseStatus<User> {
            TODO("Not yet implemented")
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class AuthTaskTestsModule {

        @Binds
        abstract fun binAuthTasks(
            fakeAuthRepository: FakeAuthRepository
        ): AuthTasks
    }

    @Test
    fun mainActivityOpensAfterUserLogin() {
        val context = composeTestRule.activity

        composeTestRule
            .onNodeWithText(context.getString(R.string.login))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(testTag = "email-field")
            .performTextInput("fruse.experiencias@gmail.com")

        composeTestRule
            .onNodeWithTag(testTag = "password-field")
            .performTextInput("frus3260")

        composeTestRule
            .onNodeWithText(context.getString(R.string.login))
            .performClick()

        onView(withId(R.id.take_photo_fab))
            .check(
                matches(
                    isDisplayed()
                )
            )
    }
}