package com.mountblue.blogapplication.repository;

import com.mountblue.blogapplication.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users,Long> {
    Users findByEmail(String email);

}
