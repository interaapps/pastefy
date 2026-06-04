package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.enums.StorageType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PasteRepository : JpaRepository<Paste, Int>, JpaSpecificationExecutor<Paste> {
    fun findByKey(key: String): Paste?

    fun existsByKey(key: String): Boolean

    fun findAllByUserId(userId: String): List<Paste>
    fun findAllByFolderOrderByUpdatedAtDesc(folder: String): List<Paste>
    fun findAllByUserIdAndFolderIsNullOrderByUpdatedAtDesc(userId: String, pageable: Pageable): List<Paste>
    fun findAllByKeyIn(keys: Collection<String>): List<Paste>
    fun countByUserIdIsNotNull(): Long
    fun countByStorageType(storageType: StorageType): Long

    fun deleteByUserId(userId: String)

    @Query(
        """
        select p from Paste p
        left join PublicPasteEngagement e on e.pasteId = p.id
        where p.visibility = de.interaapps.pastefy.enums.PasteVisibility.PUBLIC
          and p.encrypted = false
          and (:createdAfter is null or p.createdAt > :createdAfter)
        order by coalesce(e.score, 0) desc, p.createdAt desc
        """,
    )
    fun findTrending(@Param("createdAfter") createdAfter: java.time.Instant?, pageable: Pageable): List<Paste>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Paste p set p.indexedInElastic = :indexed where p.id = :id")
    fun updateIndexedInElastic(@Param("id") id: Int, @Param("indexed") indexed: Boolean)
}
