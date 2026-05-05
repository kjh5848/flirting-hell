package com.flirtinghell.consultation.adapter.in.web;

import com.flirtinghell.consultation.application.service.RoomService;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@GetMapping
	ApiResponse<RoomService.RoomListResult> listRooms(
			@AuthenticationPrincipal AuthenticatedUser user,
			HttpServletRequest request
	) {
		return ApiResponse.of(roomService.listRooms(user.firebaseUid()), RequestIds.from(request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<CreateRoomResponse> createRoom(
			@AuthenticationPrincipal AuthenticatedUser user,
			@Valid @RequestBody CreateRoomRequest body,
			HttpServletRequest request
	) {
		RoomService.RoomResult room = roomService.createRoom(user.firebaseUid(), body.toCommand());
		return ApiResponse.of(new CreateRoomResponse(room), RequestIds.from(request));
	}

	@GetMapping("/{roomId}")
	ApiResponse<RoomService.RoomDetailResult> getRoom(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable String roomId,
			HttpServletRequest request
	) {
		return ApiResponse.of(roomService.getRoom(user.firebaseUid(), roomId), RequestIds.from(request));
	}

	public record CreateRoomRequest(
			@NotBlank @Size(max = 40) String alias,
			@NotNull RelationshipStage relationshipStage,
			@Size(max = 160) String currentConcern,
			@Size(max = 160) String cautionNotes,
			StrategyId preferredStrategyId
	) {
		RoomService.CreateRoomCommand toCommand() {
			return new RoomService.CreateRoomCommand(
					alias,
					relationshipStage,
					currentConcern,
					cautionNotes,
					preferredStrategyId
			);
		}
	}

	public record CreateRoomResponse(RoomService.RoomResult room) {
	}
}
