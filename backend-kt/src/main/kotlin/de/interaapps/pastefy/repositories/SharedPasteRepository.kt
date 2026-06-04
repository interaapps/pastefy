package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.SharedPaste
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SharedPasteRepository : JpaRepository<SharedPaste, Int> {
    fun findAllByTargetId(targetId: String): List<SharedPaste>
    fun findAllByTargetIdOrderByUpdatedAtDesc(targetId: String, pageable: Pageable): List<SharedPaste>
    fun deleteByTargetIdOrUserId(targetId: String, userId: String)
}
