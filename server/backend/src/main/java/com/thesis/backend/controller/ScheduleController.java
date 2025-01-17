package com.thesis.backend.controller;

import com.thesis.backend.dto.request.ScheduleRequest;
import com.thesis.backend.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/teacher")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<String> register(@Valid @RequestBody ScheduleRequest scheduleRequest) throws IOException {
        return ResponseEntity.ok(scheduleService.registerSchedule(scheduleRequest));
    }

    @PutMapping
    public ResponseEntity<ScheduleRequest> update(@Valid @RequestBody ScheduleRequest scheduleRequest) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleRequest));
    }

    @DeleteMapping
    public ResponseEntity<String> delete(@Valid @RequestBody ScheduleRequest scheduleRequest) {
        scheduleService.deleteSchedule(scheduleRequest);
        return ResponseEntity.ok("Delete successfully!");
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleRequest>> fetch(@RequestParam int teacherid) {
        return ResponseEntity.ok(scheduleService.fetch(teacherid));
    }
}
