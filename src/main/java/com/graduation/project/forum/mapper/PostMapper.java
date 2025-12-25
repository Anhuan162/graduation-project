package com.graduation.project.forum.mapper;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import java.util.List;
import java.util.UUID;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PostMapper {

  // ===== helpers =====
  default UUID safeTopicId(Post post) {
    return (post != null && post.getTopic() != null) ? post.getTopic().getId() : null;
  }

  default UUID safeAuthorId(Post post) {
    return (post != null && post.getAuthor() != null) ? post.getAuthor().getId() : null;
  }

  default UUID safeUserUuid(User user) {
    return user == null ? null : user.getId();
  }

  // ===== entity create =====
  @Mapping(target = "author", source = "author")
  @Mapping(target = "topic", source = "topic")
  @Mapping(target = "title", source = "request.title")
  @Mapping(target = "content", source = "request.content")
  @Mapping(target = "comments", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdDateTime", ignore = true)
  @Mapping(target = "lastModifiedDateTime", ignore = true)
  @Mapping(target = "postStatus", ignore = true)
  @Mapping(target = "approvedBy", ignore = true)
  @Mapping(target = "approvedAt", ignore = true)
  Post toPost(PostRequest request, Topic topic, User author);

  // ===== response =====
  @Mapping(target = "id", source = "post.id")
  @Mapping(target = "title", source = "post.title")
  @Mapping(target = "content", source = "post.content")

  @Mapping(target = "topicId", expression = "java(safeTopicId(post))")
  @Mapping(target = "createdById", expression = "java(safeAuthorId(post))")
  @Mapping(target = "approvedById", expression = "java(safeUserUuid(post.getApprovedBy()))")

  @Mapping(target = "createdDateTime", source = "post.createdDateTime")
  @Mapping(target = "lastModifiedDateTime", source = "post.lastModifiedDateTime")
  @Mapping(target = "approvedAt", source = "post.approvedAt")
  @Mapping(target = "postStatus", source = "post.postStatus")
  @Mapping(target = "reactionCount", source = "post.reactionCount")
  @Mapping(target = "deleted", source = "post.deleted")
  @Mapping(target = "urls", source = "urls")

  @Mapping(target = "excerpt", source = "excerpt")
  @Mapping(target = "author", source = "author")
  @Mapping(target = "stats", source = "stats")
  @Mapping(target = "userState", source = "userState")
  @Mapping(target = "permissions", source = "permissions")
  @Mapping(target = "attachments", source = "attachments")
  @Mapping(target = "slug", ignore = true)
  PostResponse toPostResponse(
      Post post,
      List<String> urls,
      String excerpt,
      PostAuthorResponse author,
      PostStatsResponse stats,
      PostUserStateResponse userState,
      PostPermissionsResponse permissions,
      List<AttachmentResponse> attachments);
}
