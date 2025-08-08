package org.bdch.services

import grails.gorm.transactions.Transactional
import org.bdch.Session
import org.bdch.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Transactional
class AuthService {

   Logger logger = LoggerFactory.getLogger(AuthService.class)
   BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
   private static final long MAX_SESSION_TIME = 3600000 // 1 hour in milliseconds

   Map<String, Object> registerUser(String username, String password) {
      if (username.isEmpty() || password.isEmpty()) {
         logger.error("Username is empty or password is empty!")
         return [status: "error", message: "Username and password are required"]
      }
      if (User.findByUsername(username) != null) {
         logger.error("User already exists with username: $username")
         return [status: "error", message: "Username already exists with username: $username"]
      }
      logger.info("Trying to register user with username: $username")
      User user = new User(username: username, passwordHash: passwordEncoder.encode(password))
      logger.info("User created with username: $user.username")
      if (!user.save(flush: true)) {
         logger.error("Failed to register user: ${user.errors}")
         return [status: "error", message: "Failed to register user: ${user.errors}"]
      }
      logger.info("User '${user.username}' registered successfully")
      return [status: "success", message: "org.bdch.User '${user.username}' registered successfully"]
   }

   Map<String, Object> login(String username, String password, HttpServletResponse response) {
      if (username.isEmpty() || password.isEmpty()) {
         logger.warn("Login attempt with empty username or password")
         return [status: "error", message: "Username and password are required"]
      }

      User user = User.findByUsername(username)

      if (user == null) {
         logger.warn("Login failed: user not found for username $username")
         return [status: "error", message: "Invalid username or password"]
      }

      if (!passwordEncoder.matches(password, user.passwordHash)) {
         logger.warn("Login failed: incorrect password for user $username")
         return [status: "error", message: "Invalid username or password"]
      }

      Session session = new Session(
         sessionKey: UUID.randomUUID().toString(),
         user: user,
         creation_timestamp: System.currentTimeMillis()
      )

      boolean saved = session.save(flush: true)
      if (!saved) {
         logger.error("org.bdch.Session couln'd be saved due to: ${session.errors}")
      }

      Cookie sessionCookie  = new Cookie('sessionKey', session.sessionKey)
      sessionCookie.httpOnly = true  // Prevent XSS attacks
      sessionCookie.secure = false   // Set to true in production with HTTPS
      sessionCookie.maxAge = 86400   // 24 hours
      sessionCookie.path = '/'
      response.addCookie(sessionCookie)

      logger.info("org.bdch.User '${user.username}' logged in successfully")
      return [status : "success",
              sessionKey: session.sessionKey,
              message: "Login successful",
              user   : [user_id   : user.id,
                        username  : user.username,
              ]
      ]
   }

   def validateSession(String sessionKey) {
      if (!sessionKey) {
         return [valid: false, message: "No session key provided"]
      }
      Session session = Session.findBySessionKey(sessionKey)
      if (!session) {
         return [valid: false, message: "Invalid session key"]
      }
      long now = System.currentTimeMillis()
      long sessionAge = now - session.creation_timestamp

      if (sessionAge > MAX_SESSION_TIME) {
         session.delete(flush: true)
         return [valid: false, message: "Session expired"]
      }
      return [valid: true, user: session.user]
   }


   def deleteSession(String username) {
      def userToDelete = User.findByUsername(username)
      if (!userToDelete) {
         logger.warn("User not found for deletion: ${username}")
         return false
      }

      int deletedSessions = Session.executeUpdate(
         "delete from Session s where s.user = :user",
         [user: userToDelete]
      )
      logger.info("Deleted ${deletedSessions} sessions for user ${username}")
      userToDelete.delete(flush: true, failOnError: true)
      logger.info("User ${username} deleted successfully")
      return true
   }


}
