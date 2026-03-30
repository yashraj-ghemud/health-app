package com.mindguard.intervention

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val gson = Gson()
    private var quotes: List<MotivationalQuote> = emptyList()
    private var quotesByCategory: Map<String, List<MotivationalQuote>> = emptyMap()
    
    data class MotivationalQuote(
        val text: String,
        val author: String? = null,
        val category: String,
        val tags: List<String> = emptyList()
    )
    
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                loadQuotesFromAssets()
                categorizeQuotes()
                Timber.d("Loaded ${quotes.size} motivational quotes")
            } catch (e: Exception) {
                Timber.e(e, "Error loading quotes from assets")
                loadDefaultQuotes()
            }
        }
    }
    
    private fun loadQuotesFromAssets() {
        val jsonString = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
        val quoteType = object : TypeToken<List<MotivationalQuote>>() {}.type
        quotes = gson.fromJson(jsonString, quoteType) ?: emptyList()
    }
    
    private fun loadDefaultQuotes() {
        // Fallback quotes if assets loading fails
        quotes = listOf(
            MotivationalQuote(
                text = "Your time is limited, don't waste it living someone else's life.",
                author = "Steve Jobs",
                category = "focus",
                tags = listOf("time", "purpose")
            ),
            MotivationalQuote(
                text = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "passion",
                tags = listOf("work", "love")
            ),
            MotivationalQuote(
                text = "Focus on being productive instead of busy.",
                author = "Tim Ferriss",
                category = "productivity",
                tags = listOf("focus", "efficiency")
            ),
            MotivationalQuote(
                text = "You don't have to be great to start, but you have to start to be great.",
                author = "Zig Ziglar",
                category = "motivation",
                tags = listOf("start", "greatness")
            ),
            MotivationalQuote(
                text = "The secret of getting ahead is getting started.",
                author = "Mark Twain",
                category = "action",
                tags = listOf("start", "progress")
            ),
            MotivationalQuote(
                text = "Don't watch the clock; do what it does. Keep going.",
                author = "Sam Levenson",
                category = "persistence",
                tags = listOf("time", "persistence")
            ),
            MotivationalQuote(
                text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                author = "Winston Churchill",
                category = "resilience",
                tags = listOf("success", "failure", "courage")
            ),
            MotivationalQuote(
                text = "Believe you can and you're halfway there.",
                author = "Theodore Roosevelt",
                category = "belief",
                tags = listOf("belief", "confidence")
            ),
            MotivationalQuote(
                text = "The future depends on what you do today.",
                author = "Mahatma Gandhi",
                category = "action",
                tags = listOf("future", "today", "action")
            ),
            MotivationalQuote(
                text = "Quality is not an act, it is a habit.",
                author = "Aristotle",
                category = "habit",
                tags = listOf("quality", "habit")
            ),
            MotivationalQuote(
                text = "Your limitation—it's only your imagination.",
                author = "Unknown",
                category = "mindset",
                tags = listOf("limitation", "imagination")
            ),
            MotivationalQuote(
                text = "Great things never come from comfort zones.",
                author = "Unknown",
                category = "growth",
                tags = listOf("comfort", "growth", "challenge")
            ),
            MotivationalQuote(
                text = "Dream it. Wish it. Do it.",
                author = "Unknown",
                category = "action",
                tags = listOf("dream", "wish", "do")
            ),
            MotivationalQuote(
                text = "Success doesn't just find you. You have to go out and get it.",
                author = "Unknown",
                category = "success",
                tags = listOf("success", "effort")
            ),
            MotivationalQuote(
                text = "The harder you work for something, the greater you'll feel when you achieve it.",
                author = "Unknown",
                category = "effort",
                tags = listOf("work", "achievement", "effort")
            ),
            MotivationalQuote(
                text = "Don't stop when you're tired. Stop when you're done.",
                author = "Unknown",
                category = "persistence",
                tags = listOf("tired", "done", "persistence")
            ),
            MotivationalQuote(
                text = "Wake up with determination. Go to bed with satisfaction.",
                author = "Unknown",
                category = "daily",
                tags = listOf("morning", "evening", "determination")
            ),
            MotivationalQuote(
                text = "Do something today that your future self will thank you for.",
                author = "Sean Patrick Flanery",
                category = "future",
                tags = listOf("future", "today", "gratitude")
            ),
            MotivationalQuote(
                text = "Little things make big days.",
                author = "Unknown",
                category = "habits",
                tags = listOf("small", "big", "habits")
            ),
            MotivationalQuote(
                text = "It's going to be hard, but hard does not mean impossible.",
                author = "Unknown",
                category = "challenge",
                tags = listOf("hard", "impossible", "challenge")
            )
        )
        
        categorizeQuotes()
    }
    
    private fun categorizeQuotes() {
        quotesByCategory = quotes.groupBy { it.category }
    }
    
    fun getRandomQuote(): String {
        if (quotes.isEmpty()) {
            return "Focus on being productive instead of busy."
        }
        return quotes.random().text
    }
    
    fun getRandomQuoteFromCategory(category: String): String {
        val categoryQuotes = quotesByCategory[category] ?: return getRandomQuote()
        return categoryQuotes.random().text
    }
    
    fun getQuoteWithTag(tag: String): String {
        val taggedQuotes = quotes.filter { quote ->
            quote.tags.any { it.equals(tag, ignoreCase = true) }
        }
        return if (taggedQuotes.isNotEmpty()) {
            taggedQuotes.random().text
        } else {
            getRandomQuote()
        }
    }
    
    fun getQuotesForMood(mood: String): List<String> {
        return when (mood.lowercase()) {
            "tired" -> {
                quotes.filter { quote ->
                    quote.tags.any { it.equals("energy", ignoreCase = true) } ||
                    quote.tags.any { it.equals("rest", ignoreCase = true) } ||
                    quote.category == "persistence"
                }.map { it.text }
            }
            "distracted" -> {
                quotes.filter { quote ->
                    quote.tags.any { it.equals("focus", ignoreCase = true) } ||
                    quote.tags.any { it.equals("attention", ignoreCase = true) } ||
                    quote.category == "focus"
                }.map { it.text }
            }
            "unmotivated" -> {
                quotes.filter { quote ->
                    quote.tags.any { it.equals("motivation", ignoreCase = true) } ||
                    quote.tags.any { it.equals("start", ignoreCase = true) } ||
                    quote.category == "motivation"
                }.map { it.text }
            }
            "stressed" -> {
                quotes.filter { quote ->
                    quote.tags.any { it.equals("calm", ignoreCase = true) } ||
                    quote.tags.any { it.equals("peace", ignoreCase = true) } ||
                    quote.category == "mindset"
                }.map { it.text }
            }
            else -> quotes.map { it.text }
        }
    }
    
    fun getQuoteForTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> {
                // Morning quotes
                getQuoteWithTag("morning") ?: getRandomQuoteFromCategory("action")
            }
            in 12..17 -> {
                // Afternoon quotes
                getQuoteWithTag("focus") ?: getRandomQuoteFromCategory("productivity")
            }
            in 18..22 -> {
                // Evening quotes
                getQuoteWithTag("evening") ?: getRandomQuoteFromCategory("reflection")
            }
            else -> {
                // Night quotes
                getQuoteWithTag("rest") ?: getRandomQuoteFromCategory("mindset")
            }
        }
    }
    
    fun getAllCategories(): Set<String> {
        return quotesByCategory.keys
    }
    
    fun getAllTags(): Set<String> {
        return quotes.flatMap { it.tags }.toSet()
    }
    
    fun getQuoteCount(): Int = quotes.size
    
    fun searchQuotes(query: String): List<String> {
        return quotes.filter { quote ->
            quote.text.contains(query, ignoreCase = true) ||
            quote.author?.contains(query, ignoreCase = true) == true ||
            quote.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }.map { it.text }
    }
}
