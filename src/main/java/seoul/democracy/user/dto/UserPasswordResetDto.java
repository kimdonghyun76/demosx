package seoul.democracy.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class UserPasswordResetDto {

    @NotBlank
    private String token;

    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 20)
    private String password;
}
