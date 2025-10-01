package com.threego.algo.coding.query.controller;

import com.threego.algo.coding.query.dto.CodingProblemDetailDTO;
import com.threego.algo.coding.query.dto.CodingProblemSummaryDTO;
import com.threego.algo.coding.query.service.CodingProblemQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CodingProblem (Admin)", description = "관리자용 코딩문제 API")
@RequestMapping("/admin/coding-problem")
@RestController
@RequiredArgsConstructor
public class AdminCodingProblemQueryController {

    private final CodingProblemQueryService codingProblemQueryService;

    // 관리자용 알고리즘 문제 게시물 목록 조회
    // GET /admin/coding-problem/posts
    @Operation(
            summary = "관리자용 알고리즘 문제 게시물 목록 조회",
            description = "관리자가 자신의 관리자용 알고리즘 문제 게시물 목록 조회합니다."
    )
    @GetMapping("/posts")
    public List<CodingProblemSummaryDTO> findProblemsListForAdmin(
            @RequestParam(value = "visibility", required = false) String visibility, // Y/N/ALL
            @RequestParam(value = "keyword", required = false) String keyword) {
        return codingProblemQueryService.findProblemsListForAdmin(visibility, keyword);
    }

    // 관리자용 알고리즘 문제 게시물 상세 조회
    // GET /admin/codingproblem/posts/{postId}
    @Operation(
            summary = "관리자용 알고리즘 문제 게시물 상세 조회",
            description = "관리자가 자신의 관리자용 알고리즘 문제 게시물 상세 조회합니다."
    )
    @GetMapping("/problems/{postId}")
    public CodingProblemDetailDTO findProblemDetailForAdmin(@PathVariable int postId) {
        return codingProblemQueryService.findProblemDetailForAdmin(postId);
    }
}
