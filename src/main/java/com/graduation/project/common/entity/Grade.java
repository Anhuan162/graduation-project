package com.graduation.project.common.entity;

public enum Grade {
  A_PLUS(4.0),
  A(3.7),
  B_PLUS(3.5),
  B(3.0),
  C_PLUS(2.5),
  C(2.0),
  D_PLUS(1.5),
  D(1.0),
  F(0.0);

  private final double score;

  Grade(double score) {
    this.score = score;
  }

  public double getScore() {
    return score;
  }

  public static double toScore(String letter) {
    try {
      return Grade.valueOf(letter.toUpperCase()).getScore();
    } catch (IllegalArgumentException e) {
      return 0.0;
    }
  }

  public static String fromScore(double score) {
    Grade closest = F;
    double minDiff = Double.MAX_VALUE;
    for (Grade grade : Grade.values()) {
      double gradeDiff = grade.getScore() - score;
      if (gradeDiff < minDiff) {
        minDiff = gradeDiff;
        closest = grade;
      }
    }
    return closest.name();
  }
}
