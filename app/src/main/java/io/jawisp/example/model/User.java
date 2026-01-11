package io.jawisp.example.model;

import io.jawisp.core.annotation.Entity;

@Entity
public record User(String id, String name, int age) {
}

