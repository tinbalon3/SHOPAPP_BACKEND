package com.project.shopapp.components;

import com.project.shopapp.models.Category;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

//consumer
@Component
public class KafkaCategoryConsumer {

    @KafkaListener(id = "insertCategory",topics ="insert-a-category" )
    public void listenCategory(Category category){
        System.out.println("Received: " + category);
    }



    @KafkaListener(id = "getAllCategory",topics ="get-all-categories" )
    public void listenListOfCategories(List<Category> categories) {
        System.out.println("Received: " + categories);
    }
}
