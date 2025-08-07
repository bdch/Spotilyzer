//package services
//
//import org.grails.web.json.JSONElement
//import java.net.URL
//
//class AuthServiceKotlin {
//
//    fun getSpotifyProfile(accessToken: String): JSONElement? {
//
//        val conn = URL("https://api.spotify.com/v1/me").openConnection()
//        conn.setRequestProperty("Authorization", "Bearer $accessToken")
//        val response = conn.inputStream.bufferedReader().readText()
//
//        return grails.converters.JSON.parse(response)
//    }
//
//}
