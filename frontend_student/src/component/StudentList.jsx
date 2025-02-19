import { DataGrid } from '@mui/x-data-grid';
import { Button, Snackbar } from '@mui/material';
import { useState, useEffect } from 'react';
import EditStudent from './EditStudent';
import { useNavigate, Link } from "react-router-dom";
import { fetchWithAuth } from '../common/fetchWithAuth.js';

export default function StudentList() {
    // 학생 목록을 저장할 상태 변수
    const [students, setStudents] = useState([]);
    // 스낵바 메시지를 저장할 상태 변수
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [totalRows, setTotalRows] = useState(0);
    const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 10 });
    const [selectedStudent, setSelectedStudent] = useState(null); // 모달용 선택 학생
    const [isEditModalOpen, setIsEditModalOpen] = useState(false); // 모달 열림 상태
    const navigate = useNavigate();

    /**
     * 컴포넌트가 마운트될 때 한 번 실행되며, paginationModel이 변경될 때도 실행됩니다.
     */
    useEffect(() => {
        fetchStudents();
    }, [paginationModel]);

    // 학생 목록을 불러오는 함수
    const fetchStudents = async () => {
        const { page, pageSize } = paginationModel; // 현재 페이지와 페이지 크기, 0, 10으로 초기화된 값이 들어있음

        try {
            const response = await fetchWithAuth(`/students?page=${page}&size=${pageSize}`, { method: "GET" });
            console.log("학생 목록 응답 객체:", response);

            if (response.ok) {
                const data = await response.json(); // JSON 데이터 추출
                setStudents(data.dtoList || []);
                setTotalRows(data.total || 0);
            } else {
                console.error("학생 목록 불러오기 실패:", response.status);
                const errorData = await response.json();
                alert(`학생 목록 불러오기 실패: ${errorData.message}`);
            }
        } catch (error) {
            console.error("학생 목록 가져오는 중 오류 발생:", error.message);
            if (error.message === "Unauthorized") {
                navigate("/unauthorized");
            } else {
                alert("학생 목록 가져오기 실패: 네트워크 또는 서버 오류");
            }
        }
    };
    // 학생 삭제 함수
    const deleteStudent = async (id) => {
        if (window.confirm("정말로 삭제하시겠습니까?")) {
            try {
                const response = await fetchWithAuth(`/students/${id}`, { method: "DELETE" });
                console.log("학생 삭제 응답 객체:", response);

                if (response.ok) {
                    await fetchStudents(); // 목록 갱신
                    setSnackbarMessage("학생이 삭제되었습니다.");
                    setSnackbarOpen(true);
                } else {
                    console.error("학생 삭제 실패:", response.status);
                    const errorData = await response.json();
                    alert(`학생 삭제 실패: ${errorData.message}`);
                }
            } catch (error) {
                console.error("학생 삭제 중 오류 발생:", error.message);
                alert("학생 삭제 실패: 네트워크 또는 서버 오류");
            }
        }
    };

    // 학생 목록에서 특정 학생을 수정하기 위해서 [수정] 버튼을 누르면 호출되는 함수
    // (student) : DataGrid의 각 행에 해당하는 데이터(학생 한명 정보)
    const openEditModal = (student) => {
        // 1. 인수로 전달된 학생 정보를 selectedStudent 상태 변수에 저장합니다.
        setSelectedStudent(student);
        // 2. 모달을 열기 위해 isEditModalOpen을 true로 설정합니다.
        setIsEditModalOpen(true);
    };

    const closeEditModal = () => {
        setSelectedStudent(null);
        setIsEditModalOpen(false);
    };

    // 학생 정보 수정 함수로 자식인 EditStudent 컴포넌트에서 호출되고 거기서 보낸 학생을 받는다.
    const updateStudent = async (updatedStudent) => {
        try {
            const response = await fetchWithAuth(`/students/${updatedStudent.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedStudent),
            });
            if (response.ok) {
                await fetchStudents();
                setSnackbarMessage('학생 정보가 수정되었습니다.');
                setSnackbarOpen(true);
            } else {
                alert('학생 수정 실패');
            }
        } catch (error) {
            console.error(error);
        } finally {
            closeEditModal();
        }
    };

    // DataGrid의 각 컬럼 정의
    const columns = [
        { field: 'id', headerName: 'ID', flex: 1 },
        {
            field: 'name',
            headerName: '이름',
            flex: 2,
            renderCell: (params) => (
                <Link to={`/viewStudent/${params.row.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                    {params.value}
                </Link>
            ),
        },
        { field: 'email', headerName: '이메일', flex: 3 },
        { field: 'age', headerName: '나이', flex: 1 },
        { field: 'phone', headerName: '연락처', flex: 2 },
        {
            field: 'edit',
            headerName: '수정',
            flex: 1,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color="primary"
                    // params.row : DataGrid의 각 행에 해당하는 데이터(학생 한명 정보)
                    onClick={() => openEditModal(params.row)}
                >
                    수정
                </Button>
            ),
        },
        {
            field: 'delete',
            headerName: '삭제',
            flex: 1,
            renderCell: (params) => (
                <Button
                    variant="contained"
                    color="secondary"
                    onClick={() => deleteStudent(params.row.id)}
                >
                    삭제
                </Button>
            ),
        },
    ];

    return (
        <div style={{ height: 700, width: '100%' }}>
            <DataGrid
                rows={students}
                columns={columns}
                rowCount={totalRows}
                paginationMode="server"
                pageSizeOptions={[5, 10, 20]}
                paginationModel={paginationModel}
                onPaginationModelChange={(newModel) => setPaginationModel(newModel)}
                disableRowSelectionOnClick
            />

            <Snackbar
                open={snackbarOpen}
                autoHideDuration={3000}
                onClose={() => setSnackbarOpen(false)}
                message={snackbarMessage}
            />

            <Button variant="contained" onClick={() => navigate('/addStudent')}>
                학생 등록
            </Button>
            {/* React에서 && 연산자는 왼쪽 조건이 true일 때만 오른쪽의 JSX가 렌더링
            isEditModalOpen이 true가 되면, EditStudent 컴포넌트가 렌더링된다.
            부모 컴포넌트가 모달로 열릴 수 있도록 프롭스를 전달한다.
            studentData 프롭스 : 선택된 학생 정보를 EditStudent 컴포넌트로 전달해서 수정할 수 있도록 함
            updateStudent 프롭스 : EditStudent 컴포넌트에서 수정된 학생 정보를 [저장]
            onClose 프롭스 : EditStudent 컴포넌트에서 [취소] 버튼을 누르면 모달을 닫기 위해 사용
            */}
            {isEditModalOpen && (
                <EditStudent
                    studentData={selectedStudent}
                    updateStudent={updateStudent}
                    onClose={closeEditModal}
                />
            )}
        </div>
    );
}
