package com.fruse.dogedex.auth.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import com.fruse.dogedex.core.R
import com.fruse.dogedex.core.model.User
import com.fruse.dogedex.core.ui.theme.DogedexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            DogedexTheme {
                AuthScreen(
                    onUserLoggedIn = ::startMainActivity
                )
            }
        }
//        val binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        viewModel.status.observe(this) { status ->
//            when (status) {
//                is ApiResponseStatus.Error -> {
//                    binding.loadingWheel.isVisible = false
//                    showErrorDialog(status.messageId)
//                }
//
//                is ApiResponseStatus.Loading -> binding.loadingWheel.isVisible = true
//                is ApiResponseStatus.Success -> binding.loadingWheel.isVisible = false
//            }
//        }
//
//        viewModel.user.observe(this) { user ->
//            if (user != null) {
//                User.setLoggedInUser(this, user)
//                startMainActivity()
//            }
//        }
    }

    private fun startMainActivity(userValue: User) {
        User.setLoggedInUser(this, userValue)
        try {
            startActivity(
                Intent(
                    this,
                    Class.forName("com.fruse.dogedex.main.MainActivity")
                )
            )
        } catch (e: ClassNotFoundException) {
            Toast.makeText(this, "Main Activity not found", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun showErrorDialog(messageId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.there_was_an_error)
            .setMessage(messageId)
            .setPositiveButton(android.R.string.ok) { _, _ ->

            }
            .create()
            .show()
    }

//    override fun onRegisterButtonClick() {
//        findNavController(R.id.nav_host_fragment).navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
//    }

//    override fun onLoginFieldsValidate(email: String, password: String) {
//        viewModel.login(email, password)
//    }

//    override fun onSignUpFieldsValidate(
//        email: String, password: String, passwordConfirmation: String
//    ) {
//        viewModel.signUp(email, password, passwordConfirmation)
//    }
}