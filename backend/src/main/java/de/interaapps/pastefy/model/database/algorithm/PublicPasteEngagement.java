package de.interaapps.pastefy.model.database.algorithm;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;

import java.sql.Timestamp;

@Dates
public class PublicPasteEngagement extends Model {
    @Column
    public int id;

    @Column
    public int pasteId;

    @Column
    public int score = 0;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public static void addInterestFromPaste(Paste paste, int score){
        new Thread(()->{
            PublicPasteEngagement interestInteraction = Repo.get(PublicPasteEngagement.class).where("pasteId", paste.getId()).first();
            if (interestInteraction == null) {
                interestInteraction = new PublicPasteEngagement();
                interestInteraction.pasteId = paste.getId();
            }
            interestInteraction.score += score;
            interestInteraction.save();
        }).start();
    }
}
