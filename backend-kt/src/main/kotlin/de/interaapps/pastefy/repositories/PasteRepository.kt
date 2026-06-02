package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.Paste
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PasteRepository : JpaRepository<Paste, Int> {
    fun findByKey(key: String): Paste?

    fun existsByKey(key: String): Boolean

    fun findAllByUserId(userId: String): List<Paste>

    fun deleteByUserId(userId: String)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Paste p set p.indexedInElastic = :indexed where p.id = :id")
    fun updateIndexedInElastic(@Param("id") id: Int, @Param("indexed") indexed: Boolean)
}
