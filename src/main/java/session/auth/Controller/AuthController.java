package session.auth.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import session.auth.Entity.User;
import session.auth.Services.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.signup(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        return userService.login(loginRequest.getEmail() , loginRequest.getPassword());
    }

    @PostMapping("/logout")
    public String logout(@RequestParam String email) {
        userService.logout(email);
        return "Logged out successfully";
    }

    @GetMapping("/profile")
    public User getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer " , "");
        return userService.getProfile(token);
    }
}
