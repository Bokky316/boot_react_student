# 스프링 부트와 리액트 프로젝트를 AWS에 배포하기 전에, 로컬 PC에서 백엔드 배포를 위한 JAR 파일을 생성하고, 프론트엔드 실행을 위한 Nginx 서버를 설정하는 방법
1. 이 버전은 모든 것을 AWS 클라우드 배포에 초점을 맞춰서 수정되었다.
2. AWS에 앱을 배포하고 탄력적 ip를 할당받아서 프론트엔드와 백엔드를 연결해야 한다.
- 백엔드는 8080 포트로 실행되고, 프론트엔드는 80 포트로 실행된다.
3. 기존에 리액트에서 localhost로 사용하던 주소를 탄력적ip인 43.200.140.40:8080으로 변경해야 한다.
- export const SERVER_URL = "http://43.200.140.40:8080/";
4. application.properties에서 다음과 같이 하던 내용은 수정할 필요없음
- spring.data.redis.host=localhost
- 
## 프론트 엔드 배포하기

### 1. Spring Boot에서 정적 파일 형태로 제공

 1) React 애플리케이션을 빌드한 후, Spring Boot의 src/main/resources/static 폴더에 빌드된 정적 파일(dist 또는
   build 폴더)을 배치하면 Spring Boot 자체에서 정적 파일을 서빙할 수 있습니다.
 2) npm run build 후 생성된 build 폴더를 src/main/resources/static에 복사
 3) Spring Boot 실행 시 정적 파일 형태로 제공된다.
   이 방법의 단점: 프론트엔드를 수정할 때마다 생성된 결과를 스프링 부트에 복사해야 하므로 번거롭다. 

### 2. Nginx 를 통한 프록시 서버로 배포

 1) 프록시 서버로 Nginx를 사용하여 프론트엔드(React)와 백엔드(Spring Boot) 간의 요청을 중계하는 방식으로 배포한다.
 2) 사용자가 웹사이트에 접근하면 Nginx가 React의 정적 파일을 제공 (예: index.html, CSS, JS 등)
 3) 사용자가 API 요청을 보내면 Nginx가 해당 요청을 Spring Boot로 전달(프록시 역할)
 4) Spring Boot에서 처리한 결과를 다시 Nginx를 통해 클라이언트(React)로 전달
 5) (보충)사용자가 웹브라우저에서 보게 되는 화면(HTML, CSS, JS)은 Nginx가 제공하는 React 정적 파일이고
    이후 API 요청이나 WebSocket 연결이 발생하면 Nginx가 이를 스프링 부트(8080 포트)로 프록시하여 전달합니다.

## 3. Nginx를 사용하면 좋은 점
    
 1) Nginx는 정적 파일을 빠르게 제공할 수 있어 웹사이트의 로딩 속도를 향상시킬 수 있다.
 2) Nginx는 프록시 서버로 사용할 수 있어 백엔드 서버의 부하를 줄일 수 있다.
 3) Nginx는 로드 밸런싱, SSL 인증서 적용 등 다양한 기능을 제공하여 웹사이트의 안정성을 높일 수 있다.

