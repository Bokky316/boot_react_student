package com.javalab.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Bean 설정
 * - RestTemplate Bean을 생성하여 Bean으로 등록한다.
 * - RestTemplate은 HTTP 통신을 위한 객체로, HTTP 요청을 보내고 응답을 받을 수 있다.
 * - PaymentTest에서 PortOne API를 호출하기 위해 사용한다.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
