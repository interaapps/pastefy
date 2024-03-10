package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Filterable;
import org.javawebstack.orm.annotation.Table;

@Table("paste_tags")
public class PasteTag extends Model {
    @Column
    private int id;


    @Column(size = 8)
    @Filterable
    public String paste;

    @Column
    @Filterable
    public String tag;
}
