package com.project.shopapp.components.aspect;

import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.models.User;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
;
@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    @Pointcut("within(com.project.shopapp.controller.*)")
    public void controllerMethods(){}
    @Around("controllerMethods()")
    public Object loggerActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] objectArgs = joinPoint.getArgs();
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        // Ẩn mật khẩu nếu đối tượng là UserLoginDTO
        StringBuilder argsString = new StringBuilder();
        for (Object arg : objectArgs) {
            if (arg instanceof UserLoginDTO) {
                UserLoginDTO userLoginDTO = (UserLoginDTO) arg;
                argsString.append("UserLoginDTO(email=").append(userLoginDTO.getUserName())
                        .append(", password=****")
//                        .append(", roleId=").append(userLoginDTO.getRoleId())
//                        .append(", rememberMe=").append(userLoginDTO.getRememberMe())
                        .append(")");
            } else {
                argsString.append(arg);
            }
            argsString.append(", ");
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        logger.info("User logging: [" + timestamp + "] Method - " + methodName + ", Args - [" + argsString + "], IP address - " + remoteAddress);
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        logger.info("User activity finished: " + methodName + ", executionTime:" + executionTime);

        // Ghi thông tin vào file Excel
//        writeLogToExcel(methodName, argsString.toString(), remoteAddress, executionTime,timestamp);

        return result;
    }
    private void writeLogToExcel(String methodName, String args, String remoteAddress, long executionTime,String timestamp) {
        String excelFilePath = "src/main/resources/logs/excel/UserActivityLog.xlsx";
        Workbook workbook;
        Sheet sheet;

        try {
            File file = new File(excelFilePath);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Activity Logs");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Timestamp");
                header.createCell(1).setCellValue("Method Name");
                header.createCell(2).setCellValue("Arguments");
                header.createCell(3).setCellValue("IP Address");
                header.createCell(4).setCellValue("Execution Time (ms)");
            }

            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(rowCount + 1);

            row.createCell(0).setCellValue(timestamp);
            row.createCell(1).setCellValue(methodName);
            row.createCell(2).setCellValue(args);
            row.createCell(3).setCellValue(remoteAddress);
            row.createCell(4).setCellValue(executionTime);

            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
            }

            workbook.close();
        } catch (IOException e) {
            logger.error("Failed to write log to Excel file", e);
        }
    }

}
