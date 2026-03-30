package com.mindguard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindguard.data.model.ActionType
import com.mindguard.data.model.AppCategory
import com.mindguard.data.model.StrictLevel
import com.mindguard.data.model.TriggerType
import com.mindguard.data.model.UsageEvent
import com.mindguard.data.model.UsageRule
import com.mindguard.data.repository.UsageEventRepository
import com.mindguard.data.repository.UsageRuleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.UUID

@HiltWorker
class BehaviorAnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageEventRepository: UsageEventRepository,
    private val usageRuleRepository: UsageRuleRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val days: Int = 7
        val dateRange: Pair<String, String> = usageEventRepository.getDateRangeForLastDays(days)
        val startDate: String = dateRange.first
        val endDate: String = dateRange.second
        
        val eventsList: List<UsageEvent> = usageEventRepository.getEventsForDateRange(startDate, endDate).first()
        
        val entertainmentEvents: List<UsageEvent> = eventsList.filter { event: UsageEvent -> 
            event.category == AppCategory.PASSIVE_ENTERTAINMENT 
        }
        
        val groupedEvents: Map<String, List<UsageEvent>> = entertainmentEvents.groupBy { event: UsageEvent -> 
            event.packageName 
        }
        
        val appSummaries: List<Pair<String, Int>> = groupedEvents.map { entry: Map.Entry<String, List<UsageEvent>> ->
            val totalMinutes: Int = entry.value.sumOf { event: UsageEvent -> event.durationMinutes }
            Pair(entry.key, totalMinutes)
        }.sortedByDescending { summary: Pair<String, Int> -> 
            summary.second 
        }.take(3)

        for (summary: Pair<String, Int> in appSummaries) {
            val packageName: String = summary.first
            val totalMinutes: Int = summary.second
            val averageDaily: Int = totalMinutes / days
            
            if (averageDaily > 60) {
                val suggestionId: String = "suggested_" + UUID.randomUUID().toString()
                val suggestion = UsageRule(
                    id = suggestionId,
                    targetCategory = null,
                    targetPackage = packageName,
                    triggerType = TriggerType.CUMULATIVE_DAILY,
                    thresholdMinutes = 60,
                    actionType = ActionType.NOTIFY,
                    blockDurationMinutes = 0,
                    isEnabled = false,
                    strictLevel = StrictLevel.BALANCED
                )
                usageRuleRepository.insertRule(suggestion)
            }
        }

        return Result.success()
    }
}
