package com.example.sasi_smart_life.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sasi_smart_life.R
import com.example.sasi_smart_life.viewModel.TuyaViewModel
import com.example.sasi_smart_life.view.theme.sasiColor


@Composable
fun HelloScreen(viewModel: TuyaViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(painter = painterResource(id = R.drawable.maintenance),
            contentDescription = null,
            modifier = Modifier.fillMaxSize())

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = sasiColor.red500,
            )
        ) {
            Text("Logout")
        }
    }
}
