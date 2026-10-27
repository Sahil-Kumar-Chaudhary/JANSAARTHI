package com.example.jansaarthi.ui.screens.rag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jansaarthi.App
import com.example.jansaarthi.data.repository.RagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RagViewModel(
    private val repository: RagRepository = try { App.instance.ragRepository } catch (e: Exception) { RagRepository() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(RagUiState())
    val uiState: StateFlow<RagUiState> = _uiState.asStateFlow()

    fun onQueryInputChanged(input: String) {
        _uiState.update { it.copy(queryInput = input, errorMessage = null) }
    }

    fun onModeSelected(mode: String) {
        if (mode in listOf("hybrid", "semantic", "lexical")) {
            _uiState.update { it.copy(selectedMode = mode) }
        }
    }

    fun submitQuery(queryOverride: String? = null) {
        val queryToSubmit = (queryOverride ?: _uiState.value.queryInput).trim()
        if (queryToSubmit.isEmpty()) return
        if (_uiState.value.isLoading) return // Prevent duplicate submissions while in progress

        val currentMode = _uiState.value.selectedMode
        val userMessage = RagMessage(isUser = true, text = queryToSubmit)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                queryInput = "",
                isLoading = true,
                errorMessage = null,
                lastFailedQuery = null
            )
        }

        viewModelScope.launch {
            val result = repository.askQuestion(
                query = queryToSubmit,
                k = 5,
                mode = currentMode
            )

            result.fold(
                onSuccess = { response ->
                    val assistantText = response.answer
                        ?: (if (response.sources.isEmpty()) {
                            "No matching government schemes were found for your query. Please try rephrasing with different keywords."
                        } else {
                            "Here are the matching schemes from our verified database:"
                        })

                    val aiMessage = RagMessage(
                        isUser = false,
                        text = assistantText,
                        sources = response.sources,
                        llmStatus = response.llmStatus,
                        retrievalMode = response.retrievalMode ?: currentMode
                    )

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + aiMessage,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to retrieve response from JANSAARTHI AI.",
                            lastFailedQuery = queryToSubmit
                        )
                    }
                }
            )
        }
    }

    fun retryLastQuery() {
        val failed = _uiState.value.lastFailedQuery
        if (!failed.isNullOrBlank()) {
            submitQuery(failed)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearConversation() {
        _uiState.update { it.copy(messages = emptyList(), errorMessage = null, lastFailedQuery = null) }
    }
}
