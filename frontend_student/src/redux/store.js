import { configureStore, combineReducers } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from "redux-persist";
import storage from "redux-persist/lib/storage";
import authReducer from "./authSlice"; // ✅ 사용자 인증 관련 리듀서
import snackbarReducer from "./snackbarSlice"; // ✅ 스낵바(알림) 관련 리듀서
import messageReducer from "./messageSlice"; // ✅ 메시지 관련 리듀서 (읽지 않은 메시지 개수 등)
import chatReducer from "./chatSlice"; // ✅ 채팅 관련 리듀서 (채팅방 목록, 초대받은 채팅방 개수)

/**
 * Redux Persist 설정
 * - Redux Persist는 Redux 상태를 localStorage 또는 sessionStorage에 저장하여
 *   새로고침(페이지 리로드) 후에도 상태를 유지할 수 있도록 해줍니다.
 * - persistConfig 설정을 통해 어떤 Redux 상태를 저장할지 선택합니다.
 */
const persistConfig = {
    key: "root", // localStorage에 저장될 Redux Persist 상태의 키 이름
    storage, // 사용할 스토리지 엔진 (여기서는 localStorage)
    whitelist: ["auth", "snackbar", "messages", "chat"], // ✅ Redux 상태 중 localStorage에 저장할 목록
};

/**
 * 루트 리듀서 생성
 * - 여러 개의 리듀서를 하나로 합쳐 Redux 스토어에서 관리할 수 있도록 합니다.
 * - Redux 상태에서 'auth', 'snackbar', 'messages', 'chat' 등의 상태를 각각 관리할 수 있게 됩니다.
 */
const rootReducer = combineReducers({
    auth: authReducer, // ✅ 사용자 인증 상태 (로그인 정보)
    snackbar: snackbarReducer, // ✅ 스낵바 알림 상태 (UI 메시지)
    messages: messageReducer, // ✅ 메시지 상태 (읽지 않은 메시지 개수 관리)
    chat: chatReducer, // ✅ 채팅 상태 (채팅방 목록 및 초대받은 채팅방 개수)
});

/**
 * Persisted Reducer 생성
 * - persistReducer는 Redux Persist를 적용한 리듀서를 생성하는 역할을 합니다.
 * - 기존 rootReducer를 감싸서 Redux Persist가 작동하도록 합니다.
 */
const persistedReducer = persistReducer(persistConfig, rootReducer);

/**
 * Redux Store 생성
 * - Redux 상태를 저장하는 중앙 저장소(Store)를 생성합니다.
 * - Redux Toolkit의 configureStore 함수를 사용하여 Redux DevTools 지원 및
 *   미들웨어 설정을 간편하게 관리할 수 있습니다.
 * - serializableCheck 설정을 통해 Redux Persist 관련 액션을 직렬화 오류 없이 처리합니다.
 */
export const store = configureStore({
    reducer: persistedReducer, // ✅ Redux Persist 적용된 루트 리듀서
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: {
                ignoredActions: ["persist/PERSIST", "persist/REHYDRATE"], // Redux Persist 관련 액션 직렬화 체크 제외
            },
        }),
});

/**
 * Redux Persistor 생성
 * - persistStore 함수는 Redux Persist의 상태를 관리하는 역할을 합니다.
 * - 이를 통해 Redux 상태가 localStorage에서 복구될 수 있도록 설정합니다.
 */
export const persistor = persistStore(store);
