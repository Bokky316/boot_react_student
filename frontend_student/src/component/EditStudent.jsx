import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField } from "@mui/material";
import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchWithAuth } from "../common/fetchWithAuth";
import { useSelector } from "react-redux";

/**
 * 학생 정보 수정 컴포넌트
 * - 학생 목록 화면에서 모달로 열린 경우, studentData로 학생 정보를 전달받고, onClose 함수로 모달 닫기를 받음.
 *   studentData는 학생의 정보를 담고 있는 객체
 *   onClose는 모달을 닫는 함수이다. 모달을 닫으면 학생 목록 화면으로 되돌아간다.
 * - 학생 상세보기 화면에서 일반 페이지로 열린 경우
 *   studentData는 null이고, URL에서 학생 ID를 추출하여 그걸로 학생 정보를 불러온다.
 *
 * @param studentData
 * @param updateStudent
 * @param onClose
 * @returns {JSX.Element}
 * @constructor
 */
export default function EditStudent({ studentData = null, updateStudent, onClose }) {
    const { id } = useParams(); // URL에서 ID 추출
    const [student, setStudent] = useState(studentData); // 학생 정보 상태
    const [loading, setLoading] = useState(true); // 로딩 상태
    const navigate = useNavigate();

    // 리덕스 스토어에서 사용자 정보 가져오기
    const { user } = useSelector((state) => state.auth);

    useEffect(() => {
        if (!studentData && id) {
            fetchStudentById();
        } else {
            setLoading(false); // studentData가 있는 경우 로딩 해제
        }
    }, [studentData, id]);

    // 특정 학생 정보를 조회
    const fetchStudentById = async () => {
        setLoading(true);
        try {
            const response = await fetchWithAuth(`/students/${id}`);
            if (response.ok) {
                const data = await response.json();
                setStudent(data);
            } else {
                console.error("학생 정보 조회 실패:", response.status);
                const errorData = await response.json();
                alert(`학생 정보 조회 실패: ${errorData.message}`);
            }
        } catch (error) {
            console.error("학생 정보 로드 중 오류 발생:", error.message);
            alert("학생 정보 로드 실패: 네트워크 또는 서버 오류");
        } finally {
            setLoading(false);
        }
    };
    // 입력 필드 변경 핸들러
    const handleInputChange = (event) => {
        setStudent({ ...student, [event.target.name]: event.target.value });
    };

    /**
     * 수정된 학생 정보를 저장하는 함수
     * - 모달로 열린 경우 모달을 닫는다.
     * - 일반 페이지로 열린 경우 학생 상세 정보 페이지로 이동한다.
     * @returns {Promise<void>}
     */
    const handleSave = async () => {
        try {
            const response = await fetchWithAuth(`/students/${student.id}`, {
                method: "PUT",
                body: JSON.stringify(student),
            });

            if (response.ok) {
                alert("학생 정보가 수정되었습니다.");

                if (onClose) {
                    // 모달로 열린 경우
                    updateStudent(student); // 부모 컴포넌트의 목록 갱신 함수 호출
                    onClose(); // 모달 닫기
                } else {
                    // 일반 페이지로 열린 경우
                    navigate(`/viewStudent/${id}`); // 학생 상세 정보 페이지로 이동
                }
            } else {
                console.error("학생 정보 수정 실패:", response.status);
                const errorData = await response.json();
                alert(`수정 실패: ${errorData.message}`);
            }
        } catch (error) {
            console.error("학생 수정 중 오류 발생:", error.message);
            alert("수정 실패: 네트워크 또는 서버 오류");
        }
    };


    if (loading) {
        return <div>로딩 중...</div>;
    }

    if (!student) {
        return <div>학생 정보를 로드할 수 없습니다.</div>;
    }

    return (
        <>
            {onClose ? (
                // 모달로 열린 경우
                <Dialog open={Boolean(student)} onClose={onClose}>
                    <DialogTitle>학생 정보 수정</DialogTitle>
                    <DialogContent>{renderStudentFields()}</DialogContent>
                    <DialogActions>
                        <Button onClick={onClose}>취소</Button>
                        {user?.roles?.includes("ROLE_ADMIN") && (
                            <Button onClick={handleSave} color="primary">
                                저장
                            </Button>
                        )}
                    </DialogActions>
                </Dialog>
            ) : (
                // 일반 페이지로 열린 경우
                <div style={{ padding: "20px", maxWidth: "600px", margin: "0 auto" }}>
                    <h2>학생 정보 수정</h2>
                    {renderStudentFields()}
                    <div style={{ display: "flex", justifyContent: "space-between", marginTop: "20px" }}>
                        <Button variant="outlined" onClick={() => navigate("/")}>
                            목록
                        </Button>
                        {user?.roles?.includes("ROLE_ADMIN") && (
                            <Button variant="contained" color="primary" onClick={handleSave}>
                                저장
                            </Button>
                        )}
                    </div>
                </div>
            )}
        </>
    );
    // return 구문에 사용할 학생 정보 입력 필드 렌더링 함수
    function renderStudentFields() {
        return (
            <>
                <TextField
                    label="Name"
                    name="name"
                    value={student?.name || ""}
                    onChange={handleInputChange}
                    fullWidth
                    margin="dense"
                />
                <TextField
                    label="Email"
                    name="email"
                    value={student?.email || ""}
                    onChange={handleInputChange}
                    fullWidth
                    margin="dense"
                />
                <TextField
                    label="Age"
                    name="age"
                    value={student?.age || ""}
                    onChange={handleInputChange}
                    fullWidth
                    margin="dense"
                />
                <TextField
                    label="Phone"
                    name="phone"
                    value={student?.phone || ""}
                    onChange={handleInputChange}
                    fullWidth
                    margin="dense"
                />
                <TextField
                    label="Address"
                    name="address"
                    value={student?.address || ""}
                    onChange={handleInputChange}
                    fullWidth
                    margin="dense"
                />
            </>
        );
    }
}
