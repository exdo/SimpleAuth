package xyz.idaoteng.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import xyz.idaoteng.auth.subject.UserInfo;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "D@D~IahCp!i";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final String ISSUER = "JwtTokenAuth";

    private static final ObjectMapper objectMapper = JacksonUtil.objectMapper();

    @SneakyThrows
    public static String rememberUserInfo(UserInfo userInfo, long rememberMeValidTime) {
        String jsonUserInfo = objectMapper.writeValueAsString(userInfo);
        String encryptedUserInfo = AESUtil.encrypt(jsonUserInfo);
        long expiration = System.currentTimeMillis() + rememberMeValidTime;

        return JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(expiration))
                .withSubject(encryptedUserInfo)
                .sign(ALGORITHM);
    }

    @SneakyThrows
    public static UserInfo fetchUserInfo(String token) {
        String encryptedUserInfo;
        JWTVerifier verifier = JWT.require(ALGORITHM).withIssuer(ISSUER).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            encryptedUserInfo = jwt.getSubject();
        } catch (JWTVerificationException exception){
            return null;
        }

        String jsonUserInfo = AESUtil.decrypt(encryptedUserInfo);

        return objectMapper.readValue(jsonUserInfo, UserInfo.class);
    }
}
