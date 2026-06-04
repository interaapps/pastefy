package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.repositories.FolderRepository
import de.interaapps.pastefy.repositories.PasteRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest

class FolderServiceTest {
    @Test
    fun `rejects anonymous folder listings unless public listing is enabled`() {
        val service = FolderService(
            mock(FolderRepository::class.java),
            mock(PasteRepository::class.java),
            mock(PasteService::class.java),
            mock(PasteResponseMapper::class.java),
            PastefyProperties(listPastes = false),
        )

        assertThrows<PermissionsDeniedException> {
            service.list(MockHttpServletRequest(), null)
        }
    }
}
