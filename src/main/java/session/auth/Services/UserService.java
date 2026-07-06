package session.auth.Services;

import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.patterns.IToken;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import session.auth.Entity.User;
import session.auth.JWT.JwtUtil;
import session.auth.Repository.UserRepository;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String , Object> redisTemplate;

    //SignUp
    public User signup(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    //Login - varification
    public String login(String email , String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        if(!matches) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(email);

        String sessionKey = "session:" + email;
        redisTemplate.opsForValue().set(sessionKey, token, 1 , TimeUnit.HOURS);

        return token;
    }

    public void logout(String email) {
        String sessionKye = "session:" + email;
        redisTemplate.delete(sessionKye);
    }

    public User getProfile(String token) {
        if(!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);

        String sessionKey = "session:" + email;
        Object session = redisTemplate.opsForValue().get(sessionKey);

        if(session == null) {
            throw new RuntimeException("Session expired or logged out. Please login again");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
