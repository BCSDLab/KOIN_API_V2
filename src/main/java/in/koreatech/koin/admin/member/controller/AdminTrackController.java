package in.koreatech.koin.admin.member.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.TECH_STACKS;
import static in.koreatech.koin.admin.history.enums.DomainType.TRACKS;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.member.dto.AdminTechStackRequest;
import in.koreatech.koin.admin.member.dto.AdminTechStackResponse;
import in.koreatech.koin.admin.member.dto.AdminTrackRequest;
import in.koreatech.koin.admin.member.dto.AdminTrackResponse;
import in.koreatech.koin.admin.member.dto.AdminTrackSingleResponse;
import in.koreatech.koin.admin.member.service.AdminTrackService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminTrackController implements AdminTrackApi {

    private final AdminTrackService adminTrackService;

    @GetMapping("/admin/tracks")
    public ResponseEntity<List<AdminTrackResponse>> getTracks(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        var response = adminTrackService.getTracks();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/tracks")
    @AdminActivityLogging(domain = TRACKS)
    public ResponseEntity<AdminTrackResponse> createTrack(
        @RequestBody @Valid AdminTrackRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        var response = adminTrackService.createTrack(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/tracks/{id}")
    public ResponseEntity<AdminTrackSingleResponse> getTrack(
        @PathVariable("id") Integer trackId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        var response = adminTrackService.getTrack(trackId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/tracks/{id}")
    @AdminActivityLogging(domain = TRACKS, domainIdParam = "trackId")
    public ResponseEntity<AdminTrackResponse> updateTrack(
        @PathVariable("id") Integer trackId,
        @RequestBody @Valid AdminTrackRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        var response = adminTrackService.updateTrack(trackId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/tracks/{id}")
    @AdminActivityLogging(domain = TRACKS, domainIdParam = "trackId")
    public ResponseEntity<Void> deleteTrack(
        @PathVariable("id") Integer trackId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminTrackService.deleteTrack(trackId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/techStacks")
    @AdminActivityLogging(domain = TECH_STACKS)
    public ResponseEntity<AdminTechStackResponse> createTechStack(
        @RequestBody @Valid AdminTechStackRequest request,
        @RequestParam String trackName,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        var response = adminTrackService.createTechStack(request, trackName);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/techStacks/{id}")
    @AdminActivityLogging(domain = TECH_STACKS, domainIdParam = "techStackId")
    public ResponseEntity<AdminTechStackResponse> updateTechStack(
        @RequestBody @Valid AdminTechStackRequest request,
        @RequestParam String trackName,
        @PathVariable("id") Integer techStackId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        var response = adminTrackService.updateTechStack(request, trackName, techStackId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/techStacks/{id}")
    @AdminActivityLogging(domain = TECH_STACKS, domainIdParam = "techStackId")
    public ResponseEntity<Void> deleteTechStack(
        @PathVariable("id") Integer techStackId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminTrackService.deleteTechStack(techStackId);
        return ResponseEntity.ok().build();
    }
}
