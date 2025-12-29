package com.graduation.project.forum.mapper;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.dto.TopicRequest;
import com.graduation.project.forum.dto.TopicResponse;
import com.graduation.project.forum.entity.Category;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class TopicMapperImpl implements TopicMapper {

    @Override
    public Topic toTopic(TopicRequest request, Category category, User user) {
        if ( request == null && category == null && user == null ) {
            return null;
        }

        Topic.TopicBuilder topic = Topic.builder();

        if ( request != null ) {
            topic.title( request.getTitle() );
            topic.content( request.getContent() );
            if ( request.getTopicVisibility() != null ) {
                topic.topicVisibility( Enum.valueOf( TopicVisibility.class, request.getTopicVisibility() ) );
            }
        }
        if ( category != null ) {
            topic.category( category );
            topic.createdAt( category.getCreatedAt() );
        }
        if ( user != null ) {
            topic.createdBy( user );
            Set<TopicMember> set = user.getTopicMembers();
            if ( set != null ) {
                topic.topicMembers( new LinkedHashSet<TopicMember>( set ) );
            }
        }

        return topic.build();
    }

    @Override
    public TopicResponse toTopicResponse(Topic topic) {
        if ( topic == null ) {
            return null;
        }

        TopicResponse.TopicResponseBuilder topicResponse = TopicResponse.builder();

        topicResponse.categoryName( topicCategoryName( topic ) );
        topicResponse.createdBy( topicCreatedByFullName( topic ) );
        if ( topic.getId() != null ) {
            topicResponse.id( topic.getId().toString() );
        }
        topicResponse.title( topic.getTitle() );
        topicResponse.content( topic.getContent() );
        topicResponse.createdAt( topic.getCreatedAt() );
        topicResponse.lastModifiedAt( topic.getLastModifiedAt() );
        topicResponse.topicVisibility( topic.getTopicVisibility() );

        return topicResponse.build();
    }

    private String topicCategoryName(Topic topic) {
        if ( topic == null ) {
            return null;
        }
        Category category = topic.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String topicCreatedByFullName(Topic topic) {
        if ( topic == null ) {
            return null;
        }
        User createdBy = topic.getCreatedBy();
        if ( createdBy == null ) {
            return null;
        }
        String fullName = createdBy.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }
}
