package com.thesis.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesis.backend.constant.EntityType;
import com.thesis.backend.constant.ExceptionType;
import com.thesis.backend.dto.mapper.ScheduleMapper;
import com.thesis.backend.dto.model.SubjectIDDto;
import com.thesis.backend.dto.request.ScheduleRequest;
import com.thesis.backend.exception.CustomException;
import com.thesis.backend.model.Schedule;
import com.thesis.backend.model.User;
import com.thesis.backend.repository.ScheduleRepository;
import com.thesis.backend.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleService {
    private final static String GROUP_ID = "group-id";
    private final static String SCHEDULE_TOPIC = "schedule";
    private final static String ATTENDANCE_TOPIC = "attendance";
    private final static String MODIFY_TOPIC = "update";
    private final static String DELETE_TOPIC = "delete";
    private final static String DATA_TOPIC = "data";
    private final static String CHECKIN_TOPIC = "checkin";
    private final SubjectServiceImpl subjectService;
    private final UserServiceImpl userService;
    private final ScheduleRepository scheduleRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FirebaseService firebaseService;

    @Autowired
    public ScheduleService(SubjectServiceImpl subjectService, UserServiceImpl userService, ScheduleRepository scheduleRepository, KafkaTemplate<String, Object> kafkaTemplate, FirebaseService firebaseService) {
        this.subjectService = subjectService;
        this.userService = userService;
        this.scheduleRepository = scheduleRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.firebaseService = firebaseService;
    }

    public Boolean registerSchedule(ScheduleRequest scheduleRequest) throws IOException {
        String classCode = scheduleRequest.getSemester() + "_" + scheduleRequest.getSubjectID() + "_" + scheduleRequest.getGroupCode();
        String lastMetaData = firebaseService.downloadMetadata(classCode);
        List<String> allPathList = firebaseService.listFiles("student");
        List<User> users = subjectService.findAllUsersTakeSubject(new SubjectIDDto(scheduleRequest.getSubjectID(),
                scheduleRequest.getGroupCode(),
                scheduleRequest.getSemester()));
        List<String> studentList = new ArrayList<>();
        for (User u : users) {
            studentList.add(String.valueOf(u.getId()));
        }
        List<String> pathList = firebaseService.filterPathWithStudentList(allPathList, studentList);
        List<String> studentInMetaData = loadMetadata();
        pathList.removeAll(studentInMetaData);
        Map<String, Object> message = new HashMap<>();
        message.put("student", pathList);
        message.put("lastMetaDataPath", lastMetaData);
        kafkaTemplate.send(CHECKIN_TOPIC, message);
        return true;
//        LocalDateTime startDateTimeLdt = DateUtil.convertStringToLocalDateTime(scheduleRequest.getStartTime());
//        LocalDateTime endDateTimeLdt = DateUtil.convertStringToLocalDateTime(scheduleRequest.getEndTime());
//        // if (!checkOverlap(scheduleRequest.getDeviceID(), startDateTimeLdt, endDateTimeLdt)) {
//        Schedule schedule = ScheduleMapper.toModel(scheduleRequest);
//
//        scheduleRepository.save(schedule);
//        scheduleRequest.setId(schedule.getId());
//        kafkaTemplate.send(SCHEDULE_TOPIC, scheduleRequest);
        //throw CustomException.throwException(EntityType.SCHEDULE, ExceptionType.OVERLAP);
    }

    private List<String> loadMetadata() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> temp = mapper.readValue(new File("/home/phuchung/temp.json"), Map.class);
        return (List<String>) temp.get("student_path_list");
    }

    public ScheduleRequest updateSchedule(ScheduleRequest request) {
        Optional<Schedule> schedule = scheduleRepository.findById(request.getId());
        if (schedule.isPresent()) {
            schedule.get().setStartTime(Timestamp.valueOf(DateUtil.convertStringToLocalDateTime(request.getStartTime())));
            schedule.get().setEndTime(Timestamp.valueOf(DateUtil.convertStringToLocalDateTime(request.getEndTime())));
            Schedule scheduleUpdated = scheduleRepository.save(schedule.get());
            kafkaTemplate.send(MODIFY_TOPIC, scheduleUpdated);
            return ScheduleMapper.toDto(scheduleUpdated);
        }
        throw CustomException.throwException(EntityType.SCHEDULE, ExceptionType.ENTITY_EXCEPTION, String.valueOf(request.getId()));
    }


    public void deleteSchedule(ScheduleRequest request) {
        Optional<Schedule> schedule = scheduleRepository.findById((request.getId()));
        if (schedule.isPresent()) {
            scheduleRepository.delete(schedule.get());
            kafkaTemplate.send(DELETE_TOPIC, schedule.get());
        }
        throw CustomException.throwException(EntityType.SCHEDULE, ExceptionType.ENTITY_EXCEPTION, String.valueOf(request.getId()));
    }

    public List<ScheduleRequest> fetch(int userid) {
        userService.find(userid);
        return scheduleRepository.findByTeacherID(userid)
                .stream().map(ScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<Schedule> existScheduleRightNow(Integer deviceID) {
        Timestamp now = Timestamp.from(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
        Optional<Schedule> schedule = Optional.ofNullable(scheduleRepository.findByDeviceIDAndStartTimeBeforeAndEndTimeAfter(deviceID, now, now));
        return schedule;
    }

    public Optional<Schedule> fetchOne(Integer deviceID, Timestamp timestamp) {
        return Optional.ofNullable(scheduleRepository.findByDeviceIDAndStartTimeBeforeAndEndTimeAfter(deviceID, timestamp, timestamp));
    }


    public boolean checkOverlap(Integer deviceID, LocalDateTime startTime, LocalDateTime endTime) {
        Timestamp startTimeTS = Timestamp.valueOf(startTime);
        Timestamp endTimeTS = Timestamp.valueOf(endTime);
        List<Schedule> schedules = scheduleRepository.findByDeviceIDAndEndTimeAfterOrderByEndTime(deviceID, startTimeTS);
        Optional<Schedule> minEndTimeSchedule = schedules.stream().findFirst();
        if (minEndTimeSchedule.isPresent()) {
            if (endTimeTS.getTime() > minEndTimeSchedule.get().getStartTime().getTime()) {
                return true;
            }
        }
        return false;
    }
}