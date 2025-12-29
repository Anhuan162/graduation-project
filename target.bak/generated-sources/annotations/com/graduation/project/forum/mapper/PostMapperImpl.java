package com.graduation.project.forum.mapper;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.AttachmentResponse;
import com.graduation.project.forum.dto.PostAuthorResponse;
import com.graduation.project.forum.dto.PostPermissionsResponse;
import com.graduation.project.forum.dto.PostRequest;
import com.graduation.project.forum.dto.PostResponse;
import com.graduation.project.forum.dto.PostStatsResponse;
import com.graduation.project.forum.dto.PostUserStateResponse;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Topic;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public Post toPost(PostRequest request, Topic topic, User author) {
        if ( request == null && topic == null && author == null ) {
            return null;
        }

        Post.PostBuilder post = Post.builder();

        if ( request != null ) {
            post.title( request.getTitle() );
            post.content( request.getContent() );
        }
        post.topic( topic );
        post.author( author );

        return post.build();
    }

    @Override
    public PostResponse toPostResponse(Post post, List<String> urls, String excerpt, PostAuthorResponse author, PostStatsResponse stats, PostUserStateResponse userState, PostPermissionsResponse permissions, List<AttachmentResponse> attachments) {
        if ( post == null && urls == null && excerpt == null && author == null && stats == null && userState == null && permissions == null && attachments == null ) {
            return null;
        }

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        if ( post != null ) {
            postResponse.id( post.getId() );
            postResponse.title( post.getTitle() );
            postResponse.content( post.getContent() );
            postResponse.topicId( mapTopicId( post.getTopic() ) );
            postResponse.createdById( mapUserId( post.getAuthor() ) );
            postResponse.approvedById( mapUserId( post.getApprovedBy() ) );
            postResponse.createdDateTime( post.getCreatedDateTime() );
            postResponse.lastModifiedDateTime( post.getLastModifiedDateTime() );
            postResponse.approvedAt( post.getApprovedAt() );
            postResponse.postStatus( post.getPostStatus() );
            postResponse.reactionCount( post.getReactionCount() );
            postResponse.deleted( post.isDeleted() );
        }
        List<String> list = urls;
        if ( list != null ) {
            postResponse.urls( new ArrayList<String>( list ) );
        }
        postResponse.excerpt( excerpt );
        postResponse.author( author );
        postResponse.stats( stats );
        postResponse.userState( userState );
        postResponse.permissions( permissions );
        List<AttachmentResponse> list1 = attachments;
        if ( list1 != null ) {
            postResponse.attachments( new ArrayList<AttachmentResponse>( list1 ) );
        }

        return postResponse.build();
    }
}
