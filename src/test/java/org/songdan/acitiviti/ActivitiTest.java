package org.songdan.acitiviti;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RepositoryService;
//import org.apache.catalina.security.SecurityUtil;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.songdan.activiti.StartApp;
import org.songdan.activiti.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StartApp.class)
public class ActivitiTest {

    private Logger logger = LoggerFactory.getLogger(ActivitiTest.class);

    /**
     * 流程定义相关操作
     */
    @Autowired
    private ProcessRuntime processRuntime;

    /**
     * 任务相关操作
     */
    @Autowired
    private TaskRuntime taskRuntime;

    /**
     * security工具类
     */
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 手动方式部署
     */
    @Test
    public void deploy(){
        // 进行部署
        Deployment deployment = repositoryService.createDeployment()
                // 文件夹的名称不能是process
                .addClasspathResource("processes/springboot01.bpmn")
//                .addClasspathResource("processes/qingjia.png")
//                .addClasspathResource("qingjia.bpmn")
//                .addClasspathResource("qingjia.png")
                .name("springboot01")
                .deploy();

        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    /**
     * 查看流程定义 springboot01.bpmn
     */
    @Test
    public void testDefinition01() {
        securityUtil.logInAs("salaboy");
        Page<ProcessDefinition> processDefinitionPage = processRuntime
                .processDefinitions(Pageable.of(0, 10));
        logger.info("> Available Process definitions: " +
                processDefinitionPage.getTotalItems());
        for (Object pd : processDefinitionPage.getContent()) {
            logger.info("\t > Process definition: " + pd);
        }
    }

    /**
     * 流程实例启动 springboot01.bpmn
     */
    @Test
    public void testStartInstance01(){
        securityUtil.logInAs("salaboy");
        String key = "springboot01";
        String content = "test instance";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        logger.info("> Processing content: " + content
                + " at " + formatter.format(new Date()));
        ProcessInstance processInstance = processRuntime
                .start(ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(key)
//                        .withProcessInstanceName("Processing Content: " + content)
//                        .withName("Processing Content: " + content)
                        .withVariable("content", content)
                        .build());
        logger.info(">>> Created Process Instance: " + processInstance);
    }

    /**
     * 查询任务并完成任务 springboot01.bpmn
     */
    @Test
    public void queryTaskAndComplete01(){

        securityUtil.logInAs("salaboy");

        // 7.0.M6可以创建task，不过貌似和流程没有关系，待研究
        // Let's create a Group Task (not assigned, all the members of the group can claim it)
        //  Here 'salaboy' is the owner of the created task
//        logger.info("> Creating a Group Task for 'firstGroup'");
//        taskRuntime.create(TaskPayloadBuilder.create()
//                .withName("First Group Task")
//                .withDescription("This is something really important")
//                .withCandidateGroup("firstGroup")
//                .withPriority(10)
//                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
        if( tasks.getTotalItems() > 0) {
            // 有任务时，完成任务
            for (Task task : tasks.getContent()) {
                System.out.println(task);
                // 拾取任务
                taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
                logger.info("拾取任务成功");
                // 完成任务
                taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
                logger.info("完成任务");
            }
        }
    }

}
