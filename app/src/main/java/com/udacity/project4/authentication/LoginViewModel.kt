package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class LoginViewModel : ViewModel() {

    /**
     * Gets a fact to display based on the user's set preference of which type of fact they want
     * to see (Android fact or California fact). If there is no logged in user or if the user has
     * not set a preference, defaults to showing Android facts.
     */

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        }
        else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}
