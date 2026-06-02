package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteAIInfo
import org.springframework.data.jpa.repository.JpaRepository

interface PasteAIInfoRepository : JpaRepository<PasteAIInfo, Int>
