/**
 * src/common/fetchWithAuth.js
 * - API 요청 시 JWT 액세스 토큰을 헤더에 포함하여 요청하는 fetchWithAuth 함수 정의
 * - API 요청 시 인증이 필요 없는 경우 fetchWithoutAuth 함수 정의
 * - 액세스 토큰 만료 시 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받는 refreshAccessToken 함수 정의
 */

const API_URL = import.meta.env.VITE_API_URL; // ✅ 프로덕션 환경에서도 환경변수 사용

/**
 * 액세스 토큰 갱신을 위한 리프레시 토큰 요청 함수
 */
export const refreshAccessToken = async () => {
    try {
        const response = await fetch(`${API_URL}/token/refresh`, {
            method: "POST",
            credentials: "include", // HttpOnly 쿠키 포함 요청
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            console.log("리프레시 토큰 갱신 실패", response.status);
            return false;
        }
        console.log("refreshAccessToken 리프레시 토큰 발급 성공 response: ", response);
        return true;
    } catch (error) {
        console.error("리프레시 토큰 처리 오류:", error.message);
        return false;
    }
};

/**
 * JWT 토큰을 포함하여 API 요청하는 함수
 * -  credentials: "include", 옵션을 통해 HttpOnly 쿠키를 포함하여 요청
 */
export const fetchWithAuth = async (endpoint, options = {}) => {
    const config = {
        ...options,
        headers: {
            "Content-Type": "application/json",
        },
        credentials: "include",
    };

    try {
        let response = await fetch(`${API_URL}${endpoint}`, config);

        if (response.status === 401) {
            const errorData = await response.json();
            console.warn(`401 Error: ${errorData.message}`);

            if (errorData.message.includes("만료")) {
                console.log("fetchWithAuth.js: 액세스 토큰 만료되어 refreshAccessToken() 호출 - 1");
                const refreshSuccess = await refreshAccessToken();

                if (refreshSuccess) {
                    console.log("리프레시 토큰 성공, 기존 요청 재시도");
                    response = await fetch(`${API_URL}${endpoint}`, config);
                } else {
                    console.error("리프레시 토큰 갱신 실패");
                    throw new Error("Unauthorized: 리프레시 토큰 갱신 실패");
                }
            } else {
                throw new Error(`Unauthorized: ${errorData.message}`);
            }
        }

        return response;
    } catch (error) {
        console.error("API 요청 실패:", error.message);
        throw error;
    }
};

/**
 * 인증이 필요 없는 API 요청을 보내는 함수
 */
export const fetchWithoutAuth = async (endpoint, options = {}) => {
    const config = {
        ...options,
        headers: {
            "Content-Type": "application/json",
        },
        credentials: "include",
    };

    try {
        const response = await fetch(`${API_URL}${endpoint}`, config);
        return response;
    } catch (error) {
        console.error("API 요청 실패:", error.message);
        throw error;
    }
};
