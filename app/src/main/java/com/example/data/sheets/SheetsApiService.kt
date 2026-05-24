package com.example.data.sheets

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SheetsApiService {

    /**
     * Retrieve all transaction rows from the remote Google Sheet.
     */
    @GET("exec")
    suspend fun getSheetData(
        @Query("action") action: String = "getTransactions"
    ): Response<List<SheetsTransaction>>

    /**
     * Append a new transaction row to the remote Google Sheet.
     */
    @POST("exec")
    suspend fun appendTransaction(
        @Body transaction: SheetsTransaction
    ): Response<SheetsResponse>
}
