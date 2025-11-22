package com.graduation.project.user.controller;

import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.user.dto.CategoryRequest;
import com.graduation.project.user.dto.CategoryResponse;
import com.graduation.project.user.service.CategoryService;
import java.util.List;
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
  public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest categoryRequest) {
    return ApiResponse.<CategoryResponse>builder()
        .result(categoryService.createCategory(categoryRequest))
        .build();
  }

  @GetMapping("/{categoryId}")
  public ApiResponse<CategoryResponse> getOne(@PathVariable String categoryId) {
    return ApiResponse.<CategoryResponse>builder()
        .result(categoryService.getOne(categoryId))
        .build();
  }

  @PutMapping("/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<CategoryResponse> update(
      @PathVariable String categoryId, @RequestBody CategoryRequest request) {
    return ApiResponse.<CategoryResponse>builder()
        .result(categoryService.update(categoryId, request))
        .build();
  }

  @DeleteMapping("/{categoryId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<String> delete(@PathVariable String categoryId) {
    categoryService.delete(categoryId);
    return ApiResponse.<String>builder().result("Deleted successfully").build();
  }

  @GetMapping
  public ApiResponse<List<CategoryResponse>> getAll() {
    return ApiResponse.<List<CategoryResponse>>builder().result(categoryService.getAll()).build();
  }
}
