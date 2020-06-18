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
import it.uniroma3.siw.taskmanager.controller.validation.TagValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
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

	@RequestMapping(value = { "/tag/addTagForm/{taskId}" }, method = RequestMethod.GET)
	public String addTagFormToTask(@PathVariable("taskId") Long taskId, Model model) {
		Credentials credentials = this.sessionData.getLoggedCredentials();
		User loggedUser = this.sessionData.getLoggedUser();
		Task task = this.taskService.getTask(taskId);
		model.addAttribute("loggedUser",loggedUser);
		model.addAttribute("task", task);
		model.addAttribute("credentialsForm", credentials);
		model.addAttribute("tagForm", new Tag());
		return "addTagForm";
	}

	@RequestMapping(value = { "/tag/add/{taskId}" }, method = RequestMethod.POST)
	public String addTagToTask(@PathVariable("taskId") Long taskId,
			@Valid @ModelAttribute("tagForm") Tag tag, BindingResult tagBindingResult,
			Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		this.tagValidator.validate(tag, tagBindingResult);
		if(!tagBindingResult.hasErrors()) {
			Task task = this.taskService.getTask(taskId);
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
		return "/tag/addTagForm/{taskId}";

	}

	@RequestMapping(value = { "/tag/{tagId}/{taskId}" }, method = RequestMethod.GET)
	public String tagView(@PathVariable("tagId") Long tagId,
			@PathVariable("taskId") Long taskId, Model model) {
		User loggedUser = this.sessionData.getLoggedUser();
		Tag tag = this.tagService.getTag(tagId);
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("tag", tag);
		model.addAttribute("task", this.taskService.getTask(taskId));
		return "tag";
	}

}





























