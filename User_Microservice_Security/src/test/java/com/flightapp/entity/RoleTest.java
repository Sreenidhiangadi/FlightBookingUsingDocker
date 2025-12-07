package com.flightapp.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void allEnumValuesShouldBeAccessible() {
        Role user = Role.USER;
        Role admin = Role.ADMIN;

        assertEquals("USER", user.name());
        assertEquals("ADMIN", admin.name());
        assertEquals(2, Role.values().length);
        assertArrayEquals(new Role[]{Role.USER, Role.ADMIN}, Role.values());
    }
}
