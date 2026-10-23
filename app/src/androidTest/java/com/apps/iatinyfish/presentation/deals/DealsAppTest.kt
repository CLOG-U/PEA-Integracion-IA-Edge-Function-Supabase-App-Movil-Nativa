package com.apps.iatinyfish.presentation.deals

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.mutableStateOf
import com.apps.iatinyfish.data.model.ProductOffer
import com.apps.iatinyfish.ui.theme.IATinyFishTheme
import org.junit.Rule
import org.junit.Test

class DealsAppTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun startScreenShowsSearchAndSuggestion() {
        composeRule.setContent {
            IATinyFishTheme {
                DealsApp(state = DealsUiState(), onEvent = {})
            }
        }

        composeRule.onNodeWithText("Buscar").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED").assertIsDisplayed()
    }

    @Test fun loadingAndErrorStatesAreVisible() {
        val state = mutableStateOf(DealsUiState(hasSearched = true, isLoading = true))
        composeRule.setContent { IATinyFishTheme { DealsApp(state.value, {}) } }
        composeRule.onNodeWithTag("loading").assertIsDisplayed()
        composeRule.runOnIdle {
            state.value = DealsUiState(hasSearched = true, errorMessage = "Sin servicio")
        }
        composeRule.onNodeWithText("Sin servicio").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test fun resultOpensDetailAndBackReturnsToList() {
        val offer = ProductOffer(
            id = "web-1", store = "Tienda de prueba", productName = "Producto de prueba",
            price = "Ver en fuente", shipping = "Consulta en tienda",
            delivery = "Disponibilidad por confirmar", rating = "—",
            sourceUrl = "https://example.com/producto", summary = "Descripción de prueba",
            tags = listOf("Resultado web"),
        )
        val state = mutableStateOf(DealsUiState(
            query = "Producto", hasSearched = true, offers = listOf(offer), isPreviewData = false,
        ))
        composeRule.setContent {
            IATinyFishTheme {
                DealsApp(state.value) { event ->
                    when (event) {
                        is DealsEvent.OpenOffer -> state.value = state.value.copy(selectedOffer = event.offer)
                        DealsEvent.Back -> state.value = state.value.copy(selectedOffer = null)
                        else -> Unit
                    }
                }
            }
        }
        composeRule.onNodeWithText("1 resultados encontrados en la web").assertIsDisplayed()
        composeRule.onNodeWithText("Producto de prueba").performClick()
        composeRule.onNodeWithText("Detalle de oferta").assertIsDisplayed()
        composeRule.onNodeWithText("Abrir fuente web").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("‹  Volver").performClick()
        composeRule.onNodeWithText("1 resultados encontrados en la web").assertIsDisplayed()
    }
}
