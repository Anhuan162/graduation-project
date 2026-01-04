package com.graduation.project.forum.mapper;

import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.mapper.FileMetadataMapper;
import com.graduation.project.common.mapper.UserMapper;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.PostRequest;
import com.graduation.project.forum.dto.PostResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class, FileMetadataMapper.class })
public interface PostMapper {

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

  @Mapping(target = "topicId", source = "post.topic.id")
  @Mapping(target = "author", source = "post.author")
  @Mapping(target = "approvedById", source = "post.approvedBy.id")
  @Mapping(target = "id", source = "post.id")
  @Mapping(target = "title", source = "post.title")
  @Mapping(target = "content", source = "post.content")
  @Mapping(target = "createdDateTime", source = "post.createdDateTime")
  @Mapping(target = "approvedAt", source = "post.approvedAt")
  @Mapping(target = "postStatus", source = "post.postStatus")
  @Mapping(target = "reactionCount", source = "post.reactionCount")
  @Mapping(target = "deleted", source = "post.deleted")
  @Mapping(target = "attachments", source = "attachments")
  @Mapping(target = "isLiked", source = "isLiked")
  @Mapping(target = "isSaved", expression = "java(false)")
  PostResponse toPostResponse(Post post, List<FileMetadataResponse> attachments, Boolean isLiked);
}
