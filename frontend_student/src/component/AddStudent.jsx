import { Button, TextField } from '@mui/material';
import { useState } from 'react';
import { useNavigate } from "react-router-dom";
import { fetchWithAuth } from "../common/fetchWithAuth"; // fetchWithAuth 함수 import

/**
 * 학생 정보 등록 컴포넌트
 */
export default function AddStudent() {
    // 입력된 학생 정보를 저장할 상태 변수
    const [student, setStudent] = useState({
        name: '',
        email: '',
        age: '',
        phone: '',
        address: '',
    });

    const navigate = useNavigate();

    // 학생 정보 입력 시 상태 변경
    const onStudentChange = (event) => {
        setStudent({ ...student, [event.target.name]: event.target.value });
    };

    // 학생 정보 등록
    const handleOnSubmit = async () => {
        try {
            console.log("학생 등록 시작"); // 시작 로그

            // 요청 옵션 설정, fetchWithAuth 함수 호출시 제공
            const requestOptions = {
                method: 'POST',
                body: JSON.stringify(student),
            };
            console.log("요청 옵션:", requestOptions);

            // fetchWithAuth 호출
            const response = await fetchWithAuth(`/students`, requestOptions);
            console.log("응답 객체:", response);

            // 응답 처리
            if (response.ok) {
                const data = await response.json(); // JSON 데이터 추출
                console.log("학생 등록 성공:", data);
                navigate("/"); // 등록 후 학생 목록으로 이동
            } else {
                console.error("학생 등록 실패:", response.status);
                const errorData = await response.json(); // 오류 메시지 추출
                alert(`학생 등록 실패: ${errorData.message}`);
            }
        } catch (error) {
            console.error("학생 등록 중 오류 발생:", error);
            alert('학생 등록 실패 (네트워크 또는 서버 오류)');
        }
    };


    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: '20px' }}>
            <TextField label="Name" name="name" value={student.name} onChange={onStudentChange} style={{ width: '400px', marginBottom: '10px' }} />
            <TextField label="Email" name="email" value={student.email} onChange={onStudentChange} style={{ width: '400px', marginBottom: '10px' }} />
            <TextField label="Age" name="age" value={student.age} onChange={onStudentChange} style={{ width: '400px', marginBottom: '10px' }} />
            <TextField label="Phone" name="phone" value={student.phone} onChange={onStudentChange} style={{ width: '400px', marginBottom: '10px' }} />
            <TextField label="Address" name="address" value={student.address} onChange={onStudentChange} style={{ width: '400px', marginBottom: '10px' }} />
            <Button variant="contained" onClick={handleOnSubmit}>
                등록
            </Button>
        </div>
    );
}
