package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ActivityEntity
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.BudgetEntity
import com.example.data.entity.CustomCategoryEntity
import com.example.data.entity.FinancialObligationEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.ProductivityLogEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY startTimeMillis ASC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY startTimeMillis ASC")
    fun getActivitiesByDate(startOfDay: Long, endOfDay: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY startTimeMillis ASC")
    fun getPendingActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivityById(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityByIdDirect(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE isAcademicOrProfessional = 1 ORDER BY startTimeMillis ASC")
    fun getAcademicAndProfessionalActivities(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM activities WHERE status = 'PENDING' AND endTimeMillis > :now ORDER BY startTimeMillis ASC")
    suspend fun getUpcomingPendingActivities(now: Long): List<ActivityEntity>
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :start AND dateMillis <= :end ORDER BY dateMillis DESC")
    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("SELECT * FROM financial_obligations ORDER BY dueDateMillis ASC")
    fun getAllObligations(): Flow<List<FinancialObligationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObligation(obligation: FinancialObligationEntity): Long

    @Update
    suspend fun updateObligation(obligation: FinancialObligationEntity)

    @Delete
    suspend fun deleteObligation(obligation: FinancialObligationEntity)

    @Query("SELECT * FROM savings_goals ORDER BY deadlineMillis ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoalEntity): Long

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoalEntity)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoalEntity)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY startTimeMillis ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY startTimeMillis ASC")
    fun getAppointmentsForDate(startOfDay: Long, endOfDay: Long): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Delete
    suspend fun delete(appointment: AppointmentEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CustomCategoryEntity): Long

    @Delete
    suspend fun delete(category: CustomCategoryEntity)
}

@Dao
interface ProductivityDao {
    @Query("SELECT * FROM productivity_logs ORDER BY dateMillis DESC")
    fun getAllLogs(): Flow<List<ProductivityLogEntity>>

    @Query("SELECT * FROM productivity_logs WHERE dateMillis >= :start AND dateMillis <= :end LIMIT 1")
    suspend fun getLogForDate(start: Long, end: Long): ProductivityLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProductivityLogEntity): Long
}
