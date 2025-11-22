//package com.graduation.project.common.entity;
//
//import jakarta.persistence.*;
//import java.util.*;
//import lombok.*;
//
//@Entity
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "tags")
//public class Tag {
//  @Id @GeneratedValue private UUID id;
//
//  @Column(unique = true, nullable = false)
//  private String name;
//
//  @ManyToMany(mappedBy = "tags")
//  private Set<Post> posts = new HashSet<>();
//}
