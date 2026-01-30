package io.jawisp.example.service;

import io.jawisp.core.annotation.Service;
import io.jawisp.example.model.User;

@Service
public class HomeService {
    
    public String getHome() {
        return "This is plain response";
    }

    public String getPage() {
        return "<h1>Home page</h1>";
    }

    public User getUser(String id, int age) {
        return new User(id, "John smith", age);
    }

    public User getUserByAge(int age) {
        return new User("1", "John smith", age);
    }
}
