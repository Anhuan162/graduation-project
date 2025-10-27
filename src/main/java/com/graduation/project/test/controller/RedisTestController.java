package com.graduation.project.test.controller;

import com.graduation.project.test.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisTestController {
  private final RedisTestService redisTestService;

  @PostMapping("/set")
  public String set(@RequestParam String key, @RequestParam String value) {
    redisTestService.save(key, value);
    return "Saved!";
  }

  @GetMapping("/get")
  public String get(@RequestParam String key) {
    return redisTestService.get(key);
  }
}
