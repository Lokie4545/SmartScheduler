package com.example.smartscheduler.di.fake

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
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryFakeModule {

    @Provides
    @Singleton
    @FakeRepository
    fun provideTaskRepository(): TaskRepository = createTaskRepository()

    @Provides
    @Singleton
    @FakeRepository
    fun provideEventRepository(): EventRepository = createEventRepository()

    fun createTaskRepository(): TaskRepository {
        val now = LocalDateTime.now()

        return FakeTaskRepository(
            initialTasks = listOf(
                UnscheduledTask(
                    id = "1",
                    name = "Купить молоко",
                    description = "Нужно купить 2л молока в Магните",
                    status = Status.PENDING,
                    priority = Priority.LOW,
                    isLocked = false,
                    deadline = now.plusDays(1),
                    preferredPlaceTime = null,
                    duration = Duration.ofMinutes(30)
                ),
                ScheduledTask(
                    id = "2",
                    name = "Встреча по проекту",
                    description = "Обсуждение архитектуры приложения",
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
                    name = "Зал",
                    description = "Тренировка ног",
                    status = Status.PENDING,
                    priority = Priority.MEDIUM,
                    isLocked = false,
                    deadline = now.plusHours(5),
                    preferredPlaceTime = now.plusHours(3),
                    duration = Duration.ofHours(1).plusMinutes(30)
                ),
                ScheduledTask(
                    id = "4",
                    name = "Проверка почты",
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
                    name = "Срочный багфикс",
                    description = "Починить крэш на главном экране",
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

    fun createEventRepository(): EventRepository {
        val now = LocalDateTime.now()

        return FakeEventRepository(
            initialEvents = listOf(
                Event(
                    id = "event-1",
                    name = "Обед",
                    description = "Перерыв между задачами",
                    startTime = now.withHour(13).withMinute(0).withSecond(0).withNano(0),
                    endTime = now.withHour(14).withMinute(0).withSecond(0).withNano(0)
                ),
                Event(
                    id = "event-2",
                    name = "Созвон",
                    description = "Синхронизация по плану дня",
                    startTime = now.plusHours(3),
                    endTime = now.plusHours(4)
                )
            )
        )
    }
}
