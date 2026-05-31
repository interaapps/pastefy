package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.model.database.Paste
import org.springframework.data.jpa.repository.JpaRepository

interface PasteRepository : JpaRepository<Paste, Int> {
    fun findByKey(key: String): Paste?

    fun existsByKey(key: String): Boolean

    fun findAllByUserId(userId: String): List<Paste>
}