package com.example.repository

import com.example.models.Asset
import com.example.models.Inspection
import kotlinx.coroutines.flow.StateFlow

interface AssetRepository {
    val assets: StateFlow<List<Asset>>
    suspend fun fetchAssets(): Result<List<Asset>>
    suspend fun addAsset(asset: Asset): Result<Unit>
    suspend fun updateAssetStatus(assetId: String, status: String): Result<Unit>
    suspend fun submitInspection(inspection: Inspection): Result<Unit>
    suspend fun getInspectionHistory(assetId: String): Result<List<Inspection>>
    suspend fun deleteAsset(assetId: String): Result<Unit>
    suspend fun updateAsset(asset: Asset): Result<Unit>
}
