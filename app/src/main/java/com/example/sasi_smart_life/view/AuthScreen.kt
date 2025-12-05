package com.example.sasi_smart_life.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.viewModel.AuthViewModel
import com.example.sasi_smart_life.view.theme.sasiColor
import com.example.sasi_smart_life.viewModel.AppState

@Composable
fun AuthScreen(viewModel: AuthViewModel, appState: AppState, onLoginSuccess: () -> Unit) {
    var showLogin by remember { mutableStateOf(true) }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Pindah ke layar utama jika sudah login
    LaunchedEffect(appState.isLoggedIn) {
        if (appState.isLoggedIn) {
            onLoginSuccess()
        }
    }


    // Observe and display toasts for info and error messages
    LaunchedEffect(authState.error, authState.info) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        authState.info?.let { infoMessage ->
            Toast.makeText(context, infoMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Judul Aplikasi", style = MaterialTheme.typography.headlineLarge)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_star),
                contentDescription = null,
            )
            Image(
                painter = painterResource(id = R.drawable.icon_left),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
            )

            Image(
                painter = painterResource(id = R.drawable.icon_right),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            )
            if (showLogin) {
                LoginContent(viewModel = viewModel, onSwitchToRegister = { showLogin = false })
            } else {
                RegisterContent(viewModel = viewModel, onSwitchToLogin = { showLogin = true })
            }
        }
    }
}

@Composable
fun LoginContent(viewModel: AuthViewModel, onSwitchToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(0.35f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Login to SASI Smart Life", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sasiColor.purple500,
                    unfocusedBorderColor = sasiColor.black300,
                    cursorColor = sasiColor.purple500,
                    focusedLabelColor = sasiColor.black300,
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sasiColor.purple500,
                    unfocusedBorderColor = sasiColor.black300,
                    cursorColor = sasiColor.purple500,
                    focusedLabelColor = sasiColor.black300,
                ),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(imageVector  = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.login(email, password) },
                colors = ButtonDefaults.buttonColors(containerColor = sasiColor.purple500),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login", color = sasiColor.purple50)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Belum punya akun? Daftar", color = sasiColor.purple500, modifier = Modifier.clickable { onSwitchToRegister() })
        }
    }

}

@Composable
fun RegisterContent(viewModel: AuthViewModel, onSwitchToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(0.35f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = sasiColor.grey50),
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Register", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sasiColor.blue500,
                    unfocusedBorderColor = sasiColor.black300,
                    cursorColor = sasiColor.blue500,
                    focusedLabelColor = sasiColor.black300,
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sasiColor.blue500,
                    unfocusedBorderColor = sasiColor.black300,
                    cursorColor = sasiColor.blue500,
                    focusedLabelColor = sasiColor.black300,
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = sasiColor.blue500,
                    unfocusedBorderColor = sasiColor.black300,
                    cursorColor = sasiColor.blue500,
                    focusedLabelColor = sasiColor.black300,
                ),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(imageVector  = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                OutlinedTextField(modifier = Modifier.weight(0.65f),
//                    value = verificationCode,
//                    onValueChange = { verificationCode = it },
//                    label = { Text("Verification Code") },
//                    singleLine = true,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = sasiColor.blue500,
//                        unfocusedBorderColor = sasiColor.black300,
//                        cursorColor = sasiColor.blue500,
//                        focusedLabelColor = sasiColor.black300,
//                    ))
//                Spacer(modifier = Modifier.width(8.dp))
//                Button(onClick = { viewModel.kirimKodeVerifikasi(email) },
//                    colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue50),
//                    modifier = Modifier.weight(0.35f)) {
//                    Text("Get Code", color = sasiColor.blue500)
//                }
//            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.daftar(name,email, password) } ,
                colors = ButtonDefaults.buttonColors(containerColor = sasiColor.blue500),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register & Login", color = sasiColor.blue50)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sudah punya akun? Login", color = sasiColor.blue500,
                modifier = Modifier.clickable { onSwitchToLogin() })
        }
    }
}
