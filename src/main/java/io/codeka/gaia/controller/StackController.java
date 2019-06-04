package io.codeka.gaia.controller;

import io.codeka.gaia.bo.Job;
import io.codeka.gaia.bo.Settings;
import io.codeka.gaia.repository.StackRepository;
import io.codeka.gaia.repository.TerraformModuleRepository;
import io.codeka.gaia.runner.StackRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;


@Controller
public class StackController {

    private StackRepository stackRepository;

    private StackRunner stackRunner;

    private TerraformModuleRepository terraformModuleRepository;

    private Settings settings;

    @Autowired
    public StackController(StackRepository stackRepository, StackRunner stackRunner, TerraformModuleRepository terraformModuleRepository, Settings settings) {
        this.stackRepository = stackRepository;
        this.stackRunner = stackRunner;
        this.terraformModuleRepository = terraformModuleRepository;
        this.settings = settings;
    }

    @GetMapping("/stacks")
    public String listStacks(){
        return "stacks";
    }

    @GetMapping("/stacks/{stackId}")
    public String editStack(@PathVariable String stackId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
            model.addAttribute("settings", settings);
        }
        return "stack";
    }

    @GetMapping("/stacks/{stackId}/apply")
    public String applyStack(@PathVariable String stackId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
        }

        // create a new job
        var job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setStackId(stackId);

        model.addAttribute("jobId", job.getId());

        // get the stack
        var stack = this.stackRepository.findById(stackId).get();
        // get the module
        var module = this.terraformModuleRepository.findById(stack.getModuleId()).get();

        this.stackRunner.run(job, module, stack);

        return "stack_apply";
    }

    @GetMapping("/api/stacks/{stackId}/jobs/{jobId}")
    @ResponseBody
    public Job getJob(@PathVariable String stackId, @PathVariable String jobId){
        return this.stackRunner.getJob(jobId);
    }

}