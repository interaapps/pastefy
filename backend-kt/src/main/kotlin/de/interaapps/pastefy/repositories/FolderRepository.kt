package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.Folder
import org.springframework.data.jpa.repository.JpaRepository

interface FolderRepository : JpaRepository<Folder, Int> {
    fun findByKey(key: String): Folder?
    fun findAllByUserId(userId: String): List<Folder>
    fun findAllByUserIdAndParentIsNull(userId: String): List<Folder>
    fun findAllByParent(parent: String): List<Folder>
    fun deleteByUserId(userId: String)
}
