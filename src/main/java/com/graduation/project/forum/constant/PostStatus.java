package com.graduation.project.forum.constant;

public enum PostStatus {
  PENDING {
    @Override
    public boolean canTransitionTo(PostStatus target) {
      if (target == null)
        return false;
      return target == APPROVED || target == REJECTED;
    }
  },
  APPROVED {
    @Override
    public boolean canTransitionTo(PostStatus target) {
      if (target == null)
        return false;
      return target == ARCHIVED;
    }
  },
  REJECTED {
    @Override
    public boolean canTransitionTo(PostStatus target) {
      if (target == null)
        return false;
      return target == PENDING;
    }
  },
  ARCHIVED {
    @Override
    public boolean canTransitionTo(PostStatus target) {
      return false;
    }
  };

  public abstract boolean canTransitionTo(PostStatus target);
}
