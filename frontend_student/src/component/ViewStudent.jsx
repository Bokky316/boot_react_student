import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux"; // 리덕스 스토어에서 상태를 가져오기 위한 useSelector 사용
import { Button, TextField, Typography, Box } from "@mui/material";
import { fetchWithAuth } from "../common/fetchWithAuth"; // fetchWithAuth 함수 import


export default function ViewStudent() {
    const { id } = useParams();
    const [student, setStudent] = useState(null);
    const navigate = useNavigate();

    // 리덕스 스토어에서 사용자 정보 가져오기
    const { user } = useSelector((state) => state.auth);

    // useEffect에서 학생 정보를 가져오는 부분 수정
    useEffect(() => {
        const fetchStudent = async () => {
            try {
                const response = await fetchWithAuth(`/students/${id}`);
                if (response.ok) {
                    const data = await response.json();
                    setStudent(data);
                } else {
                    console.error("학생 정보 로드 실패:", response.status);
                }
            } catch (error) {
                console.error("학생 정보 로드 중 오류 발생:", error.message);
            }
        };

        fetchStudent();
    }, [id]);

// handleDelete 메소드 수정
    const handleDelete = async () => {
        if (window.confirm("정말로 삭제하시겠습니까?")) {
            try {
                const response = await fetchWithAuth(`/students/${id}`, { method: "DELETE" });
                if (response.ok) {
                    alert("학생이 삭제되었습니다.");
                    navigate("/");
                } else {
                    alert("삭제 실패");
                }
            } catch (error) {
                console.error("학생 삭제 중 오류 발생:", error.message);
            }
        }
    };

    if (!student) return <div>로딩 중...</div>;

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                padding: 2,
                margin: "0 auto",
                maxWidth: 600,
                gap: 2,
            }}
        >
            <Typography variant="h4" gutterBottom>
                {student.name}님의 상세 정보
            </Typography>
            <TextField
                label="이메일"
                value={student.email}
                fullWidth
                InputProps={{
                    readOnly: true,
                }}
            />
            <TextField
                label="나이"
                value={student.age}
                fullWidth
                InputProps={{
                    readOnly: true,
                }}
            />
            <TextField
                label="연락처"
                value={student.phone}
                fullWidth
                InputProps={{
                    readOnly: true,
                }}
            />
            <TextField
                label="주소"
                value={student.address}
                fullWidth
                InputProps={{
                    readOnly: true,
                }}
            />
            <Box sx={{ display: "flex", gap: 2, justifyContent: "center" }}>
                {user?.roles?.includes("ROLE_ADMIN") && (
                    <>
                        <Button variant="contained" onClick={() => navigate("/editStudent/" + id)}>
                            수정
                        </Button>
                        <Button variant="contained" color="error" onClick={handleDelete}>
                            삭제
                        </Button>
                    </>
                )}
                <Button variant="outlined" onClick={() => navigate("/listStudent")}>
                    목록
                </Button>
            </Box>
        </Box>
    );
}
