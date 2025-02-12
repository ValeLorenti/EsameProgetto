package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

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
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TaskService;
import it.uniroma3.siw.taskmanager.model.Commento;
import it.uniroma3.siw.taskmanager.service.CommentoService;

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

	@Autowired
	CredentialsService credentialsService;

	@Autowired
	private CommentoService commentoService;

	@RequestMapping(value = { "/task/addTaskForm/{projectId}" }, method = RequestMethod.GET)
	public String addTaskForm(@PathVariable("projectId") Long projectId, Model model) {
		Credentials credentials = this.sessionData.getLoggedCredentials();
		User loggedUser = this.sessionData.getLoggedUser();
		Project project = this.projectService.getProject(projectId);
		model.addAttribute("project", project);
		model.addAttribute("taskForm", new Task());
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("credentialsForm", credentials);
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
			model.addAttribute("project", project);
			model.addAttribute("userForm", loggedUser);
			model.addAttribute("task", task);
			model.addAttribute("manager", task.getManager());
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

	@RequestMapping(value = { "/task/share/{projectId}/{taskId}" }, method = RequestMethod.GET)
	public String shareTaskWithForm(@PathVariable("projectId") Long projectId, 
			@PathVariable("taskId") Long taskId, Model model) {
		Project project = this.projectService.getProject(projectId);
		Task task = this.taskService.getTask(taskId);
		List<Credentials> membersCredentials = this.credentialsService.getMembersCredentialsByProject(project);

		/*Rimuovo le credenziali del manager dalla lista membersCredentials
		 * 
		 */
		model.addAttribute("membersCredentials", membersCredentials);
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		return "shareTaskWithForm";
	}

	@RequestMapping(value = { "/task/shareTaskWith/{userName}/{taskId}/{projectId}" }, method = RequestMethod.POST)
	public String shareTaskWith(@PathVariable("userName") String userName, 
			@PathVariable("taskId") Long taskId, @PathVariable("projectId") Long projectId, Model model) {
		User member = this.credentialsService.getCredentials(userName).getUser();
		Task task = this.taskService.getTask(taskId);
		task.setManager(member);
		this.taskService.saveTask(task);
		return "redirect:/task/{taskId}/{projectId}";
	}

	@RequestMapping(value = { "/task/{taskId}/{projectId}" }, method = RequestMethod.GET)
	public String taskView(@PathVariable("taskId") Long taskId,
			@PathVariable("projectId") Long projectId, Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		Task task = this.taskService.getTask(taskId);
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("task", task);
		model.addAttribute("project", this.projectService.getProject(projectId));
		return "task";
	}

	@RequestMapping( value = {"/projects/{projectId}/task/{taskId}/addComment"}, method = RequestMethod.GET)
	public String addComment(Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {

		Project project = projectService.getProject(projectId);
		User loggedUser = sessionData.getLoggedUser();

		if((project.getMembers().contains(loggedUser))||(project.getOwner().equals(loggedUser))) {
			Task task = taskService.getTask(taskId);
			String commento = new String();
			model.addAttribute("project", project);
			model.addAttribute("task", task);
			model.addAttribute("commento", commento);
			return "addComment";
		}
		return "redirect:/projects/{projectId}/task/{taskId}";
	}

	@RequestMapping( value = {"/projects/{projectId}/task/{taskId}/addComment"}, method = RequestMethod.POST)
	public String addComment(@ModelAttribute("commento") String commento, Model model, @PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId) {
		User loggedUser = this.sessionData.getLoggedUser();
		Project project = this.projectService.getProject(projectId);
		Task task = taskService.getTask(taskId);
		Commento comment = new Commento();
		comment.setCommento(commento);
		comment.setUser(sessionData.getLoggedUser());
		task.addCommento(comment);
		System.out.println();
		commentoService.saveCommento(comment);
		taskService.saveTask(task);
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		model.addAttribute("userForm", loggedUser);

		return "task";

	}

}







