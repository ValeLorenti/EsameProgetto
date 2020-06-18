package it.uniroma3.siw.taskmanager.controller;

import java.util.ArrayList;
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
import it.uniroma3.siw.taskmanager.controller.validation.TagValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Tag;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TagService;
import it.uniroma3.siw.taskmanager.service.TaskService;

@Controller
public class TagController {

	@Autowired 
	TaskService taskService;

	@Autowired 
	TagService tagService;

	@Autowired 
	TagValidator tagValidator;

	@Autowired
	SessionData sessionData;

	@Autowired
	ProjectService projectService;

	@Autowired
	CredentialsService credentialsService;
	
	@RequestMapping(value = { "/tag/addExistingTag/{taskId}/{projectId}" }, method = RequestMethod.GET)
	public String addExistingTagToTaskForm(@PathVariable("taskId") Long taskId, @PathVariable("projectId") Long projectId, 
			Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		Credentials credentials = this.sessionData.getLoggedCredentials();
		Project project = this.projectService.getProject(projectId);
		Task task = this.taskService.getTask(taskId);
		
		/*rimuovo i tag del task da quelli del progetto*/
		List<Tag> projectTags = project.getTags();
		List<Tag> taskTags = task.getTags();
		List<Tag> addableTags = new ArrayList<>();
		for(Tag tag : projectTags) {
			if(!taskTags.contains(tag))
				addableTags.add(tag);
		}
		
		model.addAttribute("tags", addableTags);
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("credentialsForm", credentials);
		return "addExistingTags";
	}
	
	@RequestMapping(value = { "/tag/addExistingTag/{tagId}/{taskId}" }, method = RequestMethod.POST)
	public String addExistingTagToTask(@PathVariable("tagId") Long tagId, @PathVariable("taskId") Long taskId, Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		Task task = this.taskService.getTask(taskId);
		Tag tag = this.tagService.getTag(tagId);
		tag.getlinkedTasks().add(task);
		this.tagService.saveTag(tag);
		task.getTags().add(tag);
		this.taskService.saveTask(task);
		model.addAttribute("linkedTasks", tag.getlinkedTasks());
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("task", task);
		model.addAttribute("tag", tag);
		return "tag";
	}
	
	@RequestMapping(value = { "/tag/addTagForm/{projectId}" }, method = RequestMethod.GET)
	public String addTagToProjectForm(@PathVariable("projectId") Long projectId, Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		Credentials loggedUserCredentials = this.sessionData.getLoggedCredentials();
		Project project = this.projectService.getProject(projectId);
		model.addAttribute("project", project);
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("credentialsForm", loggedUserCredentials);
		model.addAttribute("tagForm", new Tag());
		return "addTagForm";
	}
	
	@RequestMapping(value = { "/tag/add/{projectId}" }, method = RequestMethod.POST)
	public String addTagToProject(@PathVariable("projectId") Long projectId,
			@Valid @ModelAttribute("tagForm") Tag tag, BindingResult tagBindingResult,
			Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		this.tagValidator.validate(tag, tagBindingResult);
		if(!tagBindingResult.hasErrors()) {
			Project project = this.projectService.getProject(projectId);
			project.getTags().add(this.tagService.saveTag(tag));
			this.projectService.saveProject(project);
			model.addAttribute("tag", tag);
			model.addAttribute("linkedTasks", tag.getlinkedTasks());
			model.addAttribute("userForm", loggedUser);
			return "tag";
		}
		return  "/tag/addTagForm/{projectId}";
	}
	
	@RequestMapping(value = { "/tag/{tagId}/{taskId}" }, method = RequestMethod.GET)
	public String tagView(@PathVariable("tagId") Long tagId,
			@PathVariable("taskId") Long taskId, Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		Tag tag = this.tagService.getTag(tagId);
		model.addAttribute("linkedTasks", tag.getlinkedTasks());
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("tag", tag);
		model.addAttribute("task", this.taskService.getTask(taskId));
		return "tag";
	}

}





























