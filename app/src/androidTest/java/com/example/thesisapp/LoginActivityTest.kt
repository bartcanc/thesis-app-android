package com.example.thesisapp

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun test_login_button_displays_error_when_fields_are_empty() {
        onView(withId(R.id.btnLogin)).perform(click())

        onView(withId(R.id.tvErrorMessage))
            .check(matches(withText("Please fill in all fields")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun test_login_success_with_valid_credentials() {
        onView(withId(R.id.etLoginUsername)).perform(typeText("admin"))
        closeSoftKeyboard()
        onView(withId(R.id.etLoginPassword)).perform(typeText("admin"))
        closeSoftKeyboard()

        onView(withId(R.id.btnLogin)).perform(click())

        Thread.sleep(1000)

        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun test_login_failure_with_invalid_credentials() {
        onView(withId(R.id.etLoginUsername)).perform(typeText("amdin"))
        closeSoftKeyboard()
        onView(withId(R.id.etLoginPassword)).perform(typeText("adimn"))
        closeSoftKeyboard()

        onView(withId(R.id.btnLogin)).perform(click())

        onView(withId(R.id.tvErrorMessage))
            .check(matches(withText("Login failed!")))
            .check(matches(isDisplayed()))
    }
}
