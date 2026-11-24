package oi.lebentes.controllers;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import oi.lebentes.domain.Comment;
import oi.lebentes.domain.CrcCard;
import oi.lebentes.domain.UseCase;
import oi.lebentes.domain.User;
import oi.lebentes.services.CommentService;
import oi.lebentes.services.CrcCardService;
import oi.lebentes.services.ProjectService;
import oi.lebentes.services.UseCaseService;
import oi.lebentes.services.UserService;

@Controller
public class CommentsController {

	private CommentService commentService;
	private ProjectService projectService;
	private UserService userService;
	private UseCaseService useCaseService;
	private CrcCardService crcCardService;

	public CommentsController(CommentService commentService,
			ProjectService projectService,
			UserService userService,
			UseCaseService useCaseService,
			CrcCardService crcCardService) {
		this.commentService = commentService;
		this.projectService = projectService;
		this.userService = userService;
		this.useCaseService = useCaseService;
		this.crcCardService = crcCardService;
	}

	@GetMapping("/projects/{projectId}/usecases/{useCaseId}/comments")
	public String useCaseComments(@PathVariable Integer projectId,
			@PathVariable Integer useCaseId,
			Model model,
			Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new IllegalArgumentException("No access to project");
		}

		UseCase uc = useCaseService.get(projectId, useCaseId);
		List<Comment> comments = commentService.list(projectId, "USECASE", useCaseId);

		model.addAttribute("projectId", projectId);
		model.addAttribute("useCase", uc);
		model.addAttribute("currentUserId", me.getId());
		model.addAttribute("comments", comments);
		model.addAttribute("form", new CommentForm());
		return "usecase_comments";
	}

	@PostMapping("/projects/{projectId}/usecases/{useCaseId}/comments")
	public String addUseCaseComment(@PathVariable Integer projectId,@PathVariable Integer useCaseId,@ModelAttribute CommentForm form,Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new IllegalArgumentException("No access to project");
		}

		commentService.add(projectId, "USECASE", useCaseId, me.getId(), me.getName(), form.getText());
		return "redirect:/projects/" + projectId + "/usecases/" + useCaseId + "/comments";
	}

	@GetMapping("/projects/{projectId}/crccards/{crcCardId}/comments")
	public String crcCardComments(@PathVariable Integer projectId,@PathVariable Integer crcCardId,Model model,Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new IllegalArgumentException("No access to project");
		}

		CrcCard card = crcCardService.get(projectId, crcCardId);
		List<Comment> comments = commentService.list(projectId, "CRC", crcCardId);

		model.addAttribute("projectId", projectId);
		model.addAttribute("crcCardId", crcCardId);
		model.addAttribute("crcCard", card);
		model.addAttribute("currentUserId", me.getId());
		model.addAttribute("comments", comments);
		model.addAttribute("form", new CommentForm());
		return "crccard_comments";
	}

	@PostMapping("/projects/{projectId}/crccards/{crcCardId}/comments")
	public String addCrcCardComment(@PathVariable Integer projectId,@PathVariable Integer crcCardId,@ModelAttribute CommentForm form,Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new IllegalArgumentException("No access to project");
		}

		commentService.add(projectId, "CRC", crcCardId, me.getId(), me.getName(), form.getText());
		return "redirect:/projects/" + projectId + "/crccards/" + crcCardId + "/comments";
	}

	@GetMapping("/projects/{projectId}/comments/{commentId}/edit")
	public String editComment(@PathVariable Integer projectId,
			@PathVariable Integer commentId,
			Model model,
			Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new AccessDeniedException("No access to project");
		}

		Comment comment = commentService.getOwned(projectId, commentId, me.getId());

		CommentForm form = new CommentForm();
		form.setText(comment.getText());

		model.addAttribute("projectId", projectId);
		model.addAttribute("commentId", commentId);
		model.addAttribute("form", form);
		model.addAttribute("backUrl", commentPage(comment));

		return "comment_edit";
	}

	@PostMapping("/projects/{projectId}/comments/{commentId}/edit")
	public String updateComment(@PathVariable Integer projectId,
			@PathVariable Integer commentId,
			@ModelAttribute CommentForm form,
			Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new AccessDeniedException("No access to project");
		}

		Comment comment = commentService.update(projectId, commentId, me.getId(), form.getText());

		return "redirect:" + commentPage(comment);
	}

	@PostMapping("/projects/{projectId}/comments/{commentId}/delete")
	public String deleteComment(@PathVariable Integer projectId,
			@PathVariable Integer commentId,
			Authentication auth) {

		User me = userService.getByEmail(auth.getName());
		if (!projectService.canAccess(projectId, me.getId())) {
			throw new AccessDeniedException("No access to project");
		}

		Comment comment = commentService.delete(projectId, commentId, me.getId());

		return "redirect:" + commentPage(comment);
	}

	private String commentPage(Comment comment) {
		if ("USECASE".equals(comment.getTargetType())) {
			return "/projects/" + comment.getProjectId() + "/usecases/" + comment.getTargetId() + "/comments";
		}

		if ("CRC".equals(comment.getTargetType())) {
			return "/projects/" + comment.getProjectId() + "/crccards/" + comment.getTargetId() + "/comments";
		}

		throw new IllegalArgumentException("Invalid comment target");
	}
}