## 4. Nginx 설치 및 설정
 1) Windows용 Nginx 다운로드 및 설치
  - Nginx 다운로드 링크 : http://nginx.org/en/download.html (윈도우즈용 Stable version 다운로드)
  - 압축을 해제하고 폴더를 c:javaworks/ 디렉토리에 복사한다.(C:\javaworks\nginx-1.26.3)
 2) Nginx 명령어 
  - Nginx 실행 : 해당 폴더에서 nginx.exe를 실행하면 Nginx가 실행된다.([Ctrl + Alt + Delete]로 작업관리자에서 확인 가능)
  - Nginx 서버 가동 유무 확인
    C:\javaworks\nginx-1.26.3>tasklist | findstr nginx (2개 프로세스가 실행중이면 정상)
    nginx.exe                     6660 Console                    2      8,064 K
    nginx.exe                    19108 Console                    2      8,384 K
  - netstat -ano | findstr :80 (80번 포트 사용중인 프로세스 확인)
  - 웹브라우저에서 localhost로 접속하여 Nginx가 실행되는지 확인한다
  - Nginx 설정을 변경한 경우(nginx.conf 수정 등) 재시작 : nginx -s reload
  - Nginx 재실행() : start nginx
  - Nginx 중지 : nginx -s stop

  - 모든 Nginx 프로세스 종료 : taskkill /f /im nginx.exe 또는 taskkill /IM nginx.exe /F
  - Nginx 정상작동 여부 체크
    C:\javaworks\nginx-1.26.3>nginx -t
    nginx: the configuration file C:\javaworks\nginx-1.26.3/conf/nginx.conf syntax is ok
    nginx: configuration file C:\javaworks\nginx-1.26.3/conf/nginx.conf test is successful
2) Nginx 실행
  
    .
3) Nginx 설정 파일 수정[이 파일은 윈도우즈 메모장으로 빨리 수정하고 저장하고 닫을것]
    - Nginx 설치 폴더의 conf/nginx.conf 파일을 수정한다. Nginx 설정 파일에서 server 블록을 추가한다.
      // 📌 React 정적 파일 제공 (dist 폴더)
      location / {
      root   C:\javaworks\nginx-1.26.3\html;
      index  index.html;
      try_files $uri /index.html;
      }

      // 📌 Spring Boot API 프록시 설정 (localhost:8080)
      location /api/ {
      proxy_pass http://localhost:8080/;
      proxy_set_header Host $host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
      }

로컬에서 BOOTJAR 파일 생성
1. 백엔드 포트를 8080으로 변경, 프론트엔드 포트를 3000으로 변경
2. 프로젝트 루트에서 다음 명령어를 실행하여 BOOTJAR 파일을 생성한다.
   - gradlew build -x test
   - build/libs 디렉토리에 jar 파일이 생성된다.
   - java -jar 파일명.jar로 실행한다.
   - java -jar 파일명.jar --server.port=8080으로 포트번호를 지정할 수 있다.(선택)
