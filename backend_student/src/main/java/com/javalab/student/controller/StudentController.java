package com.javalab.student.controller;

import com.javalab.student.dto.PageRequestDTO;
import com.javalab.student.dto.PageResponseDTO;
import com.javalab.student.dto.StudentDto;
import com.javalab.student.dto.StudentFormDto;
import com.javalab.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@Slf4j
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 학생 등록
    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@RequestBody @Valid StudentFormDto studentFormDto) {
        log.info("학생 등록 요청 수신: {}", studentFormDto);

        // 학생 서비스 호출
        StudentDto savedStudent = null;
        try {
            savedStudent = studentService.createStudent(studentFormDto);
            log.info("학생 등록 성공: {}", savedStudent);
        } catch (Exception e) {
            log.error("학생 등록 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 내부 서버 오류 반환
        }

        // 저장된 학생 정보 반환
        return ResponseEntity.ok(savedStudent);
    }

    // 학생 수정
    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable("id") Long id, @RequestBody @Valid StudentFormDto studentFormDto) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentFormDto));
    }

    // 학생 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable("id") Long id) {
        log.info("학생 정보 조회 : " + id);
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    // 페이징 처리된 학생 목록 조회
    @GetMapping
    public ResponseEntity<PageResponseDTO<StudentDto>> getAllStudents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();
        log.info("페이지 : " + pageRequestDTO.getPage() + " " + pageRequestDTO.getSize());

        PageResponseDTO<StudentDto> responseDTO = studentService.getAllStudents(pageRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // 학생 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
