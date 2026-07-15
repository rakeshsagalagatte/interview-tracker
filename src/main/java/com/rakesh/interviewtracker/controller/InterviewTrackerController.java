package com.rakesh.interviewtracker.controller;

import com.rakesh.interviewtracker.constants.LoggerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/IVT")
public class InterviewTrackerController {

    private static final Logger LOGGER = LogManager.getLogger(InterviewTrackerController.class);

    @GetMapping("/getQuestions")
    public void getQuestions(){
        String methodName = "getQuestions";
        LOGGER.info(LoggerConstants._2_BRACE, methodName, LoggerConstants.METHOD_STARTS);
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOGGER.info(LoggerConstants._2_BRACE, methodName, LoggerConstants.METHOD_ENDS);
    }
}
