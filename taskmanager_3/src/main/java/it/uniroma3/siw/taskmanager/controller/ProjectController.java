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
import it.uniroma3.siw.taskmanager.controller.validation.ProjectValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.UserService;

@Controller
public class ProjectController {

	@Autowired
	ProjectService projectService;

	@Autowired
	UserService userService;
	
	@Autowired
	CredentialsService credentialsService;


	@Autowired
	ProjectValidator projectValidator;

	@Autowired
	SessionData sessionData;


	@RequestMapping(value = {"/projects"}, method = RequestMethod.GET)
	public String myOwnedProjects(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		List<Project> projectsList = projectService.retrieveProjectsOwnedBy(loggedUser);
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectsList", projectsList);
		return "myOwnedProjects";
	}
	@RequestMapping(value = {"/projects/{projectId}"}, method = RequestMethod.GET)
	public String project(@PathVariable Long projectId, Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		if(project == null)
			return "redirect:/projects";	

		List<User> members = userService.getMembers(project);
		if(!project.getOwner().equals(loggedUser) && !members.contains(loggedUser))
			return "redirect:/projects";	
		
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("members", members);
		return "project";


	}

	@RequestMapping(value = { "/projects/add" }, method = RequestMethod.GET)
	public String createProjectForm(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectForm", new Project());
		return "addProject";
	}

	@RequestMapping(value = { "/projects/add" }, method = RequestMethod.POST)
	public String createProject(@Valid @ModelAttribute("projectForm") Project project,
			BindingResult projectBindingResult, Model model) {
		
		User loggedUser = sessionData.getLoggedUser();
		projectValidator.validate(project, projectBindingResult);
		if(!projectBindingResult.hasErrors()) {
			project.setOwner(loggedUser);
			this.projectService.saveProject(project);
			return "redirect:/projects/" + project.getId();
		}
		model.addAttribute("loggedUser", loggedUser);
		return "addProject";
	}


	@RequestMapping(value = { "/delete/{id}" }, method = RequestMethod.GET)
	public String deleteProject(@PathVariable("id")Long id, Model model) {
		this.projectService.deleteProject(id);
		return "redirect:/projects/";
	}
	
	@RequestMapping(value = { "/project/shareWithForm/{id}" }, method = RequestMethod.GET)
	public String shareWithForm(@PathVariable("id")Long id, Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = this.projectService.getProject(id);
		model.addAttribute("project", project);
		List<Credentials> allCredentials = this.credentialsService.getAllCredentials();
		
		/*rimuovo me stesso per non apparire tra gli utenti con cui 
		 * posso condividere il progetto
		 */
		allCredentials.remove(sessionData.getLoggedCredentials());
		model.addAttribute("allCredentials", allCredentials);
		model.addAttribute("loggedUser", loggedUser);
		return "shareWithForm";
	}
	
	@RequestMapping(value = { "/project/shareWith/{userName}/{id}" }, method = RequestMethod.GET)
	public String shareWith(@PathVariable("id")Long id, @PathVariable String userName, Model model) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = this.projectService.getProject(id);
		User member = this.credentialsService.getCredentials(userName).getUser();
		member.getVisibleProjects().add(project);
		this.userService.saveUser(member);
		this.projectService.shareProjectWithUser(project, member);
		model.addAttribute("loggedUser", loggedUser);
		return"redirect:/projects";
	}
	
	@RequestMapping(value = { "/sharedProjects" }, method = RequestMethod.GET)
	public String sharedProjectsView(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("sharedProjects", this.projectService.retrieveProjectsMemberBy(loggedUser));
		return "sharedProjects";
	}
	
	@RequestMapping(value="/project/update/{id}",method=RequestMethod.GET)
	public String updateProjectForm (@PathVariable("id")Long projectId, Model model) {
		User loggedUser = sessionData.getLoggedUser();
	    Project project = this.projectService.getProject(projectId);
		
		model.addAttribute("userForm", loggedUser);
		model.addAttribute("project", project);
	
		return "updateMyProjectForm";
	}
	
	@RequestMapping(value="/project/me/update/{id}",method=RequestMethod.POST)
	public String updateProject(@PathVariable("id")Long projectId,
			@Valid @ModelAttribute("project") Project newProject,
			Model model) {
		Project oldProject = this.projectService.getProject(projectId);
		oldProject.setName(newProject.getName());
		oldProject.setDescription(newProject.getDescription());
		
		this.projectService.saveProject(oldProject);
		return "updatedProjectSuccessful";
	}
	
}
