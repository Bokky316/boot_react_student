import React, { useState, useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { fetchUserInfo, clearUser } from "./redux/authSlice";
import { persistor } from "./redux/store";
import { setUnreadCount } from "./redux/messageSlice";
import useWebSocket from "./hooks/useWebSocket";
import { fetchWithAuth } from "./common/fetchWithAuth.js";
import {
    AppBar,
    Toolbar,
    Typography,
    Button,
    Badge,
    Snackbar,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions
} from "@mui/material";
import { showSnackbar, hideSnackbar } from "./redux/snackbarSlice";
import { Routes, Route, Link, Navigate, useNavigate  } from "react-router-dom";
import StudentList from "./component/StudentList";
import AddStudent from "./component/AddStudent";
import Login from "./component/Login";
import MyPage from "./component/member/MyPage.jsx";
import ViewStudent from "./component/ViewStudent";
import EditStudent from "./component/EditStudent";
import RegisterMember from "./component/member/RegisterMember";
import UnauthorizedPage from "./component/UnAuthorizedPage.jsx";
import Home from "./component/Home";
import MessageList from "./component/MessageList";
import { setChatRooms, setInvitedChatRoomsCount } from "./redux/chatSlice"; // ✅ chatSlice에서 가져옴
import ChatRoomList from "./component/chat/ChatRoomList";
import OrderDetail from "./component/OrderDetail"; // Import the OrderDetail component
import PayResult from "./component/PayResult"; // ✅ 결제 결과 페이지 추가


function App() {
    const { user, isLoggedIn } = useSelector((state) => state.auth);
    const unreadCount = useSelector(state => state.messages.unreadCount || 0);  // ✅ 읽지 않은 메시지 개수를 리덕스 스토어에서 가져옴
    const { open, message } = useSelector((state) => state.snackbar || { open: false, message: "" });   // ✅ 스낵바 상태를 리덕스 스토어에서 가져옴
    const invitedChatRoomsCount = useSelector((state) => state.chat.invitedChatRoomsCount); // ✅ 초대받은 채팅 메시지를 리덕스 스토어에서 가져옴

    const dispatch = useDispatch();

    const [openMessageModal, setOpenMessageModal] = useState(false); // ✅ 메시지 모달 상태
    const [messages, setMessages] = useState([]); // ✅ 받은 메시지 목록 상태
    const navigate = useNavigate();

    useWebSocket(user);  // ✅ WebSocket 연결을 App에서 실행


    // ✅ 사용자명 위의 배지를 클릭하면 받은 메시지 목록 모달을 열어주는 함수
    const handleOpenMessageModal = async () => {
        //await fetchReceivedMessages();
        setOpenMessageModal(true);
    };

    // ✅ 채팅방 초대받은 메시지 목록을 가져오는 함수
    const handleOpenChatRooms = () => {
        if (!user) {
            alert("로그인이 필요합니다.");
            return;
        }
        navigate("/chatrooms");
    };

    const handleLogout = async () => {
        try {
            await fetchWithAuth(`/auth/logout`, { method: "POST" });
            dispatch(clearUser());
            await persistor.purge();
            window.location.href = "/";
        } catch (error) {
            console.error("로그아웃 실패:", error.message);
            alert("로그아웃 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="App">
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h3" style={{ flexGrow: 1 }}>
                        <Button color="inherit" component={Link} to="/">학생관리시스템</Button>
                        {isLoggedIn && <Button color="inherit" component={Link} to="/listStudent">학생목록</Button>}
                        {isLoggedIn && user?.roles?.includes("ROLE_ADMIN") && <Button color="inherit" component={Link} to="/addStudent">학생 등록</Button>}
                        {isLoggedIn && <Button color="inherit" component={Link} to={`/mypage/${user?.id}`}>마이페이지</Button>}
                        {isLoggedIn && <Button color="inherit" component={Link} to="/messages">메시지목록</Button>}
                        {/* {isLoggedIn && <Button color="inherit" onClick={handleOpenChatRooms}>채팅하기</Button>} */}
                        {isLoggedIn && (
                            <Badge
                                badgeContent={invitedChatRoomsCount > 0 ? invitedChatRoomsCount : null}
                                color="secondary"
                                style={{ cursor: "pointer", marginRight: "15px" }}
                            >
                                <Button color="inherit" onClick={handleOpenChatRooms}>채팅하기</Button>
                            </Badge>
                        )}
                        {isLoggedIn && (
                            <Button color="inherit" component={Link} to="/orderDetail">상품주문</Button>
                        )}
                    </Typography>

                    {isLoggedIn ? (
                        <>
                            {/* 🔹 배지를 클릭하면 메시지 목록 모달이 열림 */}
                            <Badge
                                badgeContent={unreadCount > 0 ? unreadCount : null}
                                color="error"
                                onClick={handleOpenMessageModal}
                                style={{ cursor: "pointer" }}
                            >
                                <Typography variant="body1" style={{ marginRight: "10px", fontSize: "14px" }}>
                                    {user.name}
                                </Typography>
                            </Badge>
                            <Button color="inherit" onClick={handleLogout}>로그아웃</Button>
                        </>
                    ) : (
                        <Button color="inherit" component={Link} to="/login">로그인</Button>
                    )}
                </Toolbar>
            </AppBar>

            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/listStudent" element={<StudentList />} />
                <Route path="/addStudent" element={user?.roles?.includes("ROLE_ADMIN") ? <AddStudent /> : <Navigate to="/unauthorized" replace />} />
                <Route path="/viewStudent/:id" element={<ViewStudent />} />
                {isLoggedIn && user?.roles?.includes("ROLE_ADMIN") && <Route path="/editStudent/:id" element={<EditStudent />} />}
                <Route path="/registerMember" element={<RegisterMember />} />
                <Route path="/login" element={<Login />} />
                <Route path="/mypage/:id" element={<MyPage />} />
                <Route path="/messages" element={<MessageList />} />
                <Route path="/chatrooms" element={<ChatRoomList />} />
                <Route path="/unauthorized" element={<UnauthorizedPage />} />
                <Route path="/orderDetail" element={<OrderDetail />} /> {/* ✅ 상품 주문 페이지 */}
                <Route path="/payResult" element={<PayResult />} />  {/* ✅ 결제 결과 페이지 라우트 추가 */}

            </Routes>

            {/* ✅ 이름 위의 읽지 않은 메시지 숫자의 배지 클릭시 메시지 목록 모달 */}
            <Dialog open={openMessageModal} onClose={() => setOpenMessageModal(false)} fullWidth maxWidth="md">
                <DialogTitle>메시지 목록</DialogTitle>
                <DialogContent dividers>
                    <MessageList />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenMessageModal(false)} color="primary">닫기</Button>
                </DialogActions>
            </Dialog>


            {/* ✅ 스낵바 알림 */}
            <Snackbar
                open={open}
                autoHideDuration={3000}
                onClose={() => dispatch(hideSnackbar())}
                message={message}
            />
        </div>
    );
}

export default App;
