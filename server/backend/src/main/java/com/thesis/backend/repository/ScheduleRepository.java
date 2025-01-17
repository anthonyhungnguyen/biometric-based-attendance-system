package com.thesis.backend.repository;

import com.thesis.backend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByDeviceIDAndEndTimeAfterOrderByEndTime(Integer deviceID, String startTimeInsert);

    List<Schedule> findByTeacherID(Integer teacherID);

    //    @Query(value = "select * from schedule where device_id = 1 and start_time <= Convert_TZ(Now(),\"SYSTEM\",\"+07:00\") and end_time >= Convert_TZ(Now(),\"SYSTEM\",\"+07:00\")", nativeQuery = true)
    Schedule findByDeviceIDAndStartTimeBeforeAndEndTimeAfter(Integer device, String timestamp, String timestamp1);

    Integer countScheduleBySemesterAndSubjectIDAndGroupCode(int semester, String subjectID, String groupCode);
}
