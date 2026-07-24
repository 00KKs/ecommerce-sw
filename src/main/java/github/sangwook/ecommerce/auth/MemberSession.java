package github.sangwook.ecommerce.auth;

import lombok.Getter;

@Getter
public class MemberSession {

    private final Long id;
    private final String email;
    private final String name;

    public MemberSession(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
}
