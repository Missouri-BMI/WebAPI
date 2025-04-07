package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.Key;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.util.ExpiringMultimap;

/**
 *
 * @author gennadiy.anisimov
 */
public class TokenManager {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final SecretKey key = Jwts.SIG.HS256.key().build();

  public static String createJsonWebToken(String subject, String sessionId, Date expiration) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(Constants.SESSION_ID, sessionId);
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setExpiration(expiration)
            .signWith(key)
            .compact();
  }


  public static String getSubject(String jwt) throws JwtException {
    return getBody(jwt).getSubject();
  }

  public static Claims getBody(String jwt) throws JwtException {

    return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwt)
            .getPayload();
  }

  public static Boolean invalidate(String jwt) {
    if (jwt == null)
      return false;

    String subject;
    try {
      subject = getSubject(jwt);
    }
    catch(JwtException e) {
      return false;
    }
    return true;
  }

  public static String extractToken(ServletRequest request) {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);

    String header =  httpRequest.getHeader(AUTHORIZATION_HEADER);
    if (header == null || header.isEmpty())
      return null;

    if (!header.toLowerCase(Locale.ENGLISH).startsWith("bearer"))
      return null;

    String[] headerParts = header.split(" ");
    if (headerParts.length != 2)
      return null;

    String jwt = headerParts[1];
    return jwt;
  }
}
