package com.graduation.project.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis")
public class RedisStreamProperties {
  private Stream stream = new Stream();
  private Retry retry = new Retry();

  public Stream getStream() {
    return stream;
  }

  public Retry getRetry() {
    return retry;
  }

  public static class Stream {
    private String key;
    private String notificationGroup;
    private String activityGroup;
    private String dlqSuffix = "-dlq";

    // getters/setters
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getNotificationGroup() {
      return notificationGroup;
    }

    public void setNotificationGroup(String notificationGroup) {
      this.notificationGroup = notificationGroup;
    }

    public String getActivityGroup() {
      return activityGroup;
    }

    public void setActivityGroup(String activityGroup) {
      this.activityGroup = activityGroup;
    }

    public String getDlqSuffix() {
      return dlqSuffix;
    }

    public void setDlqSuffix(String dlqSuffix) {
      this.dlqSuffix = dlqSuffix;
    }
  }

  public static class Retry {
    private int maxAttempts = 3;
    private long pendingScanIntervalMs = 5000;
    private long baseBackoffMs = 1000;
    private long processedTtlSeconds = 86400;

    // getters/setters
    public int getMaxAttempts() {
      return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
    }

    public long getPendingScanIntervalMs() {
      return pendingScanIntervalMs;
    }

    public void setPendingScanIntervalMs(long pendingScanIntervalMs) {
      this.pendingScanIntervalMs = pendingScanIntervalMs;
    }

    public long getBaseBackoffMs() {
      return baseBackoffMs;
    }

    public void setBaseBackoffMs(long baseBackoffMs) {
      this.baseBackoffMs = baseBackoffMs;
    }

    public long getProcessedTtlSeconds() {
      return processedTtlSeconds;
    }

    public void setProcessedTtlSeconds(long processedTtlSeconds) {
      this.processedTtlSeconds = processedTtlSeconds;
    }
  }
}
