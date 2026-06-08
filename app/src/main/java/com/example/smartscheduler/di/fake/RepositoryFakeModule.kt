package com.example.smartscheduler.di.fake

import android.content.Context
import com.example.smartscheduler.R
import com.example.smartscheduler.data.repository.fake.FakeEventRepository
import com.example.smartscheduler.data.repository.fake.FakeTaskRepository
import com.example.smartscheduler.domain.model.Event
import com.example.smartscheduler.domain.model.Priority
import com.example.smartscheduler.domain.model.ScheduledTask
import com.example.smartscheduler.domain.model.Status
import com.example.smartscheduler.domain.model.UnscheduledTask
import com.example.smartscheduler.domain.repository.EventRepository
import com.example.smartscheduler.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryFakeModule {

    @Provides
    @Singleton
    fun provideTaskRepository(@ApplicationContext context: Context): TaskRepository = createTaskRepository(context)

    @Provides
    @Singleton
    fun provideEventRepository(@ApplicationContext context: Context): EventRepository = createEventRepository(context)

    @Provides
    @Singleton
    @FakeRepository
    fun provideQualifiedTaskRepository(taskRepository: TaskRepository): TaskRepository = taskRepository

    @Provides
    @Singleton
    @FakeRepository
    fun provideQualifiedEventRepository(eventRepository: EventRepository): EventRepository = eventRepository

    private fun createTaskRepository(context: Context): TaskRepository {
        val now = LocalDateTime.now()

        return FakeTaskRepository(
            initialTasks = listOf(
                UnscheduledTask(
                    id = "1",
                    name = context.getString(R.string.fake_task_buy_milk),
                    description = context.getString(R.string.fake_task_buy_milk_description),
                    status = Status.PENDING,
                    priority = Priority.LOW,
                    isLocked = false,
                    deadline = now.plusDays(1),
                    preferredPlaceTime = null,
                    duration = Duration.ofMinutes(30)
                ),
                ScheduledTask(
                    id = "2",
                    name = context.getString(R.string.fake_task_project_meeting),
                    description = context.getString(R.string.fake_task_project_meeting_description),
                    status = Status.SCHEDULED,
                    priority = Priority.HIGH,
                    isLocked = true,
                    deadline = null,
                    preferredPlaceTime = null,
                    startTime = now.plusHours(1),
                    endTime = now.plusHours(2)
                ),
                UnscheduledTask(
                    id = "3",
                    name = context.getString(R.string.fake_task_gym),
                    description = context.getString(R.string.fake_task_gym_description),
                    status = Status.PENDING,
                    priority = Priority.MEDIUM,
                    isLocked = false,
                    deadline = now.plusHours(5),
                    preferredPlaceTime = now.plusHours(3),
                    duration = Duration.ofHours(1).plusMinutes(30)
                ),
                ScheduledTask(
                    id = "4",
                    name = context.getString(R.string.fake_task_email_check),
                    description = null,
                    status = Status.COMPLETED,
                    priority = Priority.LOW,
                    isLocked = false,
                    deadline = null,
                    preferredPlaceTime = null,
                    startTime = now.minusHours(2),
                    endTime = now.minusHours(1)
                ),
                ScheduledTask(
                    id = "5",
                    name = context.getString(R.string.fake_task_urgent_bugfix),
                    description = context.getString(R.string.fake_task_urgent_bugfix_description),
                    status = Status.OVERDUE,
                    priority = Priority.HIGH,
                    isLocked = false,
                    deadline = now.minusMinutes(30),
                    preferredPlaceTime = null,
                    startTime = now.minusHours(1),
                    endTime = now
                )
            )
        )
    }

    private fun createEventRepository(context: Context): EventRepository {
        val now = LocalDateTime.now()

        return FakeEventRepository(
            initialEvents = listOf(
                Event(
                    id = "event-1",
                    name = context.getString(R.string.fake_event_lunch),
                    description = context.getString(R.string.fake_event_lunch_description),
                    startTime = now.withHour(13).withMinute(0).withSecond(0).withNano(0),
                    endTime = now.withHour(14).withMinute(0).withSecond(0).withNano(0)
                ),
                Event(
                    id = "event-2",
                    name = context.getString(R.string.fake_event_call),
                    description = context.getString(R.string.fake_event_call_description),
                    startTime = now.plusHours(3),
                    endTime = now.plusHours(4)
                )
            )
        )
    }
}
