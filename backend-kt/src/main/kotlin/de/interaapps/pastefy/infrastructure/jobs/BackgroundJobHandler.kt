package de.interaapps.pastefy.infrastructure.jobs

import de.interaapps.pastefy.entities.BackgroundJob

interface BackgroundJobHandler {
    val type: BackgroundJob.Type
    fun handle(job: BackgroundJob)
}
