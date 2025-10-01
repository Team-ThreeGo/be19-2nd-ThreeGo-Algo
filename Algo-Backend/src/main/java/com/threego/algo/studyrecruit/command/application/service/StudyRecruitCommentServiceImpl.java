package com.threego.algo.studyrecruit.command.application.service;

import com.threego.algo.common.util.DateTimeUtils;
import com.threego.algo.member.command.domain.aggregate.Member;
import com.threego.algo.member.command.domain.aggregate.MemberRole;
import com.threego.algo.member.command.domain.repository.MemberRepository;
import com.threego.algo.member.command.domain.repository.MemberRoleRepository;
import com.threego.algo.studyrecruit.command.application.dto.create.StudyRecruitCommentCreateDTO;
import com.threego.algo.studyrecruit.command.application.dto.update.StudyRecruitCommentUpdateDTO;
import com.threego.algo.studyrecruit.command.domain.aggregate.StudyRecruitComment;
import com.threego.algo.studyrecruit.command.domain.aggregate.StudyRecruitPost;
import com.threego.algo.studyrecruit.command.domain.repository.StudyRecruitCommentRepository;
import com.threego.algo.studyrecruit.command.domain.repository.StudyRecruitPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRecruitCommentServiceImpl implements StudyRecruitCommentService {

    private final StudyRecruitCommentRepository studyRecruitCommentRepository;
    private final StudyRecruitPostRepository studyRecruitPostRepository;
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;

    @Override
    public ResponseEntity<String> createComment(int postId, int memberId, StudyRecruitCommentCreateDTO request) {
        try {
            // 1. 회원 존재 여부 확인
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            // 2. 모집글 존재 여부 확인 (공개상태만)
            StudyRecruitPost studyRecruitPost = studyRecruitPostRepository
                    .findById(postId)
                    .filter(post -> post.getVisibility().equals("Y"))
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 공개되지 않은 모집글입니다."));

            // 3. 대댓글인 경우 부모 댓글 확인
            StudyRecruitComment parentComment = null;
            if (request.getParentId() != null) {
                parentComment =  studyRecruitCommentRepository
                        .findByIdAndVisibility(request.getParentId(), "Y")
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
            }

            // 4. Entity 생성
            StudyRecruitComment comment;
            if (parentComment != null) {
                // 대댓글
                comment = new StudyRecruitComment(
                        studyRecruitPost,
                        member,
                        parentComment,
                        request.getContent(),
                        DateTimeUtils.nowDateTime()
                );
            } else {
                // 일반 댓글
                comment = new StudyRecruitComment(
                        studyRecruitPost,
                        member,
                        request.getContent(),
                        DateTimeUtils.nowDateTime()
                );
            }

            // 5. 저장
            studyRecruitCommentRepository.save(comment);

            // 6. 스터디 모집 게시물 댓글 수 증가
            studyRecruitPost.incrementCommentCount();
            studyRecruitPostRepository.save(studyRecruitPost);

            return ResponseEntity.status(HttpStatus.CREATED).body("댓글이 성공적으로 등록되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 등록 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ResponseEntity<String> updateComment(int commentId, int memberId, StudyRecruitCommentUpdateDTO request) {
        try {
            // 1. 작성자 권한 확인
            StudyRecruitComment comment = studyRecruitCommentRepository
                    .findByIdAndMemberIdAndVisibility(commentId, memberId, "Y")
                    .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없거나 존재하지 않는 댓글입니다."));

            // 2. Entity 업데이트
            comment.setContent(request.getContent());
            comment.setUpdatedAt(DateTimeUtils.nowDateTime());

            // 3. 저장 (더티 체킹으로 자동 업데이트)
            studyRecruitCommentRepository.save(comment);

            return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수정 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ResponseEntity<String> deleteComment(int commentId, int memberId) {
        try {
            // 1. 작성자 권한 확인
            StudyRecruitComment comment = studyRecruitCommentRepository
                    .findByIdAndMemberIdAndVisibility(commentId, memberId, "Y")
                    .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없거나 존재하지 않는 댓글입니다."));

            // 2. Soft Delete 처리
            comment.softDelete();
            comment.setUpdatedAt(DateTimeUtils.nowDateTime());

            // 3. 저장 (더티 체킹으로 자동 업데이트)
            studyRecruitCommentRepository.save(comment);

            // 4. 스터디 모집 게시물 댓글 수 감소
            StudyRecruitPost post = comment.getStudyRecruitPost();
            post.decrementCommentCount();
            studyRecruitPostRepository.save(post);


            return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제 중 오류가 발생했습니다.");
        }
    }

    @Override
    public ResponseEntity<String> adminDeleteComment(int commentId, int adminId) {
        try {
            // 1. 관리자 권한 확인
            if (!isAdmin(adminId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 필요합니다.");
            }

            // 2. 모집 댓글 존재 여부 확인
            StudyRecruitComment comment = studyRecruitCommentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("모집 댓글을 찾을 수 없습니다."));

            // 3. 이미 삭제된 댓글인지 확인
            if (comment.getVisibility().equals("N")) {
                return ResponseEntity.badRequest().body("이미 삭제된 댓글입니다.");
            }

            // 4. 소프트 딜리트 (VISIBILITY: Y → N)
            comment.softDelete();
            studyRecruitCommentRepository.save(comment);

            return ResponseEntity.ok("모집 댓글이 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("모집 댓글 삭제 중 오류가 발생했습니다.");
        }
    }

    private boolean isAdmin(int memberId) {
        try {
            int roleId = memberRoleRepository.getRoleIdByMemberId(memberId);
            return roleId == 2;
        } catch (Exception e) {
            return false;
        }
    }
}