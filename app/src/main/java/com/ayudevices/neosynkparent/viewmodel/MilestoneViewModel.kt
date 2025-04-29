package com.ayudevices.neosynkparent.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ayudevices.neosynkparent.data.database.chatdatabase.MilestoneResponseEntity
import com.ayudevices.neosynkparent.data.repository.MilestoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.forEach
import javax.inject.Inject


@HiltViewModel
class MilestoneViewModel @Inject constructor(
    private val repository: MilestoneRepository
) : ViewModel() {
    // Progress tracking
    private val _overallProgress = MutableStateFlow(0)
    val overallProgress: StateFlow<Int> = _overallProgress


    private val _motorProgress = MutableStateFlow(0)
    val motorProgress: StateFlow<Int> = _motorProgress

    private val _sensoryProgress = MutableStateFlow(0)
    val sensoryProgress: StateFlow<Int> = _sensoryProgress

    private val _communicationProgress = MutableStateFlow(0)
    val communicationProgress: StateFlow<Int> = _communicationProgress

    private val _feedingProgress = MutableStateFlow(0)
    val feedingProgress: StateFlow<Int> = _feedingProgress

    // Current leap tracking
    private val _currentLeap = MutableStateFlow(1)
    val currentLeap: StateFlow<Int> = _currentLeap

    private val leapQuestions = mapOf(
        1 to mapOf(
            "M" to listOf(
                "Is your baby making smoother arm or leg movements than before?",
                "Is your baby kicking or waving their arms more when they get excited?",
                "Does your baby move their arms when they see someone or something they know?"
            ),
            "S" to listOf(
                "Does your baby seem more interested in looking around or focusing on objects with strong contrasts?",
                "Have you noticed your baby reacting to your voice or facial expressions or people during this time?",
                "Can your baby now focus better on objects farther than 8-10 inches away?",
                "Has your baby started reacting more to their surroundings, like turning toward sounds or lights?"
            ),
            "C" to listOf(
                "Has your baby started smiling for the first time?",
                "Did you spot your baby producing a tear for the first time while crying?",
                "Does your baby look at you more when you talk or sing?"
            ),
            "F" to listOf(
                "Does your baby react to your voice or face while feeding, like pausing sucking or looking at you?",
                "Is your baby able to latch onto your nipple or bottle?",
                "Is your baby more easily distracted by light or movement during feeding?"
            )
        ),
        2 to mapOf(
            "M" to listOf(
                "Does your baby stare at their hands or move them with curiosity?",
                "Is your baby trying to touch or grab things on their own?",
                "Does your baby seem more aware of patterns?"
            ),
            "S" to listOf(
                "Does your baby respond more to different sounds around them?",
                "Is your baby more alert to changes in light or movement?",
                "Does your baby look around more actively, trying to follow things with their eyes?"
            ),
            "C" to listOf(
                "Does your baby cry or make more sounds when they want attention?",
                "Is your baby reacting more when you talk or sing to them?",
                "Does your baby make more sounds than before, like cooing or gurgling?",
                "Does your baby smile when you talk or make eye contact?"
            ),
            "F" to listOf(
                "Is your baby showing signs of hunger in a predictable way?",
                "Does your baby look at your face or around the room while feeding?",
                "Does your baby stop sucking for a moment and then start again while feeding?"
            )
        ),
        3 to mapOf(
            "M" to listOf(
                "Have your baby's movements started to look smoother and less jerky?",
                "Does your baby turn their head or body in a flowing way when looking at things?",
                "Does your baby enjoy games with gentle movement?",
                "Is your baby more focused on moving their body or using their voice during this stage?",
                "Is your baby beginning to enjoy smooth movements?",
                "Does your baby seem quieter or move less than they usually do?"
            ),
            "S" to listOf(
                "Has your baby shown more interest in putting objects in their mouth?",
                "Have you seen signs that your baby is starting to notice small changes in light, sound, or motion?"
            ),
            "C" to listOf(
                "Has your baby started sucking their thumb more often than before?",
                "Do you notice your baby experimenting with their voice in new ways?",
                "Does your baby seem quieter or move less or showing signs of shyness than they usually do?"
            ),
            "F" to listOf(
                "Does your baby seem more interested in putting things in their mouth during feeding?",
                "Does your baby suck more gently and calmly?",
                "Is your baby turning their head or making smooth motions while feeding?",
                "Does your baby occasionally lose interest or pause more during feeding?"
            )
        ),
        4 to mapOf(
            "M" to listOf(
                "Has your baby started reaching for and grabbing objects on purpose?",
                "Can your baby now hold onto a toy for longer than before?",
                "Does your baby seem to enjoy games with movement more than before?",
                "Have you noticed smoother hand or body coordination during playtime?"
            ),
            "S" to listOf(
                "Does your baby seem to recognize that multiple things happening together form one event?",
                "Has your baby started responding to music or children's songs with excitement or motion?",
                "Does your baby show interest in exploring toys using more than one sense?"
            ),
            "C" to listOf(
                "Has your baby started babbling sounds like 'mama,' 'baba,' or 'tata'?",
                "Does your baby try to repeat sounds they hear from you or others?",
                "Do you notice your baby being more expressive with facial cues or sounds?"
            ),
            "F" to listOf(
                "Does your baby seem more interested in grabbing or reaching for the bottle or breast?",
                "Has your baby started to babble or make sounds while feeding?",
                "Does your baby seem more focused on the feeding process?"
            )
        ),
        5 to mapOf(
            "M" to listOf(
                "Can your baby pick up objects and place them inside a basket or box?",
                "Have you seen your baby trying to take things out of a container?",
                "Does your baby enjoy stacking or moving blocks around?",
                "Has your baby started exploring how toys fit 'in,' 'on,' or 'under' things?"
            ),
            "S" to listOf(
                "Is your baby more curious about how things are related to each other?",
                "Does your baby look around more to understand where sounds or people are coming from?",
                "Is your baby interested in watching how objects move when placed in different positions?"
            ),
            "C" to listOf(
                "Does your baby cry or fuss when you leave the room?",
                "Does your baby calm down when they hear your voice?",
                "Have you noticed your baby making new sounds or gestures to get your attention?",
                "Is your baby showing emotions when someone walks away or comes back?",
                "Does your baby respond more when you speak to them from a distance?"
            ),
            "F" to listOf(
                "Does your baby show interest in exploring the food?",
                "Does your baby seem more aware of you during feeding?",
                "Is your baby able to manage small pauses during feeding?",
                "Has your baby started to notice the difference in taste or texture between different foods?"
            )
        ),
        6 to mapOf(
            "M" to listOf(
                "Is your baby trying to sort or group objects during playtime?",
                "Have you noticed your baby exploring objects by picking up and comparing them?",
                "Does your baby enjoy placing toys in and out of containers repeatedly?",
                "Can your baby explore different parts of an object?"
            ),
            "S" to listOf(
                "Does your baby seem interested in how things feel?",
                "Is your baby able to notice the difference between a light and heavy object?",
                "Can your baby tell the difference between big and small items?",
                "Have you seen your baby comparing animals or toys that look or sound similar?"
            ),
            "C" to listOf(
                "Does your baby react differently to your happy, sad, or angry face?",
                "Is your baby showing interest in pointing or labeling familiar things?"
            ),
            "F" to listOf(
                "Does your baby seem to recognize different types of food?",
                "Has your baby started to show preferences for certain flavors or textures?",
                "Is your baby more curious about food?",
                "Does your baby try to feed themselves or imitate your actions while eating?"
            )
        ),
        7 to mapOf(
            "M" to listOf(
                "Is your baby trying to put things on top of each other or fit them together?",
                "Have you noticed your baby doing actions in a specific order?",
                "Does your baby try to fit objects into each other?",
                "Is your baby becoming more purposeful in their movements?"
            ),
            "S" to listOf(
                "Is your baby more interested in how things go together or fit?",
                "Have you seen your baby watching a sequence of actions closely?",
                "Does your baby seem fascinated by repeating the same sequence?"
            ),
            "C" to listOf(
                "Does your baby try to 'name' things using sounds?",
                "Is your baby pointing at different things one after another?",
                "Do they expect a response when they make a sound or point?"
            ),
            "F" to listOf(
                "Is your baby trying to feed themselves?",
                "Does your baby seem more interested in the process of eating?",
                "Has your baby started to show more control while eating?"
            )
        ),
        8 to mapOf(
            "M" to listOf(
                "Is your baby trying to copy you while doing simple chores?",
                "Does your baby try to stack blocks or line up objects?",
                "Has your baby started helping with tasks?",
                "Can your baby follow a simple step-by-step action?"
            ),
            "S" to listOf(
                "Does your baby show interest in the texture of water or soap?",
                "Is your baby exploring how different household items feel?",
                "Does your baby notice changes in routines or environments?"
            ),
            "C" to listOf(
                "Is your baby trying to say words when imitating you?",
                "Does your baby point to or name items in order?",
                "Is your baby using gestures to ask for help?"
            ),
            "F" to listOf(
                "Does your baby show interest in helping with feeding?",
                "Has your baby started following a pattern while eating?",
                "Does your baby seem to recognize the steps in feeding?",
                "Is your baby able to understand the process of eating?"
            )
        ),
        9 to mapOf(
            "M" to listOf(
                "Is your toddler trying new ways to move objects?",
                "Does your toddler use different tools to complete a task?",
                "Can your toddler plan and act on a series of movements?",
                "Have you noticed your toddler changing how they play?"
            ),
            "S" to listOf(
                "Is your toddler exploring how different actions make different sounds?",
                "Does your child pay closer attention to details?",
                "Is your toddler more aware of how their environment reacts?"
            ),
            "C" to listOf(
                "Is your toddler trying different ways to get what they want?",
                "Have you seen your toddler copy your actions more?",
                "Is your child starting to 'negotiate' or test responses?"
            ),
            "F" to listOf(
                "Does your toddler try to feed themselves more independently?",
                "Has your toddler started to understand the order of eating?",
                "Is your toddler starting to recognize different types of food?",
                "Does your toddler seem more patient during mealtime?"
            )
        ),
        10 to mapOf(
            "M" to listOf(
                "Is your toddler trying to dress or undress on their own?",
                "Can your toddler open and close containers?",
                "Is your toddler climbing with more confidence?",
                "Does your toddler try to help with simple chores?",
                "Is your toddler holding a crayon and trying to draw?"
            ),
            "S" to listOf(
                "Is your toddler showing more interest in music?",
                "Does your toddler enjoy playing with toys that make sounds?",
                "Is your toddler paying attention to different textures?",
                "Does your toddler explore toys by comparing their features?"
            ),
            "C" to listOf(
                "Is your toddler using words like 'me' and 'you' correctly?",
                "Does your toddler express their preferences?",
                "Is your toddler trying to copy adult behavior?",
                "Can your toddler follow simple instructions?"
            ),
            "F" to listOf(
                "Does your toddler try to feed themselves more independently?",
                "Has your toddler started to show preferences for certain foods?",
                "Does your toddler understand when it's mealtime?",
                "Is your toddler more interested in feeding themselves?"
            )
        )
    )

    private val leapTitles = mapOf(
        1 to "The World of Sensation",
        2 to "The World of Patterns",
        3 to "The World of Smooth ",
        4 to "The World of Events",
        5 to "The World of Relationships",
        6 to "The World of Categories",
        7 to "The World of Sequences",
        8 to "The World of Programs",
        9 to "The World of Principles",
        10 to "The World of Systems"
    )

    private val leapDescriptions = mapOf(
        1 to "Around week 4 to 6, your baby goes through their first developmental leap. During this time, their senses develop rapidly, and they start noticing new things in the world.",
        2 to "Around week 7 to 10, your baby enters their second developmental leap. This is when they start recognizing patterns in what they see, hear, and feel.",
        3 to "Around week 11 to 12, your baby enters Leap 3, where they start noticing smooth changes in their surroundings.",
        4 to "Starting around week 14, your baby enters Leap 4, where they begin to understand that different things happen together as an event.",
        5 to "Starting around week 22, your baby enters Leap 5, where they start to understand how things relate to each other.",
        6 to "Starting around week 33, your baby enters Leap 6, where they begin to understand that the world is made up of different categories.",
        7 to "Around 41 weeks, your baby enters Leap 7, where they start to understand that things happen in a specific order (sequences).",
        8 to "Around 51 weeks (just before their 1st birthday), your baby enters Leap 8, where they start to understand that a goal can be achieved in different ways.",
        9 to "Around 59 weeks (just before 14 months), your toddler enters Leap 9, where they begin to understand principles—the \"why\" behind actions.",
        10 to "Around 70 weeks (about 16 months), your toddler goes through Leap 10, their final recorded mental leap."
    )


    // Track responses
    private val _responses = mutableMapOf<Int, MutableMap<String, MutableMap<String, Boolean>>>()

    // Current question tracking
    private val _currentCategory = MutableStateFlow("M")
    val currentCategory: StateFlow<String> = _currentCategory

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    // Track completed leaps
    private val _completedLeaps = MutableStateFlow(setOf<Int>())
    val completedLeaps: StateFlow<Set<Int>> = _completedLeaps

    init {
        loadSavedProgress()
    }

    fun getCurrentQuestions(leap: Int, category: String): List<String> {
        return leapQuestions[leap]?.get(category) ?: emptyList()
    }

    fun getLeapTitle(leap: Int): String {
        return leapTitles[leap] ?: ""
    }

    fun getLeapDescription(leap: Int): String {
        return leapDescriptions[leap] ?: ""
    }

    val currentQuestions: List<String>
        get() = leapQuestions[_currentLeap.value]?.get(_currentCategory.value) ?: emptyList()



    fun answerQuestion(answer: Boolean) {
        val leap = _currentLeap.value
        val category = _currentCategory.value
        val questionIndex = _currentQuestionIndex.value
        val question = currentQuestions.getOrNull(questionIndex) ?: return

        // Store in memory
        _responses.getOrPut(leap) { mutableMapOf() }
            .getOrPut(category) { mutableMapOf() }
            .put(question, answer)

        // Save to database
        viewModelScope.launch {
            try {
                repository.saveResponse(
                    MilestoneResponseEntity(
                        leap = leap,
                        category = category,
                        question = question,
                        answer = answer
                    )
                )
            } catch (e: Exception) {
                Log.e("MilestoneViewModel", "Error inserting response: ${e.message}")
            }
        }

        // Update progress
        calculateCategoryProgress(category)

        // Move to next question
        if (questionIndex < currentQuestions.size - 1) {
            _currentQuestionIndex.value = questionIndex + 1
        } else {
            moveToNextCategory()
        }
    }

    private fun moveToNextCategory() {
        val categories = listOf("M", "S", "C", "F")
        val currentIndex = categories.indexOf(_currentCategory.value)

        if (currentIndex < categories.size - 1) {
            _currentCategory.value = categories[currentIndex + 1]
            _currentQuestionIndex.value = 0
        } else {
            // All categories completed for this leap
            markLeapAsCompleted(_currentLeap.value)
            calculateOverallProgress()
        }
    }

    private fun markLeapAsCompleted(leap: Int) {
        _completedLeaps.value = _completedLeaps.value + leap
    }

    private fun calculateCategoryProgress(category: String) {
        var totalQuestions = 0
        var yesCount = 0

        for (leap in 1..10) {
            val responses = _responses[leap]?.get(category) ?: continue
            val questions = leapQuestions[leap]?.get(category) ?: continue

            totalQuestions += questions.size
            yesCount += responses.count { it.value }
        }

        val progress = if (totalQuestions > 0) {
            (yesCount.toFloat() / totalQuestions.toFloat() * 100).toInt()
        } else {
            0
        }

        when (category) {
            "M" -> _motorProgress.value = progress
            "S" -> _sensoryProgress.value = progress
            "C" -> _communicationProgress.value = progress
            "F" -> _feedingProgress.value = progress
        }
    }

    private fun calculateOverallProgress() {
        val totalCategories = 4
        val totalProgress = _motorProgress.value + _sensoryProgress.value +
                _communicationProgress.value + _feedingProgress.value

        _overallProgress.value = if (totalCategories > 0) {
            totalProgress / totalCategories
        } else {
            0
        }
    }


    private fun loadSavedProgress() {
        viewModelScope.launch {
            repository.getAllResponses().collect { savedResponses ->
                val completed = mutableSetOf<Int>()

                // Load saved responses into _responses
                savedResponses.forEach { response ->
                    val leap = response.leap
                    val category = response.category
                    val question = response.question
                    val answer = response.answer

                    _responses.getOrPut(leap) { mutableMapOf() }
                        .getOrPut(category) { mutableMapOf() }
                        .put(question, answer)
                }

                // Check which leaps are completed
                for (leap in 1..10) {
                    val allCategoriesCompleted = listOf("M", "S", "C", "F").all { category ->
                        val questions = leapQuestions[leap]?.get(category) ?: emptyList()
                        val responses = _responses[leap]?.get(category) ?: emptyMap()
                        questions.size == responses.size
                    }
                    if (allCategoriesCompleted) {
                        completed.add(leap)
                    }
                }

                _completedLeaps.value = completed
                calculateAllCategoryProgress()
            }
        }
    }

    fun changeCategory(category: String) {
        _currentCategory.value = category
        _currentQuestionIndex.value = 0
    }

    fun getResponse(leap: Int, category: String, question: String): Boolean? {
        return _responses[leap]?.get(category)?.get(question)
    }

    fun changeLeap(newLeap: Int) {
        if (newLeap in 1..10) {
            _currentLeap.value = newLeap
            _currentCategory.value = "M"
            _currentQuestionIndex.value = 0
            calculateAllCategoryProgress()
        }
    }

    private fun calculateAllCategoryProgress() {
        listOf("M", "S", "C", "F").forEach { category ->
            calculateCategoryProgress(category)
        }
        calculateOverallProgress()
    }

    fun getCategoryProgress(category: String): Int {
        return when (category) {
            "M" -> _motorProgress.value
            "S" -> _sensoryProgress.value
            "C" -> _communicationProgress.value
            "F" -> _feedingProgress.value
            else -> 0
        }
    }

    fun isLeapCompleted(leap: Int): Boolean {
        return _completedLeaps.value.contains(leap)
    }

    fun getCompletedQuestionsCount(leap: Int, category: String): Int {
        return _responses[leap]?.get(category)?.size ?: 0
    }

    fun getTotalQuestionsCount(leap: Int, category: String): Int {
        return leapQuestions[leap]?.get(category)?.size ?: 0
    }
}