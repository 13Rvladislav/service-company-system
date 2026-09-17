package ru.servicecompany.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.servicecompany.auth.entity.Role;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.repository.RoleRepository;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRole(RoleName.CLIENT, "Житель");
        createRole(RoleName.DISPATCHER, "Диспетчер");
        createRole(RoleName.ENGINEER, "Инженер");
        createRole(RoleName.ADMIN, "Администратор");

    }

    private void createRole(RoleName name, String description) {

        if (roleRepository.findByName(name).isPresent()) {
            return;
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);

        roleRepository.save(role);

    }

}