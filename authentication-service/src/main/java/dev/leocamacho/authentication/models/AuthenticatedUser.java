package dev.leocamacho.authentication.models;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record AuthenticatedUser(

        UUID id,
        String username,
        String password,
        Collection<String> authorities
) {


    public Map<String, Object> toMap() {
        Map<String, Object> user = new HashMap<>();

        user.put("id", id.toString());
        user.put("email", username);
        user.put("roles", String.join(",", authorities));
        return user;

    }
}
