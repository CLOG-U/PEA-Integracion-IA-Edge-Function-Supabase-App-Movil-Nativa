package com.apps.iatinyfish.presentation.deals

import com.apps.iatinyfish.data.model.ProductOffer
import com.apps.iatinyfish.data.repository.DealsRepository
import com.apps.iatinyfish.data.repository.RemoteSearchException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DealsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun blankQueryShowsValidationWithoutCallingRepository() = runTest {
        val repository = FakeDealsRepository()
        val viewModel = DealsViewModel(repository)

        viewModel.onEvent(DealsEvent.SubmitSearch)

        assertTrue(viewModel.state.value.hasSearched)
        assertEquals(0, repository.calls)
        assertEquals("Escribe un producto para iniciar la búsqueda.", viewModel.state.value.errorMessage)
    }

    @Test fun validQueryPublishesResults() = runTest {
        val offer = sampleOffer()
        val viewModel = DealsViewModel(FakeDealsRepository(result = listOf(offer)))

        viewModel.onEvent(DealsEvent.QueryChanged("Switch OLED"))
        viewModel.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(listOf(offer), viewModel.state.value.offers)
    }

    @Test fun remoteFailurePublishesReadableError() = runTest {
        val viewModel = DealsViewModel(FakeDealsRepository(error = RemoteSearchException("Sin servicio")))

        viewModel.onEvent(DealsEvent.QueryChanged("Laptop"))
        viewModel.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertEquals("Sin servicio", viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.offers.isEmpty())
    }

    @Test fun shortAndLongQueriesAreRejected() = runTest {
        val repository = FakeDealsRepository()
        val viewModel = DealsViewModel(repository)
        for (query in listOf("x", "a".repeat(201))) {
            viewModel.onEvent(DealsEvent.QueryChanged(query))
            viewModel.onEvent(DealsEvent.SubmitSearch)
            assertEquals("La búsqueda debe tener entre 2 y 200 caracteres.", viewModel.state.value.errorMessage)
        }
        assertEquals(0, repository.calls)
    }

    @Test fun trimsQueryAndPreservesResultsLabelWhenEditing() = runTest {
        val viewModel = DealsViewModel(FakeDealsRepository())
        viewModel.onEvent(DealsEvent.QueryChanged("  Nintendo Switch  "))
        viewModel.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()
        assertEquals("Nintendo Switch", viewModel.state.value.query)
        viewModel.onEvent(DealsEvent.QueryChanged("Laptop"))
        assertEquals("Nintendo Switch", viewModel.state.value.searchedQuery)
    }

    @Test fun aNewSearchCancelsTheOldOne() = runTest {
        val repository = object : DealsRepository {
            override val isPreviewData = false
            override suspend fun search(query: String): List<ProductOffer> {
                delay(if (query == "Primera") 1000 else 10)
                return listOf(sampleOffer().copy(id = query))
            }
        }
        val viewModel = DealsViewModel(repository)
        viewModel.onEvent(DealsEvent.UseSuggestion("Primera"))
        runCurrent()
        assertTrue(viewModel.state.value.isLoading)
        viewModel.onEvent(DealsEvent.UseSuggestion("Segunda"))
        advanceUntilIdle()
        assertEquals("Segunda", viewModel.state.value.offers.single().id)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test fun validationCancelsPendingSearchAndStopsLoading() = runTest {
        val repository = object : DealsRepository {
            override val isPreviewData = false
            override suspend fun search(query: String): List<ProductOffer> {
                delay(1000)
                return listOf(sampleOffer())
            }
        }
        val viewModel = DealsViewModel(repository)
        viewModel.onEvent(DealsEvent.UseSuggestion("Switch"))
        runCurrent()
        viewModel.onEvent(DealsEvent.QueryChanged(""))
        viewModel.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.offers.isEmpty())
        assertEquals("Escribe un producto para iniciar la búsqueda.", viewModel.state.value.errorMessage)
    }

    @Test fun offerDetailCanBeOpenedAndClosed() {
        val viewModel = DealsViewModel(FakeDealsRepository())
        val offer = sampleOffer()
        viewModel.onEvent(DealsEvent.OpenOffer(offer))
        assertEquals(offer, viewModel.state.value.selectedOffer)
        viewModel.onEvent(DealsEvent.Back)
        assertEquals(null, viewModel.state.value.selectedOffer)
    }
}

private class FakeDealsRepository(
    private val result: List<ProductOffer> = emptyList(),
    private val error: RemoteSearchException? = null,
) : DealsRepository {
    override val isPreviewData = false
    var calls = 0

    override suspend fun search(query: String): List<ProductOffer> {
        calls += 1
        error?.let { throw it }
        return result
    }
}

private fun sampleOffer() = ProductOffer(
    id = "1",
    store = "Fuente web",
    productName = "Nintendo Switch OLED",
    price = "Ver en fuente",
    shipping = "Consulta en tienda",
    delivery = "Disponibilidad por confirmar",
    rating = "—",
    sourceUrl = "https://example.com/product",
    summary = "Resultado web",
    tags = listOf("Resultado web"),
)
