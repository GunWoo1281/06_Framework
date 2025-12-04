package edu.kh.todo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.kh.todo.model.dto.Todo;
import edu.kh.todo.model.service.TodoService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MainController {

    @Autowired
    private TodoService service;

    @RequestMapping("/")
    public String mainPage(Model model) {

        log.debug("service : " + service);

        String testTitle = service.testTitle();
        model.addAttribute("testTitle", testTitle);

        Map<String, Object> map = service.selectAll();
        
        List<Todo> todoList = (List<Todo>) map.get("todoList");
        int completeCount = (int) map.get("completeCount");
        
        model.addAttribute("todoList", todoList);
        model.addAttribute("completeCount", completeCount);

        return "common/main";
    }
}
