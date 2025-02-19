import React, { useState, useEffect } from "react";
import { DataGrid } from "@mui/x-data-grid";
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Typography, Box, Autocomplete } from "@mui/material";
import RefreshIcon from "@mui/icons-material/Refresh"; // ✅ 리프레시 아이콘 추가
import { useSelector, useDispatch } from "react-redux";
import { fetchWithAuth } from "../common/fetchWithAuth";
import "./MessageList.css";
import { showSnackbar } from "../redux/snackbarSlice";
import useDebounce from "../hooks/useDebounce";
import { decrementUnreadCount } from "../redux/messageSlice";




export default function MessagesList() {
    const { user } = useSelector((state) => state.auth);
    const unreadCount = useSelector(state => state.messages.unreadCount); // ✅ Redux에서 읽지 않은 메시지 개수만 가져옴
    const dispatch = useDispatch();

    const [messages, setMessages] = useState([]); // ✅ 메시지 목록을 Redux가 아닌 컴포넌트 내부에서 관리
    const [openSendMessageModal, setOpenSendMessageModal] = useState(false);
    const [messageContent, setMessageContent] = useState("");
    const [selectedUser, setSelectedUser] = useState(null);
    const [users, setUsers] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const debouncedQuery = useDebounce(searchQuery, 300);
    const [openReplyModal, setOpenReplyModal] = useState(false);
    const [selectedMessage, setSelectedMessage] = useState(null);
    const [replyContent, setReplyContent] = useState("");

    useEffect(() => {
        if (user) {
            fetchMessages();
        }

        if (debouncedQuery.length >= 2) {
            fetchUsers(debouncedQuery);
        } else {
            setUsers([]);
        }
    }, [user, debouncedQuery]);

    const fetchUsers = async (query) => {
        if (!query) return;

        try {
            const response = await fetchWithAuth(`/members/search?query=${query}`);
            if (response.ok) {
                const data = await response.json();
                setUsers(data.data || []);
            } else {
                setUsers([]);
            }
        } catch (error) {
            console.error("🚨 사용자 검색 실패:", error.message);
            setUsers([]);
        }
    };

    const fetchMessages = async () => {
        try {
            const response = await fetchWithAuth(`/messages/${user.id}`);
            if (response.ok) {
                const data = await response.json();
                setMessages(data); // ✅ Redux가 아닌 로컬 상태에서 메시지 관리
            }
        } catch (error) {
            console.error("🚨 메시지 목록 조회 실패:", error.message);
        }
    };


    // 새로고침 버튼 클릭 시 실행
    const refreshMessages = () => {
        fetchMessages(); // ✅ 메시지 목록 새로 조회
        dispatch(showSnackbar("🔄 메시지 목록이 업데이트되었습니다."));
    };

    const handleSendMessage = async () => {
        if (!selectedUser || !messageContent) {
            dispatch(showSnackbar("❌ 수신자와 메시지를 입력해주세요."));
            return;
        }

        try {
            await fetchWithAuth(`/messages/send`, {
                method: "POST",
                body: JSON.stringify({
                    senderId: user.id,
                    receiverId: selectedUser.id,
                    content: messageContent,
                }),
            });

            setOpenSendMessageModal(false);
            setMessageContent("");
            setSelectedUser(null);
            dispatch(showSnackbar("✅ 메시지가 성공적으로 전송되었습니다."));
        } catch (error) {
            console.error("🚨 메시지 전송 실패:", error.message);
        }
    };

    const handleReply = async () => {
        if (!selectedMessage || !replyContent) return;

        try {
            await fetchWithAuth(`/messages/send`, {
                method: "POST",
                body: JSON.stringify({
                    senderId: user.id,
                    receiverId: selectedMessage.senderId,
                    content: replyContent,
                }),
            });

            setOpenReplyModal(false);
            setReplyContent("");
            dispatch(showSnackbar("✅ 답장이 전송되었습니다."));
        } catch (error) {
            console.error("🚨 메시지 응답 실패:", error.message);
        }
    };

    const handleOpenMessage = async (message) => {
        setSelectedMessage(message);
        setOpenReplyModal(true);

        if (!message.read) {
            await fetchWithAuth(`/messages/read/${message.id}`, { method: "POST" });
            dispatch(decrementUnreadCount()); // ✅ 읽지 않은 메시지 개수 감소
        }
    };

    const getRowClassName = (params) => {
        return params.row.read ? "read-message" : "unread-message";
    };

    const columns = [
        {
            field: "content",
            headerName: "메시지 내용",
            flex: 3,
            renderCell: (params) => (
                <Button color="primary" onClick={() => handleOpenMessage(params.row)}>
                    {params.value.slice(0, 30) + "..."}
                </Button>
            ),
        },
        { field: "senderName", headerName: "보낸 사람", flex: 1 },
        {
            field: "regTime",
            headerName: "보낸 날짜",
            flex: 2,
            renderCell: (params) =>
                new Date(params.value).toLocaleString("ko-KR", {
                    year: "numeric",
                    month: "2-digit",
                    day: "2-digit",
                    hour: "2-digit",
                    minute: "2-digit",
                    second: "2-digit"
                }).replace(/\. /g, "-").replace(" ", " "),
        },
    ];

    return (
        <div className="data-grid-container">
            <Box display="flex" justifyContent="center" width="100%" mb={2}>

                <Typography variant="h4" gutterBottom>
                    받은 메시지 ({unreadCount})
                </Typography>

            </Box>

            <Box display="flex" justifyContent="flex-end" width="100%" mb={1}>
                <Button
                    variant="contained"
                    color="warning"  // ✅ 색상을 오렌지색으로 변경
                    onClick={refreshMessages}
                    startIcon={<RefreshIcon />}
                    style={{ marginRight: "10px" }} // ✅ 버튼 간 간격 추가
                >
                    새로고침
                </Button>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={() => setOpenSendMessageModal(true)}
                >
                    메시지 보내기
                </Button>
            </Box>


            <DataGrid
                rows={messages}
                columns={columns}
                pageSizeOptions={[5, 10, 20]}
                disableRowSelectionOnClick
                autoHeight
                getRowClassName={getRowClassName}
            />

            <Dialog
                open={openReplyModal}
                onClose={() => setOpenReplyModal(false)}
                fullWidth
                maxWidth="sm"
                PaperProps={{
                    style: {
                        backgroundColor: "#f5f5f5", // ✅ 배경색 추가
                        borderRadius: "10px" // ✅ 모서리를 둥글게
                    }
                }}
            >
                <DialogTitle style={{ textAlign: "center", fontWeight: "bold", color: "#333" }}>
                    ✉️ 메시지 내용
                </DialogTitle>
                <DialogContent>
                    <Typography
                        style={{
                            backgroundColor: "#ffffff", // ✅ 메시지 내용 배경을 흰색으로 설정
                            padding: "15px",
                            borderRadius: "8px",
                            boxShadow: "0px 2px 5px rgba(0, 0, 0, 0.1)"
                        }}
                    >
                        {selectedMessage?.content}
                    </Typography>
                    <TextField
                        fullWidth
                        multiline
                        rows={4}
                        label="답장"
                        value={replyContent}
                        onChange={(e) => setReplyContent(e.target.value)}
                        margin="normal"
                        variant="outlined"
                        InputProps={{
                            style: {
                                backgroundColor: "#ffffff", // ✅ 입력 필드 배경을 흰색으로 설정
                                borderRadius: "8px"
                            }
                        }}
                    />
                </DialogContent>
                <DialogActions style={{ justifyContent: "space-between", padding: "15px" }}>
                    <Button
                        onClick={() => setOpenReplyModal(false)}
                        style={{
                            backgroundColor: "#d32f2f",
                            color: "#fff",
                            borderRadius: "8px"
                        }}
                    >
                        취소
                    </Button>
                    <Button
                        onClick={handleReply}
                        style={{
                            backgroundColor: "#1976d2",
                            color: "#fff",
                            borderRadius: "8px"
                        }}
                    >
                        보내기
                    </Button>
                </DialogActions>
            </Dialog>


            <Dialog open={openSendMessageModal} onClose={() => setOpenSendMessageModal(false)} fullWidth maxWidth="sm">
                <DialogTitle>메시지 보내기</DialogTitle>
                <DialogContent>
                    <Autocomplete
                        options={users}
                        getOptionLabel={(option) => option.name}
                        onChange={(event, value) => setSelectedUser(value)}
                        onInputChange={(event, newInputValue) => fetchUsers(newInputValue)}
                        renderInput={(params) => <TextField {...params} label="받는 사람" fullWidth />}
                    />
                    <TextField
                        fullWidth
                        multiline
                        rows={4}
                        label="메시지 내용"
                        value={messageContent}
                        onChange={(e) => setMessageContent(e.target.value)}
                        margin="normal"
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenSendMessageModal(false)}>취소</Button>
                    <Button onClick={handleSendMessage} color="primary">보내기</Button>
                </DialogActions>
            </Dialog>





        </div>
    );
}
