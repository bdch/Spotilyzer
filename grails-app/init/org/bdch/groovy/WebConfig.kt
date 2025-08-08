package org.bdch.groovy

import util.SessionInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val sessionInterceptor: SessionInterceptor
): WebMvcConfigurer {

    companion object {}

    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(sessionInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/", "/loginPage/**", "/auth/**", "/registerPage")
    }


}
