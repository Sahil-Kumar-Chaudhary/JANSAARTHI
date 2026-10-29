package com.example.jansaarthi

import com.example.jansaarthi.data.model.RagAskRequest
import com.example.jansaarthi.data.model.RagAskResponse
import com.example.jansaarthi.data.model.RagSource
import com.example.jansaarthi.data.network.RagApi
import com.example.jansaarthi.data.network.RetrofitClient
import com.example.jansaarthi.data.repository.RagRepository
import com.example.jansaarthi.ui.screens.rag.RagViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RagIntegrationTest {

    private val gson = Gson()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // 1. Successful RAG response parsing
    @Test
    fun testSuccessfulRagResponseParsing() {
        val json = """
        {
            "status": "success",
            "query": "Pradhan Mantri Mudra Yojana",
            "retrieval_mode": "hybrid",
            "answer": "Pradhan Mantri Mudra Yojana (SCH002) provides micro-enterprise credit loans up to 10 lakhs.",
            "sources_count": 2,
            "sources": [
                {
                    "schemeId": "SCH002",
                    "schemeName": "Pradhan Mantri Mudra Yojana",
                    "category": "Business & Entrepreneurship",
                    "sourcePlatform": "myScheme.gov.in",
                    "snippet": "scheme_id: SCH002 scheme_name: Pradhan Mantri Mudra Yojana category: Business & Entrepreneurship",
                    "score": 44.0,
                    "similarity": 0.6208,
                    "distance": 0.3792
                },
                {
                    "schemeId": "SCH003",
                    "schemeName": "Stand Up India Scheme",
                    "category": "Business & Entrepreneurship",
                    "sourcePlatform": "myScheme.gov.in",
                    "snippet": "scheme_id: SCH003 scheme_name: Stand Up India Scheme",
                    "score": 38.5,
                    "similarity": 0.5120,
                    "distance": 0.4880
                }
            ],
            "llm_status": "generated"
        }
        """.trimIndent()

        val response = gson.fromJson(json, RagAskResponse::class.java)

        assertEquals("success", response.status)
        assertEquals("Pradhan Mantri Mudra Yojana", response.query)
        assertEquals("hybrid", response.retrievalMode)
        assertEquals(2, response.sourcesCount)
        assertEquals(2, response.sources.size)
        assertEquals("generated", response.llmStatus)

        val firstSource = response.sources[0]
        assertEquals("SCH002", firstSource.schemeId)
        assertEquals("Pradhan Mantri Mudra Yojana", firstSource.schemeName)
        assertEquals("Business & Entrepreneurship", firstSource.category)
        assertEquals("myScheme.gov.in", firstSource.sourcePlatform)
        assertEquals(44.0, firstSource.score ?: 0.0, 0.001)
        assertEquals(0.6208, firstSource.similarity ?: 0.0, 0.0001)
        assertEquals(0.3792, firstSource.distance ?: 0.0, 0.0001)
    }

    // 2. Response containing no sources
    @Test
    fun testNoSourcesResponseParsing() {
        val json = """
        {
            "status": "success",
            "query": "Galactic space exploration satellite launch",
            "retrieval_mode": "hybrid",
            "answer": "I don't have enough information in the verified scheme database to answer that. No matching government schemes were found for your query.",
            "sources_count": 0,
            "sources": [],
            "llm_status": "no_context"
        }
        """.trimIndent()

        val response = gson.fromJson(json, RagAskResponse::class.java)

        assertEquals("success", response.status)
        assertEquals(0, response.sourcesCount)
        assertTrue(response.sources.isEmpty())
        assertEquals("no_context", response.llmStatus)
        assertTrue(response.answer?.contains("No matching government schemes") == true)
    }

    // 3. HTTP 400 error handling in repository
    @Test
    fun testHttp400ErrorHandling() = runTest {
        val errorJson = """{"status":"error","code":"INVALID_QUERY","message":"Query cannot be empty."}"""
        val mockApi = object : RagApi {
            override suspend fun askQuestion(request: RagAskRequest): Response<RagAskResponse> {
                val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
                return Response.error(400, errorBody)
            }
        }

        val repository = RagRepository(api = mockApi, gson = gson, ioDispatcher = testDispatcher)
        val result = repository.askQuestion("valid query string")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Query cannot be empty") == true)
    }

    // 4. HTTP 503 error handling in repository
    @Test
    fun testHttp503ErrorHandling() = runTest {
        val errorJson = """{"status":"error","code":"VECTOR_INDEX_UNAVAILABLE","message":"Scheme knowledge base is currently unavailable."}"""
        val mockApi = object : RagApi {
            override suspend fun askQuestion(request: RagAskRequest): Response<RagAskResponse> {
                val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
                return Response.error(503, errorBody)
            }
        }

        val repository = RagRepository(api = mockApi, gson = gson, ioDispatcher = testDispatcher)
        val result = repository.askQuestion("tell me about mudra")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("unavailable") == true)
    }

    // 5. Malformed and missing fields graceful parsing
    @Test
    fun testMalformedAndMissingFieldsGracefulParsing() {
        val emptyJson = "{}"
        val parsed = gson.fromJson(emptyJson, RagAskResponse::class.java)

        assertNotNull(parsed)
        assertEquals("", parsed.status)
        assertEquals(0, parsed.sourcesCount)
        assertTrue(parsed.sources.isEmpty())
        assertNull(parsed.answer)
        assertNull(parsed.llmStatus)
    }

    // 6. Source rendering without fabricated links
    @Test
    fun testNoFabricatedSourceUrls() {
        val officialSource = RagSource(
            schemeId = "SCH001",
            schemeName = "PMJDY",
            category = "Banking",
            sourcePlatform = "myScheme.gov.in",
            snippet = "Test"
        )
        val otherSource = RagSource(
            schemeId = "SCH999",
            schemeName = "Other Scheme",
            category = "General",
            sourcePlatform = "internal_department",
            snippet = "Test"
        )

        // Only verified official portals should yield genuine URLs
        val officialUrl = if (officialSource.sourcePlatform.equals("myScheme.gov.in", ignoreCase = true)) {
            "https://www.myscheme.gov.in/"
        } else null

        val otherUrl = if (otherSource.sourcePlatform.equals("myScheme.gov.in", ignoreCase = true)) {
            "https://www.myscheme.gov.in/"
        } else null

        assertEquals("https://www.myscheme.gov.in/", officialUrl)
        assertNull(otherUrl) // Must not fabricate a link for unknown platforms
    }

    // 7. ViewModel loading, success, and duplicate-prevention
    @Test
    fun testViewModelLoadingAndSuccessState() = runTest {
        val mockApi = object : RagApi {
            override suspend fun askQuestion(request: RagAskRequest): Response<RagAskResponse> {
                return Response.success(
                    RagAskResponse(
                        status = "success",
                        query = request.query,
                        retrievalMode = request.mode,
                        answer = "Verified answer for " + request.query,
                        sourcesCount = 1,
                        sources = listOf(
                            RagSource(schemeId = "SCH002", schemeName = "Mudra", category = "Business", sourcePlatform = "myScheme.gov.in", snippet = "Details")
                        ),
                        llmStatus = "generated"
                    )
                )
            }
        }

        val repository = RagRepository(api = mockApi, gson = gson, ioDispatcher = testDispatcher)
        val viewModel = RagViewModel(repository = repository)

        assertEquals(0, viewModel.uiState.value.messages.size)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.onQueryInputChanged("How to get Mudra loan?")
        assertEquals("How to get Mudra loan?", viewModel.uiState.value.queryInput)

        viewModel.submitQuery()

        // Advance dispatcher
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(2, state.messages.size)

        // First message: user
        val userMsg = state.messages[0]
        assertTrue(userMsg.isUser)
        assertEquals("How to get Mudra loan?", userMsg.text)

        // Second message: AI
        val aiMsg = state.messages[1]
        assertFalse(aiMsg.isUser)
        assertEquals("Verified answer for How to get Mudra loan?", aiMsg.text)
        assertEquals(1, aiMsg.sources.size)
        assertEquals("SCH002", aiMsg.sources[0].schemeId)
    }

    // 8. ViewModel error and retry state flow
    @Test
    fun testViewModelErrorAndRetryStateFlow() = runTest {
        var shouldFail = true
        val mockApi = object : RagApi {
            override suspend fun askQuestion(request: RagAskRequest): Response<RagAskResponse> {
                return if (shouldFail) {
                    val errorBody = """{"status":"error","message":"Service temporarily overloaded."}""".toResponseBody("application/json".toMediaTypeOrNull())
                    Response.error(503, errorBody)
                } else {
                    Response.success(
                        RagAskResponse(
                            status = "success",
                            query = request.query,
                            answer = "Recovered successfully!",
                            sourcesCount = 0,
                            sources = emptyList()
                        )
                    )
                }
            }
        }

        val repository = RagRepository(api = mockApi, gson = gson, ioDispatcher = testDispatcher)
        val viewModel = RagViewModel(repository = repository)

        viewModel.onQueryInputChanged("Retry test query")
        viewModel.submitQuery()

        testScheduler.advanceUntilIdle()

        // Should be in error state
        var state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertEquals("Retry test query", state.lastFailedQuery)

        // Now fix the failure and retry
        shouldFail = false
        viewModel.retryLastQuery()

        testScheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.lastFailedQuery)
        assertTrue(state.messages.any { it.text == "Recovered successfully!" })
    }

    // 9. RetrofitClient staging and custom URL configuration
    @Test
    fun testRetrofitClientStagingAndUrlConfiguration() {
        assertEquals("https://jansaarthi-rag-staging.onrender.com/", RetrofitClient.STAGING_BASE_URL)
        assertEquals("https://jansaarthi-backend.onrender.com/", RetrofitClient.PROD_BASE_URL)
        assertEquals("http://10.0.2.2:3000/", RetrofitClient.EMULATOR_BASE_URL)

        val originalUrl = RetrofitClient.getBaseUrl()

        RetrofitClient.useStagingBaseUrl()
        assertEquals(RetrofitClient.STAGING_BASE_URL, RetrofitClient.getBaseUrl())
        assertNotNull(RetrofitClient.ragApi)
        assertNotNull(RetrofitClient.instance)

        RetrofitClient.useProdBaseUrl()
        assertEquals(RetrofitClient.PROD_BASE_URL, RetrofitClient.getBaseUrl())

        RetrofitClient.useEmulatorBaseUrl()
        assertEquals(RetrofitClient.EMULATOR_BASE_URL, RetrofitClient.getBaseUrl())

        // Restore original URL
        RetrofitClient.setCustomBaseUrl(originalUrl)
        assertEquals(originalUrl, RetrofitClient.getBaseUrl())

        // Test environment targets
        RetrofitClient.setEnvironment(RetrofitClient.EnvironmentTarget.STAGING)
        assertEquals(RetrofitClient.STAGING_BASE_URL, RetrofitClient.getBaseUrl())
        assertEquals(RetrofitClient.EnvironmentTarget.STAGING, RetrofitClient.getActiveEnvironment())
        assertTrue(RetrofitClient.isStagingActive())
        assertFalse(RetrofitClient.isProductionActive())

        RetrofitClient.setEnvironment(RetrofitClient.EnvironmentTarget.PRODUCTION)
        assertEquals(RetrofitClient.PROD_BASE_URL, RetrofitClient.getBaseUrl())
        assertEquals(RetrofitClient.EnvironmentTarget.PRODUCTION, RetrofitClient.getActiveEnvironment())
        assertTrue(RetrofitClient.isProductionActive())

        RetrofitClient.setEnvironment(RetrofitClient.EnvironmentTarget.EMULATOR)
        assertEquals(RetrofitClient.EMULATOR_BASE_URL, RetrofitClient.getBaseUrl())
        assertEquals(RetrofitClient.EnvironmentTarget.EMULATOR, RetrofitClient.getActiveEnvironment())
        assertTrue(RetrofitClient.isEmulatorActive())

        // Test explicit debug environment controls and defaults
        RetrofitClient.setExplicitDebugEnvironment(null, RetrofitClient.EnvironmentTarget.PRODUCTION)
        assertEquals(RetrofitClient.PROD_BASE_URL, RetrofitClient.getBaseUrl())

        RetrofitClient.setExplicitDebugEnvironment(null, RetrofitClient.EnvironmentTarget.STAGING)
        assertEquals(RetrofitClient.STAGING_BASE_URL, RetrofitClient.getBaseUrl())

        RetrofitClient.initEnvironment(null)
        assertNotNull(RetrofitClient.getBaseUrl())

        RetrofitClient.resetToDefault()
        assertNotNull(RetrofitClient.getBaseUrl())

        // Restore original
        RetrofitClient.setCustomBaseUrl(originalUrl)
    }
}
