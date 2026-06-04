package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.folder.CreateFolderRequest
import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.entities.Folder
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.repositories.FolderRepository
import de.interaapps.pastefy.repositories.PasteRepository
import jakarta.persistence.criteria.Predicate
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FolderService(
    private val folderRepository: FolderRepository,
    private val pasteRepository: PasteRepository,
    private val pasteService: PasteService,
    private val pasteResponseMapper: PasteResponseMapper,
    private val properties: PastefyProperties,
) {
    @Transactional
    fun create(request: CreateFolderRequest, user: User): Folder {
        val parent = request.parent?.let(folderRepository::findByKey)?.takeIf { it.userId == user.id }
        return folderRepository.save(
            Folder(name = request.name, userId = user.id, parent = parent?.key),
        )
    }

    fun list(request: HttpServletRequest, user: User?): List<FolderResponse> {
        val page = request.positiveInt("page", 1)
        val pageLimit = request.positiveInt("page_limit", 10).coerceAtMost(100)
        val search = request.getParameter("search")?.trim()?.takeIf(String::isNotEmpty)
        val requestedUserId = request.getParameter("user_id")?.trim()?.takeIf(String::isNotEmpty)
        if (user == null && !properties.listPastes) throw PermissionsDeniedException()
        val filteredUserId = when {
            user?.isAdmin == true -> requestedUserId
            user != null -> user.id
            else -> null
        }
        val specification = Specification<Folder> { root, _, builder ->
            val predicates = mutableListOf<Predicate>()
            filteredUserId?.let { predicates += builder.equal(root.get<String>("userId"), it) }
            search?.let { predicates += builder.like(builder.lower(root.get("name")), "%${it.lowercase()}%") }
            request.getParameter("parent")?.let { predicates += builder.equal(root.get<String>("parent"), it) }
            builder.and(*predicates.toTypedArray())
        }
        return folderRepository.findAll(
            specification,
            PageRequest.of(page - 1, pageLimit, Sort.by(Sort.Direction.DESC, "createdAt")),
        ).content.map {
            map(
                it,
                fetchChildren = false,
                fetchSubChildren = false,
                fetchPastes = false,
                showPrivate = false
            )
        }
    }

    fun get(id: String): Folder = folderRepository.findByKey(id) ?: throw NotFoundException()

    fun map(
        folder: Folder,
        fetchChildren: Boolean = true,
        fetchSubChildren: Boolean = true,
        fetchPastes: Boolean = true,
        showPrivate: Boolean = false,
    ): FolderResponse {
        val children = if (fetchChildren) {
            folderRepository.findAllByParentOrderByUpdatedAtDesc(folder.key)
                .map { map(it, fetchSubChildren, fetchSubChildren, fetchPastes, showPrivate) }
        } else null
        val pastes = if (fetchChildren && fetchPastes) {
            pasteRepository.findAllByFolderOrderByUpdatedAtDesc(folder.key)
                .filter { showPrivate || !it.isPrivate }
                .map { pasteResponseMapper.map(it) }
        } else null
        return FolderResponse(
            exists = true,
            id = folder.key,
            name = folder.name,
            userId = folder.userId,
            children = children,
            pastes = pastes,
            created = folder.createdAt?.toString() ?: "0000-00-00 00:00:00",
        )
    }

    @Transactional
    fun delete(folder: Folder) {
        folderRepository.findAllByParent(folder.key).forEach(::delete)
        pasteRepository.findAllByFolderOrderByUpdatedAtDesc(folder.key).forEach(pasteService::delete)
        folderRepository.delete(folder)
    }
}

private fun HttpServletRequest.positiveInt(name: String, default: Int): Int =
    getParameter(name)?.toIntOrNull()?.coerceAtLeast(1) ?: default
