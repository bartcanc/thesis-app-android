package com.example.thesisapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import android.view.View
import org.hamcrest.Matcher

@RunWith(AndroidJUnit4::class)
class LoginActivityUITest {

    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun testEmptyFieldsValidation() {
        onView(withId(R.id.btnLogin)).perform(click())

        onView(withId(R.id.tvErrorMessage))
            .check(matches(withText("Please fill in all fields")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSuccessfulLoginFlow() {
        onView(withId(R.id.etLoginUsername)).perform(typeText("admin"), closeSoftKeyboard())
        onView(withId(R.id.etLoginPassword)).perform(typeText("admin"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin)).perform(click())

        waitUntilVisible(R.id.tvErrorMessage)

        onView(withId(R.id.tvErrorMessage))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            .check(matches(withText("User logged in successfully")))
            .check(matches(isDisplayed()))

    }

    @Test
    fun testUnsuccessfulLoginFlow() {
        onView(withId(R.id.etLoginUsername)).perform(typeText("wronguser"), closeSoftKeyboard())
        onView(withId(R.id.etLoginPassword)).perform(typeText("wrongpass"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin)).perform(click())

        waitUntilVisible(R.id.tvErrorMessage)

        onView(withId(R.id.tvErrorMessage))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            .check(matches(withText("Login failed!")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToRegisterActivity() {
        Intents.init()
        onView(withId(R.id.btnRegister)).perform(click())

        intended(hasComponent(RegisterActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testLanguageChangeNavigation() {
        Intents.init()
        onView(withId(R.id.btnChangeLanguage)).perform(click())

        intended(hasComponent(LanguageSelectionActivity::class.java.name))
        Intents.release()
    }
}



fun waitUntilVisible(viewId: Int, timeout: Long = 5000) {
    onView(withId(viewId)).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(View::class.java)
        }

        override fun getDescription(): String {
            return "Czekanie na widoczność widoku o ID: $viewId"
        }

        override fun perform(uiController: UiController, view: View) {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            do {
                if (view.visibility == View.VISIBLE) {
                    return
                }
                uiController.loopMainThreadForAtLeast(100)
            } while (System.currentTimeMillis() < endTime)
            throw AssertionError("Widok $viewId nie stał się widoczny w ciągu $timeout ms")
        }
    })
}

