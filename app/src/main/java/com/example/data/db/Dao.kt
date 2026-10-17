package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AuditLogEntity
import com.example.data.model.CompanyEntity
import com.example.data.model.LocationEntity
import com.example.data.model.ManufacturingOrderEntity
import com.example.data.model.OfflineDraftEntity
import com.example.data.model.ReconciliationRecordEntity
import com.example.data.model.StockBalanceEntity
import com.example.data.model.StockLedgerEntity
import com.example.data.model.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<CompanyEntity>)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY code ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)
}

@Dao
interface StockBalanceDao {
    @Query("SELECT * FROM stock_balances ORDER BY diameterMm ASC, locationCode ASC")
    fun getAllBalances(): Flow<List<StockBalanceEntity>>

    @Query("SELECT * FROM stock_balances WHERE locationCode = :locationCode")
    fun getBalancesByLocation(locationCode: String): Flow<List<StockBalanceEntity>>

    @Query("""
        SELECT * FROM stock_balances 
        WHERE companyCode = :companyCode 
        AND locationCode = :locationCode 
        AND diameterMm = :diameterMm 
        AND lengthM = :lengthM 
        AND grade = :grade 
        AND finish = :finish 
        AND batchNumber = :batchNumber
        LIMIT 1
    """)
    suspend fun findBalance(
        companyCode: String,
        locationCode: String,
        diameterMm: Int,
        lengthM: Double,
        grade: String,
        finish: String,
        batchNumber: String
    ): StockBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(balance: StockBalanceEntity): Long

    @Query("DELETE FROM stock_balances WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM stock_balances WHERE totalPieces <= 0 AND balanceKg <= 0.0")
    suspend fun purgeZeroBalances()
}

@Dao
interface StockLedgerDao {
    @Query("SELECT * FROM stock_ledger ORDER BY createdAt DESC")
    fun getAllLedger(): Flow<List<StockLedgerEntity>>

    @Query("SELECT * FROM stock_ledger WHERE docNumber = :docNumber LIMIT 1")
    suspend fun getByDocNumber(docNumber: String): StockLedgerEntity?

    @Query("SELECT * FROM stock_ledger WHERE batchNumber = :batchNumber ORDER BY createdAt DESC")
    fun getByBatch(batchNumber: String): Flow<List<StockLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLedger(entry: StockLedgerEntity): Long

    @Update
    suspend fun updateLedger(entry: StockLedgerEntity)
}

@Dao
interface ManufacturingOrderDao {
    @Query("SELECT * FROM manufacturing_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<ManufacturingOrderEntity>>

    @Query("SELECT * FROM manufacturing_orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByNumber(orderNumber: String): ManufacturingOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: ManufacturingOrderEntity): Long

    @Update
    suspend fun update(order: ManufacturingOrderEntity)
}

@Dao
interface ReconciliationDao {
    @Query("SELECT * FROM reconciliation_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<ReconciliationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ReconciliationRecordEntity>)

    @Query("DELETE FROM reconciliation_records")
    suspend fun clearAll()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity): Long
}

@Dao
interface OfflineDraftDao {
    @Query("SELECT * FROM offline_drafts ORDER BY createdAt DESC")
    fun getAllDrafts(): Flow<List<OfflineDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: OfflineDraftEntity): Long

    @Query("DELETE FROM offline_drafts WHERE id = :id")
    suspend fun delete(id: Long)
}
