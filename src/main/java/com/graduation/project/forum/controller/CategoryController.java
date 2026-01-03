package com.graduation.project.forum.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.forum.dto.CategoryRequest;
import com.graduation.project.forum.dto.CategoryResponse;
import com.graduation.project.forum.service.CategoryService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest req) {
    return ApiResponse.ok(categoryService.createCategory(req));
  }

  @GetMapping("/{categoryId}")
  public ApiResponse<CategoryResponse> getOne(@PathVariable UUID categoryId) {
    return ApiResponse.ok(categoryService.getOne(categoryId));
  }

  @PutMapping("/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<CategoryResponse> update(
      @PathVariable UUID categoryId, @Valid @RequestBody CategoryRequest req) {
    return ApiResponse.ok(categoryService.update(categoryId, req));
  }

  @DeleteMapping("/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<String> delete(@PathVariable UUID categoryId) {
    categoryService.delete(categoryId);
    return ApiResponse.ok("Deleted successfully");
  }

  @GetMapping
  public ApiResponse<List<CategoryResponse>> getAll() {
    return ApiResponse.ok(categoryService.getAll());
  }
}
