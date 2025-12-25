package com.graduation.project.forum.mapper;

import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Comment;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "content", source = "comment.content")
    @Mapping(target = "authorId", source = "comment.author.id")
    @Mapping(target = "postId", source = "comment.post.id")
    @Mapping(target = "parentId", expression = "java(comment.getParent() == null ? null : comment.getParent().getId())")
    @Mapping(target = "createdDateTime", source = "comment.createdDateTime")
    @Mapping(target = "deleted", source = "comment.deleted")
    @Mapping(target = "reactionCount", source = "comment.reactionCount")
    @Mapping(target = "url", source = "legacyUrl")

    @Mapping(target = "author", source = "author")
    @Mapping(target = "stats", source = "stats")
    @Mapping(target = "userState", source = "userState")
    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "attachments", source = "attachments")
    CommentResponse toResponse(
            Comment comment,
            String legacyUrl,
            PostAuthorResponse author,
            CommentStatsResponse stats,
            CommentUserStateResponse userState,
            CommentPermissionsResponse permissions,
            List<AttachmentResponse> attachments);
}
