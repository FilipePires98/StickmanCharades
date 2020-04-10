package es_g54.services;

import es_g54.api.entities.UserData;
import es_g54.entities.DBRole;
import es_g54.entities.DBUser;
import es_g54.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository ur;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<String> registerUser(UserData validatedUserData) {
        StringBuilder duplicatedFields = new StringBuilder();
        if (ur.getUsernameCount(validatedUserData.getUsername()) > 0) {
            duplicatedFields.append("username");
        }

        if (ur.getEmailCount(validatedUserData.getEmail()) > 0) {
            if (duplicatedFields.length() > 0) {
                duplicatedFields.append(" and ");
            }
            duplicatedFields.append("email");
        }

        if (duplicatedFields.length() > 0) {
            return ResponseEntity.status(400)
                    .body(
                            String.format("Field(s) %s value(s) already in use", duplicatedFields.toString())
                    );
        }

        DBUser user = new DBUser(
                validatedUserData,
                passwordEncoder.encode(CharBuffer.wrap(validatedUserData.getPassword()))
        );

        List<DBRole> listUserGroup = ur.getRole();
        DBRole userRole;

        if (listUserGroup.isEmpty()) {
            userRole = new DBRole("ROLE_USER");
        } else {
            userRole = listUserGroup.get(0);
        }

        user.addRole(userRole);
        userRole.addUser(user);

        ur.save(user);

        Arrays.fill(validatedUserData.getPassword(), Character.MIN_VALUE);

        //ks.sendMessage();

        return ResponseEntity.ok("Registration successful");
    }
}
