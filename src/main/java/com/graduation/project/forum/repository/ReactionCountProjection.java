package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.ReactionType;

public interface ReactionCountProjection {
  ReactionType getType();

  Long getCount();
}
