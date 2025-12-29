package com.graduation.project.forum.mapper;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.dto.AttachmentResponse;
import com.graduation.project.forum.dto.CommentPermissionsResponse;
import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.dto.CommentStatsResponse;
import com.graduation.project.forum.dto.CommentUserStateResponse;
import com.graduation.project.forum.dto.PostAuthorResponse;
import com.graduation.project.forum.entity.Comment;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentResponse toResponse(Comment comment, CommentStatsResponse stats, CommentUserStateResponse userState, CommentPermissionsResponse permissions, List<AttachmentResponse> attachments) {
        if ( comment == null && stats == null && userState == null && permissions == null && attachments == null ) {
            return null;
        }

        CommentResponse.CommentResponseBuilder commentResponse = CommentResponse.builder();

        if ( comment != null ) {
            commentResponse.id( comment.getId() );
            commentResponse.content( comment.getContent() );
            commentResponse.createdDateTime( comment.getCreatedDateTime() );
            commentResponse.rootCommentId( commentRootCommentId( comment ) );
            commentResponse.replyToUser( toAuthorResponse( comment.getReplyToUser() ) );
            commentResponse.author( toAuthorResponse( comment.getAuthor() ) );
            commentResponse.deleted( comment.isDeleted() );
            commentResponse.reactionCount( comment.getReactionCount() );
        }
        commentResponse.stats( stats );
        commentResponse.userState( userState );
        commentResponse.permissions( permissions );
        List<AttachmentResponse> list = attachments;
        if ( list != null ) {
            commentResponse.attachments( new ArrayList<AttachmentResponse>( list ) );
        }

        return commentResponse.build();
    }

    @Override
    public PostAuthorResponse toAuthorResponse(User user) {
        if ( user == null ) {
            return null;
        }

        PostAuthorResponse.PostAuthorResponseBuilder postAuthorResponse = PostAuthorResponse.builder();

        if ( user.getId() != null ) {
            postAuthorResponse.id( user.getId().toString() );
        }
        postAuthorResponse.fullName( user.getFullName() );
        postAuthorResponse.avatarUrl( user.getAvatarUrl() );

        return postAuthorResponse.build();
    }

    private UUID commentRootCommentId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        Comment rootComment = comment.getRootComment();
        if ( rootComment == null ) {
            return null;
        }
        UUID id = rootComment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
