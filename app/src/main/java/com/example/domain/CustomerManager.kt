package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

class CustomerManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val customerDao = db.customerDao()

    val customers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun addCustomer(
        name: String,
        phone: String,
        company: String = "",
        status: String = "Active",
        notes: String = "",
        followUpDays: Int = 3
    ): Long {
        val now = System.currentTimeMillis()
        val nextFollowUp = now + (followUpDays * 24 * 60 * 60 * 1000L)
        return customerDao.insertCustomer(
            CustomerEntity(
                name = name.trim(),
                phone = phone.trim(),
                company = company.trim(),
                status = status,
                notes = notes,
                lastCallTimestamp = now,
                nextFollowUpTimestamp = nextFollowUp
            )
        )
    }

    suspend fun deleteCustomer(id: Long) {
        customerDao.deleteCustomerById(id)
    }

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
        return customerDao.searchCustomers(query)
    }
}