3. 프론트 엔드 리액트를 Nginx로 로컬에서 구동하기
 
 4) 백엔드의 CORS 설정 추가 WebConfig.java + SecurityConfig에 /api/public/** 추가  + WebSocket 설정
  - .allowedOrigins("http://localhost", "http://localhost:3000")
  - .requestMatchers("/api/public/**").permitAll()
  - .setAllowedOrigins("http://localhost", "http://localhost:3000") // 허용된 출처 설정

 5) nginx.conf 파일의 역할
    ✅ Nginx는 nginx.conf 설정 파일을 기반으로 동작합니다.

    🔹 역할 1️⃣: 정적 파일 제공

    location / 설정을 통해 React 빌드 파일을 제공
    브라우저가 http://localhost/로 접속하면 index.html을 제공
    🔹 역할 2️⃣: API 요청을 Spring Boot로 프록시 전송

    location /api/ 설정을 통해 /api/로 들어오는 요청을 **Spring Boot(8080)**으로 전달
    React에서 fetch("/api/...")를 호출하면 자동으로 백엔드로 연결됨

    🔹 역할 3️⃣: 서버 설정 및 보안 관리

    listen 80; 설정으로 Nginx가 포트 80에서 HTTP 요청을 받음
    proxy_set_header 설정으로 API 요청 시 추가 헤더 정보 전달

 6) 리액트 build 하기
 - 리액트 프로젝트 루트에서 다음 명령어를 실행한다.
 - npm run build
 - vite로 생성한 리액트 프로젝트를 빌드하면 dist 디렉토리가 생성된다. dist 안에 정적 파일이 생성된다.
 - Nginx 설정 파일을 수정하여 dist 디렉토리를 루트로 지정한다.
 - CMD 모드에서 다음과 같이 빌드된 파일을 Nginx의 html 디렉토리로 복사한다. 
 - xcopy /E /I /Y C:\javaworks\workspace\springbootreact\boot_react_student\frontend_student\dist C:\javaworks\nginx-1.26.3\html
   C:\javaworks\nginx-1.26.3>xcopy /E /I /Y C:\javaworks\workspace\springbootreact\boot_react_student\frontend_student\dist C:\javaworks\nginx-1.26.3\html
   C:\javaworks\workspace\springbootreact\boot_react_student\frontend_student\dist\index.html
   C:\javaworks\workspace\springbootreact\boot_react_student\frontend_student\dist\vite.svg
   C:\javaworks\workspace\springbootreact\boot_react_student\frontend_student\dist\assets\index-D1b6UEtX.css
   C:\javaworks\workspace\springbootreact\boot_react_student\frontend_student\dist\assets\index-DoeJg1FK.js
   4개 파일이 복사되었습니다.

 7) 여기까지 진행된 후의 폴더 구조
   C:\javaworks\nginx-1.26.3\
   ├── conf/nginx.conf  # Nginx 설정 파일
   ├── html/index.html  # React 메인 파일 (dist에서 복사됨)
   ├── html/assets/     # React 빌드된 JS, CSS 파일
   ├── logs/            # 로그 파일
   ├── nginx.exe        # 실행 파일

4. 애플리케이션 백엔드 + 프론트엔드 구동
 - Nginx를 실행하고 브라우저에서 localhost로 접속하여 정상적으로 페이지가 나오는지 확인한다.
5. 페이지 안열리고 500번 오류가 나면 다음에서 로그를 확인한다.
. C:\javaworks\nginx-1.26.3\logs\error.log

# 포트원(PortOne) 결제 중계 시스템
1. 결제 중계 시스템이란 : 여러 카드사, 은행, 결제 수단을 한 곳에서 통합하여 결제를 처리하는 시스템
   - 카드사, 은행, 다양한 결제 서비스 제공 업체들이 개별적으로 서비스를 제공할 수도 있지만
   사용자 입장에서는 불편하기 때문에 포트원과 같은 결제 중계 시스템을 통해 통합하여 사용자가 원하는 
   지급 결제 수단을 선택하여 결제를 처리할 수 있도록 하는 시스템을 말한다.
2. 지급 결제 수단
   - 카드사, 은행, 휴대폰 결제, 계좌이체, 가상계좌, 휴대폰 소액결제 등 다양한 결제 수단을 지원한다.
   - 요즘은 카카오페이, 네이버페이, 토스페이등 다양한 결제 수단이 있어서 결제 중계 시스템이 필요하다.
3. 포트원(PortOne) 결제 중계 시스템 API 사용을 위한 설정
   - 포트원(PortOne) 결제 중계 시스템 API 사용을 위해서는 포트원(PortOne) 홈페이지에서 API 키를 발급받아야 한다.
   - 포트원(PortOne) 홈페이지 : https://www.portone.io/
   - 포트원(PortOne) API 사용을 위한 가이드 문서 : https://docs.portone.io/
4. 포트원 사용을 위한 각종 키 발급받기
    - 포트원(PortOne) API 사용을 위한 상점 UID 발급(가맹점 식별을 위한 고유 식별자)
    - 포트원(PortOne) API 사용을 위한 API 키 발급(인증을 위한 API 키)
    - 포트원(PortOne) API 사용을 위한 API 시크릿 키 발급(인증을 위한 API 시크릿 키)
5. 애플리케이션에서 포트원 사용을 위한 설정
 1) build.gradle에 포트원 의존성 추가
    - implementation 'com.github.iamport:iamport-rest-client-java:0.2.23'
     repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' } // JitPack 저장소 추가
      }
      implementation 'com.github.iamport:iamport-rest-client-java:0.2.23'
 2) application.properties에 포트원(PortOne) API 키 설정
    # 상점 UID
    # portone.merchant-uid=imp66240214 (리액트 환경설정 파일일 .env에 설정되어 있음)
    # 포트원 결제 REST API URL
    portone.api-key=6738525558640373
    # 포트원 결제 API를 사용하기 위한 API 비밀키
    portone.api-secret=VG95in0lolTTHB13ZJKxS4iK2Iw4XY1YMWkq78jpplXAoYLi73hzwiqxxozOHsN12R8c6VbvfrT6HyRz-
 3) SecurityConfig.java 에 권한설정
  - .requestMatchers("/api/payments/**").hasAnyRole("USER", "ADMIN") // 결제 요청, 결제 검증 API는 USER, ADMIN만 접근 가능
 4) 리액트 환경 설정 파일인 .env에 상점ID 설정 - .env 파일은 반드시 프로젝트 루트에 위치해야 함.(package.json과 같은 위치)
  - VITE_PORTONE_MERCHANT_ID=imp66240214
  - .evn 설정 후에는 npm start를 다시 실행해야 함.
  - 이걸 사용할 때는 const merchantId = import.meta.env.VITE_PORTONE_MERCHANT_ID; 와 같이 사용
 5) 리액트 index.hmtml에 포트원 자바스크립트 SDK 추가
  - payment.jsx : 결제 요청 및 결제 취소 요청을 위한 포트원 자바스크립트 SDK 설정
       index.html에 포트원 자바스크립트 SDK 추가
  <script src="https://cdn.iamport.kr/v1/iamport.js"></script>

  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <script src="https://cdn.iamport.kr/v1/iamport.js"></script>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Vite + React</title>
  </head>

6. 포트원 환경설정 파일
 1) PortOneProperties.java : application.properties에 설정해놓은 포트원 정보 읽어오는 클래스 추가
 2) IamportConfig.java : 포트원 환경설정 파일 추가
7. 포트원을 통한 결제가 이뤄지는 과정
   사용자 결제 요청
   │
   ▼
   포트원 (카카오페이 연결)  ❖❖❖❖ (1차 승인,사용자는 이 시점에 카카오페이 QR코드로 결제 승인을 진행)
   │
   ▼
   결제 요청 결과 응답  ❖❖❖❖ (결제 승인 번호인 imp_uid 및 다양한 결제 정보를 응답)
   │
   ▼
   백엔드에 결제 정보 전송
   │
   ▼
   포트원 API에서 2차 검증  ❖❖❖❖ (1차 승인에서 받은 정보로 2차 승인을 진행, 이때 1차정보때 받은 imp_uid와 결제 금액이 일치해야 함. 이게 2차승인) 
   │
   ▼
   결제 정보 저장 및 주문 상태 변경  ❖❖❖❖ (주문 테이블의 주문상태를 "PAYMENT_COMPLETED" 업데이트, 결제 테이블에 결제 정보 저장)
   │
   ▼
   사용자 결제 완료 알림
8. 일반적인 쇼핑몰에서 주문과 결제가 이뤄지는 순서
 1) 장바구니, 상품상세보기에서 주문 버튼 클릭
 2) 주문 데이터 생성, 이때 주문 상태는 "ORDERED"
 3) 주문서 화면에서는 위에서 생성한 주문 데이터를 조회하고 결제 버튼 클릭
 4) 포트원을 통한 프론트 엔드에서 1차 결제 승인 진행
 5) 백엔드에서는 1차 결제 승인 정보를 받아서 2차 결제 승인 진행
 6) 2차 결제 승인이 완료되면 주문 상태를 "PAYMENT_COMPLETED"로 변경
 7) 결제 테이블에 결제 정보 저장
 8) 사용자에게 결제 완료 알림
 9) 주문 완료
 
9. 학생관리 시스템에서 테스트를 위한 사전 준비작업
 1) 엔티티 : Item, ItemImg, Orders, OrderItem, Payment
 2) 주문(Order) 테이블에 샘플 주문 데이터 1건 생성하고 그 pk 번호를 알고 있어야 함.
 3) 리액트 OrderDetail.jsx 에서 임의 상품 번호 설정 const itemId = 2;; // 위에서 임으로 생성한 주문번호를 설정(테스트용), 실제 디비에 있는 상품 ID로 변경 필요
 4) 리액트 Payment.jsx에서 merchantUid: 1, // 위에서 실제로 주문 번호로 등록한 주문번호로 변경 필요
 
10. 테스트(Test)
# 포트원 결제 API로 부터 액세스 토큰을 받아오는 cmd 모드에서 테스트
C:\Users\magic>curl -v https://api.iamport.kr/users/getToken -H "Content-Type: application/json" -d "{\"imp_key\":\"6738525558640373\", \"imp_secret\":\"VG95in0lolTTHB13ZJKxS4iK2Iw4XY1YMWkq78jpplXAoYLi73hzwiqxxozOHsN12R8c6VbvfrT6HyRz\"}"

# 포트원 액세스 토큰 발급 테스트 케이스
PortOneRestTemplateTest.java : RestTemplate을 사용하여 포트원 액세스 토큰 발급 테스트 케이스 추가

# 리액트에서 테스트시 다음 설정
- Payment.jsx에서 다음 설정
- 
// 백엔드에 결제 데이터 전송, 프론트엔드에서 1차 결제 승인후 백엔드에서 2차 결제 승인위해서
// 먼저 주문이 완료되었다고 가정하고 merchantUd를 주문번호인 1로 하드코딩함
const processPayment = async (rsp) => {
const paymentRequest = {
impUid: rsp.imp_uid, // 포트원에서 받은 결제 고유번호
merchantUid: 1, // name.merchant_uid, 임시로 오더 정보를 만들고 하드코딩함(1은 디비에 있어야 함)


# WebSocket을 활용한 채팅 서비스 구현
1. WebSocket을 활용한 실시간 채팅 서비스 구현[단일서버]

# 메시징 기능 완료 버전
## 포트번호
1. 백엔드 : 8080
2. 프론트엔드 : 3000

# Redis 서버 5.x 설치
1. 다운로드 링크 (GitHub): https://github.com/tporadowski/redis/releases
2. 설치후 [Ctrl + Alt + Delete] 해서 작업관리자를 열고 서비스 탭에서 redis-server.exe가 실행 중인지 확인
3. Redis 서버 정상 작동 확인
   - redis-server.exe 실행
   - redis-cli.exe ping
   - PONG 응답 확인

# Redis
1. Redis는 캐싱 서버로 사용할 수 있으며 인메모리 데이터 저장소로서 데이터베이스 조회 결과,
2. 세션 정보, API 응답 등을 캐싱하여 성능을 향상시키는 데 널리 활용됩니다.
3. TTL(만료 시간) 설정을 통해 자동 삭제가 가능하며, LRU(Least Recently Used) 등 캐시 정리 정책도 지원합니다. 🚀

# Redis 서버의 환경설정 파일인 C:\Program Files\Redis 폴더의 redis.windows-service.conf 파일을 열어서 다음과 같이 설정한다.
1. requirepass ezen12345 
2. application.properties 파일에 Redis 설정 추가
    # Redis 서버의 호스트 주소 (로컬에서 실행 중인 Redis 사용)
    spring.data.redis.host=localhost
    # Redis 서버의 포트 번호 (기본값: 6379)
    spring.data.redis.port=6379
    # Redis 서버 접속 시 필요한 비밀번호 (설정되지 않은 경우 빈 값)
    spring.data.redis.password=ezen12345 
3. RedisConfig.java 파일을 다음과 같이 수정하자.
 
   package com.javalab.student.config.redis;    
    @Configuration
    @EnableCaching // Spring의 캐싱 기능 활성화
    public class RedisConfig {
    
        /**
         * 🔹 application.yml에서 Redis 설정 값 가져오기
         */
        @Value("${spring.data.redis.host}")
        private String redisHost;
    
        @Value("${spring.data.redis.port}")
        private int redisPort;
    
        @Value("${spring.data.redis.password}") // ✅ 비밀번호 추가
        private String redisPassword;
    
        /**
         * 🔹 Redis 연결 팩토리 (비밀번호 설정 추가)
         */
        @Bean
        public LettuceConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(redisHost);
            config.setPort(redisPort);
            config.setPassword(RedisPassword.of(redisPassword)); // ✅ 비밀번호 설정 추가
    
            return new LettuceConnectionFactory(config);
        }
    
        /**
         * 🔹 사용자 권한 관리용 RedisTemplate (Object 저장)
         */
        @Bean
        @Primary // 기본적으로 주입되는 RedisTemplate 지정
        public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
    
            // Key 직렬화 방식 설정 (문자열)
            template.setKeySerializer(new StringRedisSerializer());
    
            // Value 직렬화 방식 설정 (JSON 변환)
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    
            return template;
        }
    
        /**
         * 🔹 CacheManager 빈 등록
         */
        @Bean
        public CacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
            return RedisCacheManager.builder(redisConnectionFactory).build();
        }
    
        /**
         * 🔹 메시징 전용 RedisTemplate (String 저장)
         */
        @Bean(name = "redisStringTemplate")
        public RedisTemplate<String, String> redisStringTemplate(LettuceConnectionFactory connectionFactory) {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
    
            StringRedisSerializer stringSerializer = new StringRedisSerializer();
            template.setKeySerializer(stringSerializer);
            template.setValueSerializer(stringSerializer);
    
            return template;
        }
    
        /**
         * 🔹 Redis Pub/Sub 메시지 리스너 컨테이너 설정
         */
        @Bean
        public RedisMessageListenerContainer redisMessageListenerContainer(
                LettuceConnectionFactory connectionFactory, MessageSubscriberService subscriber) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener(new MessageListenerAdapter(subscriber), new PatternTopic("chat_channel"));
            return container;
        }
    }


