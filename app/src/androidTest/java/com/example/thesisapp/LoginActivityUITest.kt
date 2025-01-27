package com.example.thesisapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
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
import androidx.test.espresso.matcher.ViewMatchers
import android.view.View
import org.hamcrest.Matcher

@RunWith(AndroidJUnit4::class)
class LoginActivityUITest {

    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)


    @Test
    fun testEmptyFieldsValidation() {
        // Kliknięcie przycisku login bez podania danych
        onView(withId(R.id.btnLogin)).perform(click())

        // Sprawdzenie, czy wyświetla się komunikat o błędzie
        onView(withId(R.id.tvErrorMessage))
            .check(matches(withText("Please fill in all fields")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSuccessfulLoginFlow() {
        // Wprowadzenie poprawnej nazwy użytkownika i hasła
        onView(withId(R.id.etLoginUsername)).perform(typeText("admin"), closeSoftKeyboard())
        onView(withId(R.id.etLoginPassword)).perform(typeText("admin"), closeSoftKeyboard())

        // Kliknięcie przycisku login
        onView(withId(R.id.btnLogin)).perform(click())

        waitUntilVisible(R.id.tvErrorMessage)

        // Sprawdzenie, czy komunikat o sukcesie jest widoczny
        onView(withId(R.id.tvErrorMessage))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            .check(matches(withText("User logged in successfully")))
            .check(matches(isDisplayed()))

    }

    @Test
    fun testUnsuccessfulLoginFlow() {
        // Wprowadzenie niepoprawnych danych logowania
        onView(withId(R.id.etLoginUsername)).perform(typeText("wronguser"), closeSoftKeyboard())
        onView(withId(R.id.etLoginPassword)).perform(typeText("wrongpass"), closeSoftKeyboard())

        // Kliknięcie przycisku login
        onView(withId(R.id.btnLogin)).perform(click())

        waitUntilVisible(R.id.tvErrorMessage)

        // Sprawdzenie, czy wyświetla się komunikat o błędzie
        onView(withId(R.id.tvErrorMessage))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            .check(matches(withText("Login failed!")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testRememberMeSwitch() {
        // Włączenie przełącznika "Remember Me"
        onView(withId(R.id.switchRememberMe)).perform(click())

        // Sprawdzenie, czy jest zaznaczony
        onView(withId(R.id.switchRememberMe)).check(matches(isChecked()))
    }

    @Test
    fun testNavigationToRegisterActivity() {
        Intents.init()
        // Kliknięcie przycisku rejestracji
        onView(withId(R.id.btnRegister)).perform(click())

        // Sprawdzenie, czy nowa aktywność została uruchomiona
        intended(hasComponent(RegisterActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testLanguageChangeNavigation() {
        Intents.init()
        // Kliknięcie przycisku zmiany języka
        onView(withId(R.id.btnChangeLanguage)).perform(click())

        // Sprawdzenie, czy nowa aktywność LanguageSelectionActivity została uruchomiona
        intended(hasComponent(LanguageSelectionActivity::class.java.name))
        Intents.release()
    }
}



fun waitUntilVisible(viewId: Int, timeout: Long = 5000) {
    onView(withId(viewId)).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(View::class.java)
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

