import { createSlice } from "@reduxjs/toolkit";

const messageSlice = createSlice({
    name: "messages",
    initialState: {
        unreadCount: 0, // ✅ 읽지 않은 메시지 개수만 유지
    },
    reducers: {
        /**
         * ✅ 읽지 않은 메시지 개수 설정
         * @param {number} action.payload - 읽지 않은 메시지 개수
         */
        setUnreadCount: (state, action) => {
            state.unreadCount = action.payload;
        },

        /**
         * ✅ 읽지 않은 메시지 개수 증가
         */
        incrementUnreadCount: (state) => {
            state.unreadCount++;
        },

        /**
         * ✅ 읽지 않은 메시지 개수 감소
         */
        decrementUnreadCount: (state) => {
            if (state.unreadCount > 0) {
                state.unreadCount--;
            }
        }
    }
});

// ✅ 액션 생성자 내보내기
export const { setUnreadCount, incrementUnreadCount, decrementUnreadCount } = messageSlice.actions;
export default messageSlice.reducer;