# 프로젝트 생성 및 방법
1. 스프링부트 프로젝트를 생성한다
2. 프로젝트 루트에 frontend 폴더를 생성한다
3. frontend 폴더에 리액트 프로젝트를 생성한다
4. 프로젝트 루트에 .gitignore 파일을 생성한다
5. .gitignore 파일에 node_modules 추가
6. 깃허브에 원격 저장소를 생성한다
7. 로컬에서 git init -> git add . -> git commit -m "first commit" -> git remote add origin git주소 -> git push -u origin main 명령어로 깃허브에 올린다


# WebSocket 연결 및 메시지 전송
1. 웹소켓(WebSocket)은 클라이언트와 서버 간의 지속적인 양방향 통신을 가능하게 하는 프로토콜입니다.
2. 전통적인 HTTP 방식은 요청-응답 방식으로 동작하지만, 웹소켓은 연결이 수립된 후 클라이언트와 서버 간에 실시간으로 데이터를 주고받을 수 있도록 합니다.
3. 연결이 한 번 이루어지면, 클라이언트와 서버 간에 지속적인 연결이 유지되어 여러 번의 요청/응답 없이 실시간 통신이 가능합니다.
   낮은 지연 시간: HTTP보다 훨씬 빠르게 실시간 데이터를 주고받을 수 있습니다.

