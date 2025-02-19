package com.javalab.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화를 위한 설정 클래스
 * - JPA Auditing을 사용하기 위해 @EnableJpaAuditing 어노테이션을 추가함
 * - AuditorAware 빈을 등록하여 현재 사용자를 가져올 수 있도록 함
 * - AuditorAwareImpl 클래스를 빈으로 등록하여 사용자 정보를 가져옴
 * - 사용자 정보를 가져오는 방법은 SecurityContext를 사용함
 *   SecurityContext에 저장된 사용자 정보를 가져와서 사용자 아이디를 반환
 *   이렇게 가져온 정보를 통해 누가 생성했는지, 수정했는지 알 수 있음
 */
@Configuration
@EnableJpaAuditing  // JPA Auditing 활성화
public class AuditConfig {

    /**
     * 현재 로그인한 사용자의 아이디를 가져옴, 이걸 통해 누가 생성했는지, 수정했는지 알 수 있음. 즉 등록자, 수정자로 넣어줌
     * 위 기능을 갖고 있는 AuditorAwareImpl 객체를 빈으로 등록
     * @return
     */
    @Bean
    public AuditorAware<String> auditorProvider(){
        return new AuditorAwareImpl();
    }
}
