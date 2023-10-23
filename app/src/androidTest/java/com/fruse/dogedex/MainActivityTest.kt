package com.fruse.dogedex

import android.Manifest
import androidx.camera.core.ImageProxy
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.camera.di.ClassifierModule
import com.fruse.dogedex.dogList.DogTasks
import com.fruse.dogedex.camera.machinelearning.ClassifierTasks
import com.fruse.dogedex.camera.machinelearning.DogRecognition
import com.fruse.dogedex.main.MainActivity
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.di.DogTasksModule
import com.fruse.dogedex.core.testutils.EspressoIdlingResource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@UninstallModules(DogTasksModule::class, ClassifierModule::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val runtimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule(order = 2)
    val composeTestRule = createComposeRule()

    @get:Rule(order = 3)
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingResource)
    }

    class FakeDogRepository @Inject constructor() : DogTasks {
        override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
            return ApiResponseStatus.Success(
                listOf(
                    Dog(
                        1, index = 1, "Chihuahua", "", "", "",
                        "", "", "", "", "", true
                    ),
                    Dog(
                        19, index = 23, "Guillermo", "", "", "",
                        "", "", "", "", "", false
                    )
                )
            )
        }

        override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> {
            TODO("Not yet implemented")
        }

        override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> {
            return ApiResponseStatus.Success(
                Dog(
                    89, index = 70, "Chow chow", "", "", "",
                    "", "", "", "", "", true
                )
            )
        }

        override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> {
            TODO("Not yet implemented")
        }

    }

    class FakeClassifierRepository @Inject constructor() : ClassifierTasks {
        override suspend fun recognizeImage(imageProxy: ImageProxy): List<DogRecognition> {
            return listOf(DogRecognition("a", 80f))
        }

    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class ClassifierTestModule {

        @Binds
        abstract fun bindClassifierTasks(fakeClassifierRepository: FakeClassifierRepository): ClassifierTasks
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class DogTasksTestModule {

        @Binds
        abstract fun bindDogTasks(
            fakedDogRepository: FakeDogRepository
        ): DogTasks
    }

    @Test
    fun showAllFab() {
        onView(withId(R.id.take_photo_fab)).check(matches(isDisplayed()))
        onView(withId(R.id.dog_list_fab)).check(matches(isDisplayed()))
        onView(withId(R.id.settings_fab)).check(matches(isDisplayed()))
    }

    @Test
    fun dogListOpensWhenClickingButton() {
        onView(withId(R.id.dog_list_fab)).perform(click())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val string = context.getString(R.string.my_dog_collection)
//        onView(withText(string)).check(matches(isDisplayed()))

        composeTestRule.onNodeWithText(string).assertIsDisplayed()
    }

    @Test
    fun whenRecognizingDogDetailsScreenOpens() {
        onView(withId(R.id.take_photo_fab)).perform(click())
        composeTestRule.onNodeWithTag(testTag = "close-details-screen-fab").assertIsDisplayed()
    }

}