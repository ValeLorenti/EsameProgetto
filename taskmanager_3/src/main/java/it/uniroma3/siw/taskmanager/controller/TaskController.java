package it.uniroma3.siw.taskmanager.controller;

import javax.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.TaskValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TaskService;

@Controller
public class TaskController {

	@Autowired 
	TaskService taskService;

	@Autowired
	SessionData sessionData;

	@Autowired
	ProjectService projectService;

	@Autowired
	TaskValidator taskValidator;

	@RequestMapping(value = { "/task/addTaskForm/{id}" }, method = RequestMethod.GET)
	public String addTaskForm(@PathVariable("id") Long projectId, Model model) {
		Credentials credentials = this.sessionData.getLoggedCredentials();
		User loggedUser = this.sessionData.getLoggedUser();
		Project project = this.projectService.getProject(projectId);
		model.addAttribute("project", project);
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("credentialsForm", credentials);
		model.addAttribute("taskForm", new Task());
		return "addTaskForm";
	}

	@RequestMapping(value = { "/task/add/{projectId}" }, method = RequestMethod.POST)
	public String addTask(@PathVariable("projectId") Long projectId,
			@Valid @ModelAttribute("taskForm") Task task, 
			BindingResult taskBindingResult, 
			Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		this.taskValidator.validate(task, taskBindingResult);
		if(!taskBindingResult.hasErrors()) {
			Project project = this.projectService.getProject(projectId);
			this.projectService.addTaskToProject(project, this.taskService.saveTask(task));
			model.addAttribute("userForm", loggedUser);
			model.addAttribute("task", task);
			return "task";
		}
		return "redirect:/task/addTaskForm/{projectId}";

	}

	@RequestMapping(value = {"/task/update/{id}"}, method = RequestMethod.GET)
	public String updateTaskForm(@PathVariable("id") Long taskId, Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Task task = this.taskService.getTask(taskId);

		model.addAttribute("userForm", loggedUser);
		model.addAttribute("task", task);
		return "updateMyTaskForm";	
	}

	@RequestMapping( value = {"/task/me/update/{id}"}, method = RequestMethod.POST)
	public String updateTask(@PathVariable("id") Long taskId,
			@Valid @ModelAttribute("task") Task newTask,
			Model model) {
		Task oldTask = this.taskService.getTask(taskId);

		oldTask.setName(newTask.getName());
		oldTask.setDescription(newTask.getDescription());
		this.taskService.saveTask(oldTask);

		return "updatedTaskSuccessful";
	}

	@RequestMapping(value = { "/projects/{projectId}/deleteTask/{taskId}" }, method = RequestMethod.POST)
	public String deleteTask(@PathVariable("projectId")Long projectId, 
			@PathVariable("taskId")Long taskId, Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = this.projectService.getProject(projectId);
		User owner = project.getOwner();

		if(loggedUser.equals(owner)) {
			project.deleteTaskById(taskId);
			this.taskService.deleteTask(this.taskService.getTask(taskId));
			this.projectService.saveProject(project);
			return "redirect:/projects/{projectId}";
		}
		else
			return "redirect:/projects/{projectId}";
	}

}
