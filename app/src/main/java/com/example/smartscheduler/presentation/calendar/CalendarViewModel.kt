package com.example.smartscheduler.presentation.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.AppSettings
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.Task
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.SettingsRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val taskRepository: TaskRepository,
    private val eventRepository: EventRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val today = LocalDate.now()
    private val visibleMonth = MutableStateFlow(YearMonth.from(today))
    private val selectedDate = MutableStateFlow(today)
    private val retryRequests = MutableStateFlow(0)

    private val monthData = visibleMonth.flatMapLatest { yearMonth ->
        val start = yearMonth.atDay(1).atStartOfDay()
        val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay()

        combine(
            taskRepository.observeTasks(start, end),
            eventRepository.observeEvents(start, end),
        ) { tasks, events ->
            CalendarMonthData(
                yearMonth = yearMonth,
                tasks = tasks.filterIsInstance<ScheduledTask>(),
                events = events,
            )
        }
    }

    val uiState: StateFlow<CalendarUiState> = retryRequests.flatMapLatest {
        combine(
            monthData,
            selectedDate,
            settingsRepository.settingsStream,
        ) { data, selectedDate, settings ->
            buildSuccessState(data, selectedDate, settings)
        }.map<CalendarUiState.Success, CalendarUiState> { it }
            .catch { error ->
                emit(
                    CalendarUiState.Error(
                        selectedDate = selectedDate.value,
                        message = error.message ?: context.getString(R.string.calendar_error_fallback),
                    )
                )
            }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState.Loading(today),
        )

    fun handleAction(action: CalendarAction) {
        when (action) {
            is CalendarAction.SelectDate -> selectDate(action.date)
            is CalendarAction.SelectMonth -> selectMonth(action.month)
            is CalendarAction.SelectYear -> selectYear(action.year)
            is CalendarAction.MarkTaskCompleted -> markTaskCompleted(action.taskId)
            is CalendarAction.AddQuickTask -> addQuickTask(action.title, action.description)
            is CalendarAction.AddQuickEvent -> addQuickEvent(action.title, action.description, action.timeSlot)
            CalendarAction.Retry -> retryRequests.update { it + 1 }
            CalendarAction.RequestAddTask,
            CalendarAction.RequestAddEvent,
            CalendarAction.DismissFastAddRequest,
            is CalendarAction.NavigateToScheduleItemCreate,
            is CalendarAction.NavigateToScheduleItemEdit -> Unit
        }
    }

    private fun selectDate(date: LocalDate) {
        selectedDate.value = date
        val newVisibleMonth = YearMonth.from(date)
        if (newVisibleMonth != visibleMonth.value) {
            visibleMonth.value = newVisibleMonth
        }
    }

    private fun selectMonth(month: Month) {
        val currentMonth = visibleMonth.value
        val newMonth = YearMonth.of(currentMonth.year, month)
        visibleMonth.value = newMonth
        selectedDate.update { it.clampedTo(newMonth) }
    }

    private fun selectYear(year: Int) {
        val currentMonth = visibleMonth.value
        val newMonth = YearMonth.of(year, currentMonth.month)
        visibleMonth.value = newMonth
        selectedDate.update { it.clampedTo(newMonth) }
    }

    private fun markTaskCompleted(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTask(taskId) as? ScheduledTask ?: return@launch
            val newStatus = if (task.status == Status.COMPLETED) {
                Status.SCHEDULED
            } else {
                Status.COMPLETED
            }
            taskRepository.updateTask(task.copy(status = newStatus))
        }
    }

    private fun addQuickTask(title: String, description: String) {
        viewModelScope.launch {
            val settings = settingsRepository.settingsStream.first()
            val timeSlot = selectedDate.value.toCalendarDefaultTaskSlot(settings.defaultTaskDuration)
            val newTask = ScheduledTask(
                id = UUID.randomUUID().toString(),
                name = title,
                description = description,
                status = Status.SCHEDULED,
                priority = Priority.MEDIUM,
                isLocked = false,
                deadline = null,
                preferredPlaceTime = null,
                startTime = timeSlot.startTime,
                endTime = timeSlot.endTime,
            )
            taskRepository.createTask(newTask)
        }
    }

    private fun addQuickEvent(title: String, description: String, timeSlot: TimeSlot) {
        viewModelScope.launch {
            val newEvent = Event(
                id = UUID.randomUUID().toString(),
                name = title,
                description = description,
                startTime = timeSlot.startTime,
                endTime = timeSlot.endTime,
            )
            eventRepository.createEvent(newEvent)
        }
    }

    private fun buildSuccessState(
        data: CalendarMonthData,
        selectedDate: LocalDate,
        settings: AppSettings,
    ): CalendarUiState.Success {
        val markers = buildMarkers(data.yearMonth, data.tasks, data.events)
        val selectedDayStart = selectedDate.atStartOfDay()
        val selectedDayEnd = selectedDate.plusDays(1).atStartOfDay()
        val selectedDayTasks = data.tasks.filter { it.overlaps(selectedDayStart, selectedDayEnd) }
        val selectedDayEvents = data.events.filter { it.overlaps(selectedDayStart, selectedDayEnd) }

        return CalendarUiState.Success(
            selectedDate = selectedDate,
            visibleYear = data.yearMonth.year,
            visibleMonth = data.yearMonth.month,
            yearOptions = buildYearOptions(today.year),
            months = buildMonthChips(data.yearMonth.month),
            days = buildCalendarMonthDays(
                yearMonth = data.yearMonth,
                selectedDate = selectedDate,
                markers = markers,
                today = today,
                weekStartsOnMonday = settings.weekStartsOnMonday,
            ),
            selectedDayItems = (selectedDayTasks.map { it.toAgendaItem() } + selectedDayEvents.map { it.toAgendaItem() })
                .sortedBy { it.startTime },
            suggestedTaskTimeSlot = selectedDate.toCalendarDefaultTaskSlot(settings.defaultTaskDuration),
            suggestedEventTimeSlot = selectedDate.toCalendarDefaultEventSlot(settings.defaultEventDuration),
            weekStartsOnMonday = settings.weekStartsOnMonday,
        )
    }

    private fun buildMarkers(
        yearMonth: YearMonth,
        tasks: List<ScheduledTask>,
        events: List<Event>,
    ): Map<LocalDate, CalendarDayMarker> {
        return (1..yearMonth.lengthOfMonth()).associate { day ->
            val date = yearMonth.atDay(day)
            val start = date.atStartOfDay()
            val end = date.plusDays(1).atStartOfDay()
            val dayTasks = tasks.filter { it.overlaps(start, end) }
            val dayEvents = events.filter { it.overlaps(start, end) }

            date to CalendarDayMarker(
                taskCount = dayTasks.size,
                eventCount = dayEvents.size,
                hasHighPriorityTask = dayTasks.any { it.priority == Priority.HIGH },
            )
        }
    }

    private fun buildMonthChips(selectedMonth: Month): List<CalendarMonthChipUiModel> {
        return Month.values().map { month ->
            CalendarMonthChipUiModel(
                month = month,
                label = month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                selected = month == selectedMonth,
            )
        }
    }

    private fun buildYearOptions(currentYear: Int): List<Int> {
        return (currentYear - 5..currentYear + 5).toList()
    }

    private fun LocalDate.clampedTo(yearMonth: YearMonth): LocalDate {
        val day = dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth())
        return yearMonth.atDay(day)
    }

    private fun ScheduledTask.overlaps(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return startTime < endTime && this.endTime > startTime && this.startTime < endTime
    }

    private fun Event.overlaps(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return startTime < endTime && this.endTime > startTime && this.startTime < endTime
    }

    private fun ScheduledTask.toAgendaItem(): CalendarAgendaItemUiModel.TaskItem {
        return CalendarAgendaItemUiModel.TaskItem(
            id = id,
            title = name,
            description = description,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            status = status,
            priority = priority,
        )
    }

    private fun Event.toAgendaItem(): CalendarAgendaItemUiModel.EventItem {
        return CalendarAgendaItemUiModel.EventItem(
            id = id,
            title = name,
            description = description,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
        )
    }

    private fun LocalDate.toCalendarDefaultEventSlot(duration: Duration): TimeSlot {
        val start = if (this == today) {
            val now = LocalDateTime.now().withSecond(0).withNano(0)
            val minutesToAdd = if (now.minute < 30) 30 - now.minute else 60 - now.minute
            now.plusMinutes(minutesToAdd.toLong())
        } else {
            atTime(10, 0)
        }

        return CalendarDefaultTimeSlot(
            startTime = start,
            endTime = start.plus(duration),
        )
    }

    private fun LocalDate.toCalendarDefaultTaskSlot(duration: Duration): TimeSlot {
        val start = atTime(10, 0)
        return CalendarDefaultTimeSlot(
            startTime = start,
            endTime = start.plus(duration),
        )
    }

    private data class CalendarMonthData(
        val yearMonth: YearMonth,
        val tasks: List<ScheduledTask>,
        val events: List<Event>,
    )

    private data class CalendarDefaultTimeSlot(
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
    ) : TimeSlot
}