# Redis Pub/Sub - WebSocket을 활용한 실시간 메시징 서비스
1. Redis는 메시지 브로커(Message Broker)로 사용할 수 있으며, Pub/Sub(Publish/Subscribe) 패턴을 지원합니다.
2. 사용자가 리액트의 메시지 목록 화면에서 Rest Api 형태로 메시지를 전송한다.
3. 서버의 컨트롤러에서 메시지를 받고 DB에 저장하고 Redis Publish를 통해 메시지를 발행한다.
   redisTemplate.convertAndSend(CHANNEL_NAME, jsonMessage); // 채널명으로 메시지 발행(메시지는 JSON 문자열), 이렇게 발행하면 Redis Subscriber가 메시지를 수신할 수  있음
   Redis Publish에서 위와 같이 채널명과 메시지를 발행하면, 해당 채널을 구독하고 있는 클라이언트에게 메시지가 전달됩니다.
   여기서 클라이언트 Redis Subscriber로 해당 채널을 구독하고 있어야 메시지를 수신할 수 있습니다.
   그 구독과 관련한 설정은 RedisConfig.java에서 설정을 해주었음.(Redis Pub <- Redis Sub 구독)
4. Redis Subscriber를 통해서 메시지를 전달 받고 그 메시지를 WebSocket을 통해 클라이언트에게 전달한다.
   messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getReceiverId(), objectMapper.writeValueAsString(messageDto));
   위 코드는 /topic/chat/{receiverId}로 메시지를 발행하고, 해당 채널을 구독하고 있는 클라이언트에게 메시지를 전달합니다.
