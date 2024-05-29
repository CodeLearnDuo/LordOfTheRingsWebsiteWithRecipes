package blps.duo.project.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@Data
@Table("person")
@NoArgsConstructor
public class Person implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String email;
    @Column("username")
    private String name;
    private transient String password;
    @Column("person_race_id")
    private Long personRaceId;
    @Column("a_leader")
    private boolean aLeader;

    public Person(String email, String name, String password, Long personRaceId) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.personRaceId = personRaceId;
    }

    public Person(String email, String name, Long personRaceId, boolean aLeader) {
        this.email = email;
        this.name = name;
        this.personRaceId = personRaceId;
        this.aLeader = aLeader;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return aLeader ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_LEADER")) : Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
