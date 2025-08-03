

import grails.converters.JSON
import org.bdch.services.AuthService
import util.AbstractController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

class AuthController extends AbstractController {

   Logger logger = LoggerFactory.getLogger(AuthController.class)


   AuthService authService

   @Transactional
   def register() {
      logger.info("Trying to register a new user")
      def jsonText = request.reader.text
      def json = JSON.parse(jsonText)

      def username = json?.username?.toString() // THis fucking shit is not even Typesafe HOLY SMH
      def password = json?.password?.toString()

      def result = authService.registerUser(username, password)

      render result as JSON
   }

   def renderRegisterPage() {
      renderViewFromStatic("registerPage")
   }
}
