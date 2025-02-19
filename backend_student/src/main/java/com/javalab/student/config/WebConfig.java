package com.javalab.student.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig, 환경설정 파일
 * - @Configuration : 이 클래스가 Spring의 설정 파일임을 명시, 여기에는 하나 이상의 @Bean이 있음.
 *   프로젝트가 구동될 때 이 클래스를 읽어들여 Bean으로 등록
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties 파일에 설정된 값을 가져옵니다.
    @Value("${uploadPath}")
    String uploadPath;  // file:///c:/shop/

    /**
     * Cross Origin Resource Sharing (CORS) 설정
     * - 프론트엔드에서 백엔드로 요청을 보낼 때  CORS를 허용할 URL 패턴을 설정
     * - addMapping : CORS를 적용할 URL 패턴, 모든 URL에 대해 적용하려면 /**로 설정
     * - allowedOrigins : 허용할 오리진, 여기서는 3000 포트로 들어오는 요청만 허용
     * - allowedMethods : 허용할 HTTP 메서드
     * - allowedHeaders : 허용할 HTTP 헤더
     * - allowCredentials : 쿠키를 주고 받을 수 있게 설정
     * - Nginx를 사용하기 위한 CORS 설정 추가
     *   http://localhost 추가
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://43.200.140.40")  // 실제 도메인
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
