package com.graduation.project.forum.mapper;

import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Comment;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "content", source = "comment.content")

    @Mapping(target = "createdDateTime", source = "comment.createdDateTime")

    @Mapping(target = "rootCommentId", source = "comment.rootComment.id")
    @Mapping(target = "replyToUser", source = "comment.replyToUser")

    @Mapping(target = "author", source = "comment.author")
    @Mapping(target = "stats", source = "stats")
    @Mapping(target = "userState", source = "userState")
    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "attachments", source = "attachments")
    @Mapping(target = "deleted", source = "comment.deleted")
    @Mapping(target = "reactionCount", source = "comment.reactionCount")
    CommentResponse toResponse(
            Comment comment,
            CommentStatsResponse stats,
            CommentUserStateResponse userState,
            CommentPermissionsResponse permissions,
            List<AttachmentResponse> attachments);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    PostAuthorResponse toAuthorResponse(com.graduation.project.common.entity.User user);
}