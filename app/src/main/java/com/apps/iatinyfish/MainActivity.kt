package com.apps.iatinyfish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.iatinyfish.presentation.deals.DealsApp
import com.apps.iatinyfish.presentation.deals.DealsViewModel
import com.apps.iatinyfish.ui.theme.IATinyFishTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            IATinyFishTheme {
                DealsApp(state = state, onEvent = viewModel::onEvent)
            }
        }
    }
}
