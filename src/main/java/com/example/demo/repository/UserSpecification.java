package com.example.demo.repository;

import com.example.demo.entity.EmailData;
import com.example.demo.entity.PhoneData;
import com.example.demo.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class UserSpecification {

    public static Specification<User> hasNameLike(String name) {
        return (root, query, cb) -> cb.like(root.get("name"), name + "%");
    }

    public static Specification<User> hasDateOfBirthAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThan(root.get("dateOfBirth"), date);
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<User, EmailData> join = root.join("emails");
            return cb.equal(join.get("email"), email);
        };
    }

    public static Specification<User> hasPhone(String phone) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<User, PhoneData> join = root.join("phones");
            return cb.equal(join.get("phone"), phone);
        };
    }
}
