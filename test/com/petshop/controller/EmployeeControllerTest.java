package com.petshop.controller;

import com.petshop.dao.EmployeeRepository;
import com.petshop.model.Employee;
import com.petshop.model.Role;
import com.petshop.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeControllerTest {
    @Test
    void hashesInitialPasswordBeforePassingItToRepository() {
        CapturingRepository repository = new CapturingRepository();
        EmployeeController controller = new EmployeeController(repository);

        controller.add(employee(), "HappyTails123");

        assertTrue(repository.passwordHash.startsWith("$2"));
        assertTrue(PasswordUtil.verify("HappyTails123", repository.passwordHash));
        assertFalse(repository.passwordHash.contains("HappyTails123"));
        assertEquals("NV-20261011-000001", repository.createdId);
    }

    @Test
    void rejectsWeakPasswordBeforeCallingRepository() {
        CapturingRepository repository = new CapturingRepository();
        EmployeeController controller = new EmployeeController(repository);

        assertThrows(IllegalArgumentException.class,
                () -> controller.add(employee(), "abcdefgh"));
        assertEquals(0, repository.addCalls);
    }

    private Employee employee() {
        return new Employee("", "Nguyễn An", "0901234567", Role.BAN_HANG,
                LocalDate.of(1995, 3, 12), true, true, "nguyenan", true);
    }

    private static final class CapturingRepository implements EmployeeRepository {
        private String passwordHash;
        private String createdId;
        private int addCalls;

        @Override
        public List<Employee> findAll() {
            return List.of();
        }

        @Override
        public List<Employee> search(String keyword) {
            return List.of();
        }

        @Override
        public String add(Employee employee, String hash) {
            addCalls++;
            passwordHash = hash;
            createdId = "NV-20261011-000001";
            return createdId;
        }

        @Override
        public void update(Employee employee, String passwordHash) {
        }

        @Override
        public void deactivate(String id) {
        }
    }
}
