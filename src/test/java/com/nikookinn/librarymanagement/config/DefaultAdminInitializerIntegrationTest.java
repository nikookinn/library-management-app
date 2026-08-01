package com.nikookinn.librarymanagement.config;

import com.nikookinn.librarymanagement.entity.Role;
import com.nikookinn.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:default-admin-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DefaultAdminInitializerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void createsOneDefaultAdminWhenNoAdminExists() {
        long adminCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .count();

        assertThat(adminCount).isEqualTo(1);
        assertThat(userRepository.findByEmail("admin@test.local")).isPresent();
    }
}
