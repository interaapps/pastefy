package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.BackgroundJob
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface BackgroundJobRepository : JpaRepository<BackgroundJob, String> {
    fun deleteByTypeAndEntityId(type: BackgroundJob.Type, entityId: Int)

    @Query(
        value = """
            select * from pastefy_background_jobs
            where (status = 'PENDING' and available_at <= :now)
               or (status = 'RUNNING' and lease_until is not null and lease_until <= :now)
            order by available_at asc
            limit 1
            for update skip locked
        """,
        nativeQuery = true,
    )
    fun findNextForUpdate(@Param("now") now: Instant): BackgroundJob?
}
