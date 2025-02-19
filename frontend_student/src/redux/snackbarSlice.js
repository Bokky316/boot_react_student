import { createSlice } from "@reduxjs/toolkit";

/**
 * 스낵바 상태를 관리하는 슬라이스(Slice) 정의
 * - 슬라이스(Slice)는 Redux Toolkit에서 도입된 개념으로, Redux 상태의 한 부분을(스낵바 상태) 관리하기 위한 로직을 모아놓은 단위입니다.
 * - 메시지를 보여주는 모달(Snackbar)의 오픈 여부 (open) 관리
 * - 표시될 메시지 (message) 관리
 * - Redux를 통해 상태를 전역적으로 관리할 수 있도록 설정
 * - 웹소켓을 통해 메시지가 도착하면 Redux를 활용하여 스낵바를 띄우도록 구현합니다.
 * 사용예시
 * - dispatch(showSnackbar("새로운 메시지가 도착했습니다!"));
 * - dispatch(hideSnackbar());
 */

const initialState = {// ✅ 초기 상태
    open: false,     // ✅ 스낵바가 열려 있는지 여부
    message: "",    // ✅ 스낵바에 표시될 메시지 내용
};

const snackbarSlice = createSlice({
    name: "snackbar",   // 슬라이스(Slice)의 이름
    initialState,       // 초기 상태를 위에서 정의한 initialState로 설정
    reducers: { // 리듀서 함수를 여러개 정의 (showSnackbar, hideSnackbar)
        showSnackbar: (state, action) => {  // ✅ 스낵바를 열고 메시지를 설정하는 리듀서 함수, 이렇게 하면 다른곳에서 이 함수를 사용할 수 있음, e.g dispatch(showSnackbar("메시지"));
            state.open = true;  // 스낵바를 열고
            state.message = action.payload; // payload에 전달된 메시지를 표시
        },
        hideSnackbar: (state) => {
            state.open = false;
            state.message = "";
        },
    },
});

export const { showSnackbar, hideSnackbar } = snackbarSlice.actions;
export default snackbarSlice.reducer;

/*
- export const { showSnackbar, hideSnackbar } = snackbarSlice.actions;
  snackbarSlice.actions란?
  createSlice를 사용하면 reducers에 정의한 함수들(showSnackbar, hideSnackbar)을 자동으로 액션 생성자(action creator) 형태로 만들어서 snackbarSlice.actions 객체 안에 넣어 줍니다.

  snackbarSlice.actions = {
      showSnackbar: (payload) => ({ type: "snackbar/showSnackbar", payload }),
      hideSnackbar: () => ({ type: "snackbar/hideSnackbar" })
  };

- export const { showSnackbar, hideSnackbar } = snackbarSlice.actions;
  위에서 생성된 actions 객체에서 showSnackbar과 hideSnackbar만 따로 빼내서 개별적으로 export 합니다.

  const showSnackbar = snackbarSlice.actions.showSnackbar;
  const hideSnackbar = snackbarSlice.actions.hideSnackbar;

  export { showSnackbar, hideSnackbar };

  이렇게 하면 다른 파일에서 showSnackbar과 hideSnackbar을 직접 import하여 사용가능함.

  import { showSnackbar, hideSnackbar } from "../../redux/snackbarSlice";

  dispatch(showSnackbar("새로운 메시지가 도착했습니다!"));
  dispatch(hideSnackbar());



*/