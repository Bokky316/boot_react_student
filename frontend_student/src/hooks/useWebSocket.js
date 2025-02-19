import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { showSnackbar } from "../redux/snackbarSlice";
import { useDispatch } from "react-redux";
import { useEffect } from "react";
import { API_URL, SERVER_URL} from "../constant";
import { fetchWithAuth } from "../common/fetchWithAuth";
import { setUnreadCount, incrementUnreadCount } from "../redux/messageSlice"; // ✅ 읽지 않은 메시지 개수만 관리

let stompClient = null;

const useWebSocket = (user) => {
    const dispatch = useDispatch();

    useEffect(() => {
        if (!user?.id || stompClient) return;

        console.log("🛠 WebSocket 연결 시도 - user ID:", user?.id);
        // ✅ WebSocket 연결, 처음에는 Http 요청이 가지만 이후에는 WebSocket으로 업그레이드 됨. 즉, 웹소켓으로 연결
        // 이 과정에서 /ws/info 엔드포인트로 접속하여 WebSocket 연결을 시도하여 성공하면 /ws로 연결(자동으로 진행됨)
        const socket = new SockJS(`${SERVER_URL}/ws`);

        stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log(`🔍 WebSocket Debug: ${str}`),
            reconnectDelay: 5000,

            onConnect: async () => {
                console.log("📡 WebSocket 연결 성공!");

                // ✅ 읽지 않은 메시지 개수 가져오기
                await fetchUnreadCount(user.id, dispatch);

                // ✅ 메시지 구독
                stompClient.subscribe(`/topic/chat/${user.id}`, async (message) => {
                    //alert('새로운 메시지가 도착했습니다.');
                    console.log("📨 새로운 메시지 도착!", message.body);

                    // ✅ 새로운 메시지를 받아서 읽지 않은 메시지 개수 증가
                    dispatch(incrementUnreadCount());
                    //alert('스넥바 알림 표시');
                    // ✅ 스낵바 알림 표시
                    const parsedMessage = JSON.parse(message.body);
                    dispatch(showSnackbar(`📩 새로운 메시지가 도착했습니다`));
                });
            },

            onStompError: (frame) => {
                console.error("❌ STOMP 오류 발생:", frame);
            },
        });

        stompClient.activate();

        return () => {
            if (stompClient) {
                stompClient.deactivate();
                stompClient = null;
            }
        };
    }, [user, dispatch]);
};

// ✅ 서버에서 읽지 않은 메시지 개수 조회
const fetchUnreadCount = async (userId, dispatch) => {
    try {
        const response = await fetchWithAuth(`/messages/unread/${userId}`);
        if (response.ok) {
            const count = await response.json();
            dispatch(setUnreadCount(count)); // ✅ Redux에 개수 저장
        }
    } catch (error) {
        console.error("🚨 읽지 않은 메시지 개수 조회 실패:", error.message);
    }
};

export default useWebSocket;
