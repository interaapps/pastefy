package de.interaapps.pastefy.entities

import jakarta.persistence.*

@Entity
@Table(
    name = "pastefy_paste_stars",
    indexes = [
        Index(name = "pastefy_paste_stars_paste_index", columnList = "paste"),
        Index(name = "pastefy_paste_stars_paste_user_id_index", columnList = "paste, user_id"),
    ],
)
class PasteStar(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int? = null,
    @Column(length = 8, nullable = false) var paste: String = "",
    @Column(length = 8, nullable = false) var userId: String = "",
)
