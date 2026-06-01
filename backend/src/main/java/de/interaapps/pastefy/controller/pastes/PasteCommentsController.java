package de.interaapps.pastefy.controller.pastes;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.HTTPException;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.PasteComment;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.requests.paste.CreatePasteCommentRequest;
import de.interaapps.pastefy.model.responses.ActionResponse;
import de.interaapps.pastefy.model.responses.paste.PasteCommentMarkerResponse;
import de.interaapps.pastefy.model.responses.paste.PasteCommentResponse;
import de.interaapps.pastefy.model.responses.user.PublicUserResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/paste")
public class PasteCommentsController extends HttpController {
    private static final int DEFAULT_PAGE_LIMIT = 10;
    private static final int MAX_PAGE_LIMIT = 30;
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MAX_LINE_RANGE = 1000;

    @Get("/{pasteId}/comments")
    @With({"auth-login-required-read", "awaiting-access-check", "blocked-check"})
    public List<PasteCommentResponse> getComments(Exchange exchange, @Path("pasteId") String pasteId, @Attrib("user") User user) {
        Paste.getAccessiblePasteOrFail(pasteId, user);

        int page = positiveQueryParameter(exchange, "page", 1);
        int pageLimit = Math.min(positiveQueryParameter(exchange, "page_limit", DEFAULT_PAGE_LIMIT), MAX_PAGE_LIMIT);

        Query<PasteComment> query = Repo.get(PasteComment.class)
                .where("paste", pasteId)
                .whereNull("parentId");

        if (exchange.query("line") != null) {
            int line = positiveQueryParameter(exchange, "line", 1);
            query.where("lineFrom", line);
        }

        return query
                .order("created_at", true)
                .limit(pageLimit)
                .offset((page - 1) * pageLimit)
                .all()
                .stream()
                .map(comment -> new PasteCommentResponse(comment, true))
                .collect(Collectors.toList());
    }

    @Get("/{pasteId}/comments/markers")
    @With({"auth-login-required-read", "awaiting-access-check", "blocked-check"})
    public List<PasteCommentMarkerResponse> getLineMarkers(@Path("pasteId") String pasteId, @Attrib("user") User user) {
        Paste.getAccessiblePasteOrFail(pasteId, user);

        Map<Integer, Map<String, PublicUserResponse>> profilesByLine = new LinkedHashMap<>();
        Repo.get(PasteComment.class)
                .where("paste", pasteId)
                .whereNotNull("lineFrom")
                .order("created_at")
                .all()
                .forEach(comment -> addMarkerProfiles(profilesByLine, comment));

        return profilesByLine.entrySet().stream().map(entry -> {
            PasteCommentMarkerResponse marker = new PasteCommentMarkerResponse(entry.getKey());
            List<PublicUserResponse> profiles = new ArrayList<>(entry.getValue().values());
            marker.profiles = profiles.stream().limit(2).collect(Collectors.toList());
            marker.additionalProfiles = Math.max(0, profiles.size() - marker.profiles.size());
            return marker;
        }).collect(Collectors.toList());
    }

    @Post("/{pasteId}/comments")
    @With({"rate-limiter", "auth", "awaiting-access-check", "blocked-check"})
    public PasteCommentResponse createComment(@Path("pasteId") String pasteId, @Body CreatePasteCommentRequest request, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("comments:create");

        Paste.getAccessiblePasteOrFail(pasteId, user);
        validateRequest(request);

        PasteComment comment = new PasteComment();
        comment.setPaste(pasteId);
        comment.setUserId(user.getId());
        comment.setContent(request.content.trim());
        comment.setLineFrom(request.lineFrom);
        comment.setLineTo(request.lineTo);

        if (request.parentId != null) {
            PasteComment parent = PasteComment.get(request.parentId);
            if (parent == null || !pasteId.equals(parent.getPaste()))
                throw new NotFoundException();
            comment.setParentId(parent.getParentId() == null ? parent.getId() : parent.getParentId());
        }

        comment.save();
        return new PasteCommentResponse(comment, false);
    }

    @Delete("/{pasteId}/comments/{commentId}")
    @With({"rate-limiter", "auth", "awaiting-access-check", "blocked-check"})
    public ActionResponse deleteComment(@Path("pasteId") String pasteId, @Path("commentId") String commentId, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("comments:delete");

        Paste paste = Paste.get(pasteId);
        PasteComment comment = PasteComment.get(parseCommentId(commentId));
        if (paste == null || comment == null || !pasteId.equals(comment.getPaste()))
            throw new NotFoundException();

        if (!user.isAdmin()
                && !user.getId().equals(comment.getUserId())
                && (paste.getUserId() == null || !paste.getUserId().equals(user.getId())))
            throw new PermissionsDeniedException();

        Repo.get(PasteComment.class).where("parentId", comment.getId()).delete();
        comment.delete();
        return new ActionResponse(true);
    }

    private void addMarkerProfiles(Map<Integer, Map<String, PublicUserResponse>> profilesByLine, PasteComment comment) {
        User commentUser = comment.getUser();
        if (commentUser == null)
            return;

        profilesByLine
                .computeIfAbsent(comment.getLineFrom(), ignored -> new LinkedHashMap<>())
                .putIfAbsent(commentUser.getId(), new PublicUserResponse(commentUser));
    }

    private void validateRequest(CreatePasteCommentRequest request) {
        if (request == null || request.content == null || request.content.trim().isEmpty())
            throw new HTTPException(400, "Comment content is required");
        if (request.content.trim().length() > MAX_CONTENT_LENGTH)
            throw new HTTPException(400, "Comment content must not exceed 2000 characters");
        if (request.lineFrom == null && request.lineTo != null)
            throw new HTTPException(400, "line_to requires line_from");
        if (request.lineFrom != null && request.lineFrom < 1)
            throw new HTTPException(400, "line_from must be positive");
        if (request.lineTo != null && request.lineTo < request.lineFrom)
            throw new HTTPException(400, "line_to must not be smaller than line_from");
        if (request.lineTo != null && request.lineTo - request.lineFrom >= MAX_LINE_RANGE)
            throw new HTTPException(400, "Comment line ranges must not exceed 1000 lines");
    }

    private int positiveQueryParameter(Exchange exchange, String name, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(exchange.query(name, String.valueOf(fallback))));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int parseCommentId(String commentId) {
        try {
            return Integer.parseInt(commentId);
        } catch (NumberFormatException ignored) {
            throw new NotFoundException();
        }
    }
}
