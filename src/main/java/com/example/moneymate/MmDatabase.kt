package com.example.moneymate

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert

// =====================================================================
//  MONEYMATE - THE DATABASE THAT LIVES ON THE PHONE (Room)
//  Each class marked @Entity is one table. Every row carries the uid of
//  the signed-in Firebase user, so two people can share one phone.
//  "dirty = true" means: this row has changes that are not yet in Firestore.
// =====================================================================

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val name: String,
    val lastFour: String,
    val balanceCents: Int,
    val dirty: Boolean = false
)

@Entity(tableName = "transactions")
data class TxEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val merchant: String,
    val category: String,
    val amountCents: Int,
    val date: String,
    val accountId: String,
    val dirty: Boolean = false,
    val receiptUrl: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val category: String,
    val limitCents: Int,
    val dirty: Boolean = false
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val name: String,
    val targetCents: Int,
    val savedCents: Int,
    val dirty: Boolean = false
)

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val uid: String,
    val currency: String,
    val theme: String,
    val hideBalances: Boolean,
    val budgetAlerts: Boolean,
    val dirty: Boolean = false
)

/**
 * A shopping list of rows that were deleted while the phone was offline, so the
 * cloud copy can be cleaned up later instead of resurrecting them.
 */
@Entity(tableName = "deleted")
data class DeletedEntity(
    @PrimaryKey val key: String,
    val uid: String,
    val collection: String,
    val docId: String
)

// --------------------------------------------------------------------- DAO
@Dao
interface MmDao {

    // ---- accounts ----
    @Query("SELECT * FROM accounts WHERE uid = :uid ORDER BY name")
    suspend fun accounts(uid: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE uid = :uid AND dirty = 1")
    suspend fun dirtyAccounts(uid: String): List<AccountEntity>

    @Upsert
    suspend fun upsertAccounts(items: List<AccountEntity>)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)

    @Query("DELETE FROM accounts WHERE uid = :uid")
    suspend fun wipeAccounts(uid: String)

    // ---- transactions ----
    @Query("SELECT * FROM transactions WHERE uid = :uid ORDER BY date DESC, id DESC")
    suspend fun transactions(uid: String): List<TxEntity>

    @Query("SELECT * FROM transactions WHERE uid = :uid AND dirty = 1")
    suspend fun dirtyTransactions(uid: String): List<TxEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE uid = :uid")
    suspend fun transactionCount(uid: String): Int

    @Upsert
    suspend fun upsertTransactions(items: List<TxEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions WHERE uid = :uid")
    suspend fun wipeTransactions(uid: String)

    // ---- budgets ----
    @Query("SELECT * FROM budgets WHERE uid = :uid ORDER BY category")
    suspend fun budgets(uid: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE uid = :uid AND dirty = 1")
    suspend fun dirtyBudgets(uid: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE uid = :uid AND LOWER(category) = LOWER(:category) LIMIT 1")
    suspend fun budgetFor(uid: String, category: String): BudgetEntity?

    @Upsert
    suspend fun upsertBudgets(items: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: String)

    @Query("DELETE FROM budgets WHERE uid = :uid")
    suspend fun wipeBudgets(uid: String)

    // ---- goals ----
    @Query("SELECT * FROM goals WHERE uid = :uid ORDER BY name")
    suspend fun goals(uid: String): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE uid = :uid AND dirty = 1")
    suspend fun dirtyGoals(uid: String): List<GoalEntity>

    @Upsert
    suspend fun upsertGoals(items: List<GoalEntity>)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    @Query("DELETE FROM goals WHERE uid = :uid")
    suspend fun wipeGoals(uid: String)

    // ---- profile + settings ----
    @Query("SELECT * FROM profile WHERE uid = :uid")
    suspend fun profile(uid: String): ProfileEntity?

    @Upsert
    suspend fun upsertProfile(item: ProfileEntity)

    @Query("SELECT * FROM settings WHERE uid = :uid")
    suspend fun settings(uid: String): SettingsEntity?

    @Query("SELECT * FROM settings WHERE uid = :uid AND dirty = 1")
    suspend fun dirtySettings(uid: String): List<SettingsEntity>

    @Upsert
    suspend fun upsertSettings(item: SettingsEntity)

    // ---- deletions waiting to reach the cloud ----
    @Query("SELECT * FROM deleted WHERE uid = :uid")
    suspend fun tombstones(uid: String): List<DeletedEntity>

    @Upsert
    suspend fun addTombstone(item: DeletedEntity)

    @Query("DELETE FROM deleted WHERE key = :key")
    suspend fun removeTombstone(key: String)

    @Query("DELETE FROM deleted WHERE uid = :uid")
    suspend fun wipeTombstones(uid: String)
}

// --------------------------------------------------------------------- database
@Database(
    entities = [
        AccountEntity::class,
        TxEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        ProfileEntity::class,
        SettingsEntity::class,
        DeletedEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MmDatabase : RoomDatabase() {
    abstract fun mm(): MmDao

    companion object {
        @Volatile
        private var instance: MmDatabase? = null

        fun get(context: Context): MmDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MmDatabase::class.java,
                    "moneymate.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instance = it }
            }
    }
}