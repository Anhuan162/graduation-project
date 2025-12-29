package com.graduation.project.forum.mapper;

import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.CategoryType;
import com.graduation.project.forum.dto.CategoryRequest;
import com.graduation.project.forum.dto.CategoryResponse;
import com.graduation.project.forum.entity.Category;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toCategory(CategoryRequest request, User user) {
        if ( request == null && user == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        if ( request != null ) {
            category.name( request.getName() );
            category.description( request.getDescription() );
            if ( request.getCategoryType() != null ) {
                category.categoryType( Enum.valueOf( CategoryType.class, request.getCategoryType() ) );
            }
        }
        category.creator( user );
        category.createdAt( java.time.Instant.now() );

        return category.build();
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        UUID id = categoryCreatorId( category );
        if ( id != null ) {
            categoryResponse.createdById( id.toString() );
        }
        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.description( category.getDescription() );
        if ( category.getCategoryType() != null ) {
            categoryResponse.categoryType( category.getCategoryType().name() );
        }
        categoryResponse.createdAt( category.getCreatedAt() );

        return categoryResponse.build();
    }

    private UUID categoryCreatorId(Category category) {
        if ( category == null ) {
            return null;
        }
        User creator = category.getCreator();
        if ( creator == null ) {
            return null;
        }
        UUID id = creator.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
