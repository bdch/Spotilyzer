package org.bdch.services

import org.bdch.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import grails.gorm.transactions.Transactional
import org.bdch.User

@Transactional
class AuthService {

   Logger logger = LoggerFactory.getLogger(AuthService.class)
   BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()


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
      return [status: "success", message: "User '${user.username}' registered successfully"]
   }

   Map<String, Object> login(String username, String password) {
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

      // Session
      String sessionKey = UUID.randomUUID().toString()

//      Session.where {
//         owner == user
//      }.deleteAll()

      Session session = new Session(sessionKey: sessionKey, owner: user, cts: System.currentTimeMillis().toLong())
      session.save(flush: true)

      logger.info("User '${user.username}' logged in successfully  with sessionKey '${sessionKey}'")
      return [status: "success", message: "Login successful", user: user, sessionKey: sessionKey]
   }


}
