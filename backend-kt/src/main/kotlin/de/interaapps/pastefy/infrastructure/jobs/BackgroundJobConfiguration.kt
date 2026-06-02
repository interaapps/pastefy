package de.interaapps.pastefy.infrastructure.jobs

import de.interaapps.pastefy.config.PastefyProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@ConditionalOnProperty(prefix = "pastefy.ai", name = ["enabled"], havingValue = "true")
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
