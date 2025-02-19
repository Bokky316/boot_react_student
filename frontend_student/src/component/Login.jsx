import React, { useState } from "react";
import { Button, TextField } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { API_URL } from "../constant";
import { setUser } from "../redux/authSlice";
import { fetchWithAuth } from "../common/fetchWithAuth";
import { setUnreadCount } from "../redux/messageSlice";
import { setInvitedChatRoomsCount } from "../redux/chatSlice";
import { useSelector, useDispatch } from "react-redux";

/**
 * ✅ 로그인 컴포넌트
 * - 로그인 후 사용자 정보를 Redux에 저장하고,
 * - 읽지 않은 메시지 개수 & 초대받은 채팅방 개수를 가져와 Redux에 저장
 */
export default function Login({ onLogin }) {
    const [credentials, setCredentials] = useState({ email: "test@example.com", password: "1234" });
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const user = useSelector((state) => state.auth.user);

    const handleChange = (event) => {
        setCredentials({ ...credentials, [event.target.name]: event.target.value });
    };

    /**
     * ✅ 로그인 요청을 보내는 함수
     */
    const handleLogin = async () => {
        try {
            const formData = new URLSearchParams();
            formData.append("username", credentials.email);
            formData.append("password", credentials.password);

            console.log('API_URL : ', API_URL)

            const response = await fetch(`${API_URL}/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: formData,
                credentials: "include", // 쿠키 포함
            });

            const data = await response.json();

            if (!response.ok || data.status === "failed") {
                setErrorMessage(data.message || "로그인 실패");
                return;
            }

            console.log(`Login 성공: ${data.name} / ${data.id}`);

            dispatch(setUser({
                id: data.id,
                name: data.name,
                email: credentials.email,
                roles: data.roles,
            }));

            fetchUnreadMessagesCount(data.id);
            fetchInvitedChatRoomsCount(data.id);

            navigate("/");
        } catch (error) {
            console.error("로그인 요청 실패:", error.message);
        }
    };

    /**
     * ✅ 읽지 않은 메시지 개수 조회
     */
    const fetchUnreadMessagesCount = async (userId) => {
        try {
            const response = await fetchWithAuth(`/messages/unread/${userId}`);
            if (response.ok) {
                const data = await response.json();
                dispatch(setUnreadCount(data));
            }
        } catch (error) {
            console.error("🚨 읽지 않은 메시지 조회 실패:", error.message);
        }
    };

    /**
     * ✅ 초대받은 채팅방 개수 조회
     */
    const fetchInvitedChatRoomsCount = async (userId) => {
        try {
            const response = await fetchWithAuth(`/chat/invitation/count/${userId}`);
            if (response.ok) {
                const data = await response.json();
                dispatch(setInvitedChatRoomsCount(data));
            }
        } catch (error) {
            console.error("🚨 초대받은 채팅방 개수 조회 실패:", error.message);
        }
    };

    return (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", marginTop: "20px" }}>
            <TextField
                label="Email"
                name="email"
                value={credentials.email}
                onChange={handleChange}
                style={{ width: "400px", marginBottom: "10px" }}
            />
            <TextField
                label="Password"
                name="password"
                type="password"
                value={credentials.password}
                onChange={handleChange}
                style={{ width: "400px", marginBottom: "10px" }}
                error={!!errorMessage}
                helperText={errorMessage}
            />
            <div style={{ display: "flex", justifyContent: "space-between", width: "400px" }}>
                <Button variant="contained" onClick={handleLogin}>
                    로그인
                </Button>
                <Button variant="outlined" onClick={() => navigate("/registerMember")}>
                    회원가입
                </Button>
            </div>
        </div>
    );
}
