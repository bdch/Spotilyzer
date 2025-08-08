

import grails.converters.JSON
import org.bdch.services.AuthService
import util.AbstractController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

class LoginController extends AbstractController {

   Logger logger = LoggerFactory.getLogger(LoginController.class)

   AuthService authService

   @Transactional
   def login() {
      logger.info("Trying to login")
      def jsonText = request.reader.text
      def json = JSON.parse(jsonText)
      def username = json?.username?.toString()
      def password = json?.password?.toString()

      def result = authService.login(username, password, response)
      render result as JSON
   }

   def renderLoginPage() {
      renderViewFromStatic("loginPage")
   }

}
