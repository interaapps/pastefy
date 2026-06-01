package de.interaapps.pastefy.model.responses.paste;

import de.interaapps.pastefy.model.database.PasteComment;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.user.PublicUserResponse;
import org.javawebstack.orm.Repo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PasteCommentResponse {
    public int id;
    public String content;
    public Integer parentId;
    public Integer lineFrom;
    public Integer lineTo;
    public String createdAt;
    public PublicUserResponse user;
    public List<PasteCommentResponse> replies = new ArrayList<>();

    public PasteCommentResponse(PasteComment comment, boolean fetchReplies) {
        id = comment.getId();
        content = comment.getContent();
        parentId = comment.getParentId();
        lineFrom = comment.getLineFrom();
        lineTo = comment.getLineTo();
        createdAt = comment.createdAt.toString();

        User commentUser = comment.getUser();
        if (commentUser != null) {
            user = new PublicUserResponse(commentUser);
        }

        if (fetchReplies) {
            replies = Repo.get(PasteComment.class)
                    .where("parentId", comment.getId())
                    .order("created_at")
                    .all()
                    .stream()
                    .map(reply -> new PasteCommentResponse(reply, false))
                    .collect(Collectors.toList());
        }
    }
}
