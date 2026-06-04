package de.interaapps.pastefy.infrastructure.jobs

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.infrastructure.ai.AiEnabledCondition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@Conditional(AiEnabledCondition::class)
class BackgroundJobConfiguration {
    @Bean
    fun backgroundJobExecutor(properties: PastefyProperties): ThreadPoolTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = properties.ai.jobs.workers.coerceAtLeast(1)
            maxPoolSize = corePoolSize
            queueCapacity = 0
            setThreadNamePrefix("pastefy-job-")
            setWaitForTasksToCompleteOnShutdown(true)
            initialize()
        }
}
