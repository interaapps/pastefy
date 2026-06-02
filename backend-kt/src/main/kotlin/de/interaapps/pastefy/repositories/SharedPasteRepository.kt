package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.SharedPaste
import org.springframework.data.jpa.repository.JpaRepository

interface SharedPasteRepository : JpaRepository<SharedPaste, Int> {
    fun findAllByTargetId(targetId: String): List<SharedPaste>
    fun deleteByTargetIdOrUserId(targetId: String, userId: String)
}
