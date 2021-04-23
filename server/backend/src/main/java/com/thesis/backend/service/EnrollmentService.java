package com.thesis.backend.service;

import com.thesis.backend.dto.model.EnrollmentDto;
import com.thesis.backend.dto.model.SubjectDto;
import com.thesis.backend.dto.model.SubjectIDDto;
import com.thesis.backend.dto.model.UserDto;

import java.util.List;

public interface EnrollmentService {
    EnrollmentDto enroll(EnrollmentDto enrollmentDto);

    void unregister(EnrollmentDto enrollmentDto);

    List<UserDto> findAllUsersTakeSubject(SubjectIDDto subjectIDDto);

    List<SubjectDto> findAllSubjectsTakenByUser(Integer userid);

    List<SubjectIDDto> findAllSubjectIdsTakenByUser(Integer userid);

    boolean checkDidEnrolled(UserDto user, SubjectDto subject);
}