5. 클라이언트는 다음과 같이 웹소켓을 구독하고 있기 때문에 4.번에서 웹소켓을 통해서 전송한 메시지를 받을 수 있다.
   stompClient.subscribe(`/topic/chat/${user.id}`, async (message) => {..}
6. 여기서 특징은 우리 시스템은 웹소켓만을 사용하여 메시지를 송수신하는 것이 아니라
   Redis Pub/Sub을 통해 메시지를 발행하고 구독하여 메시지를 송수신하는 방식을 사용하고 있다는 것이다.
   Redis Sub에서는 메시지를 수신하고, WebSocket을 통해 클라이언트에게 메시지를 전달한다.
7. WebSocket.java의 configureMessageBroker 메소드는 웹소켓만을 사용할 경우의 설정을 현재는 사용하지 않고있다.
8. CharController는 WebSocket 만 사용하여 실시간 메시지을 구현하기 때문에 여기서는 사용하지 않고 있다.

 
# Nginx를 사용한 프록시 서버 설정 파일

## 1. Nginx 로컬 애플리케이션 버전(localhost 환경)

#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
worker_connections  1024;
}


http {
include       mime.types;
default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        #location / {
        #    root   html;
        #    index  index.html index.htm;
        #}

		# 📌 React 정적 파일 제공 (nginx가 제공)
        location / {
            root   C:/javaworks/nginx-1.26.3/html;
            index  index.html;
            try_files $uri /index.html;
        }

        # 📌 Spring Boot API 프록시 설정 (api요청은 스프링 부트로 전달)
        location /api/ {
            proxy_pass http://localhost:8080/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

		# 📌 WebSocket 프록시 설정 (추가)
		location /ws/ {
			proxy_pass http://localhost:8080/ws/;
			proxy_http_version 1.1;
			proxy_set_header Upgrade $http_upgrade;
			proxy_set_header Connection "Upgrade";

			# ✅ WebSocket용 CORS 헤더 추가
			add_header Access-Control-Allow-Origin *;
			add_header Access-Control-Allow-Methods "GET, POST, OPTIONS";
			add_header Access-Control-Allow-Headers "Authorization, Content-Type";
			add_header Access-Control-Allow-Credentials true;
		}
		
        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}

## 2. Nginx AWS 애플리케이션 버전(AWS 환경)
[ec2-user@ip-172-31-34-202 frontend_student]$ sudo nano /etc/nginx/conf.d/app.conf

server {
listen 80;
server_name your_domain.com;

    # React 정적 파일 제공
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Spring Boot API 프록시 설정
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 프록시 설정
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;

        # ✅ WebSocket CORS 헤더 추가
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, OPTIONS";
        add_header Access-Control-Allow-Headers "Authorization, Content-Type";
        add_header Access-Control-Allow-Credentials true;
    }
}

