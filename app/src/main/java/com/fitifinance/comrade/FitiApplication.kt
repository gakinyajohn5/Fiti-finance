package com.fitifinance.comrade

import android.app.Application
import com.fitifinance.comrade.data.AppDatabase
import com.fitifinance.comrade.engine.AiAdviceEngine
import com.fitifinance.comrade.engine.LocalHeuristicAdviceEngine
import com.fitifinance.comrade.engine.LocationContextEngine
import com.fitifinance.comrade.repository.FinanceRepository

class FitiApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: FinanceRepository
        private set
    lateinit var locationContextEngine: LocationContextEngine
        private set

    /** Defaults to the on-device engine so nothing requires network access out of the box. */
    var adviceEngine: AiAdviceEngine = LocalHeuristicAdviceEngine()

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = FinanceRepository(database)
        locationContextEngine = LocationContextEngine(this)
    }
}
