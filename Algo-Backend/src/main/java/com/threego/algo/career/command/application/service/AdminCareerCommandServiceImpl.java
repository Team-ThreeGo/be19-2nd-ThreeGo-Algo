package com.threego.algo.career.command.application.service;

import com.threego.algo.career.command.domain.aggregate.CareerInfoComment;
import com.threego.algo.career.command.domain.aggregate.CareerInfoPost;
import com.threego.algo.career.command.domain.aggregate.enums.Status;
import com.threego.algo.career.command.domain.repository.CareerCommentRepository;
import com.threego.algo.career.command.domain.repository.CareerPostRepository;
import com.threego.algo.member.command.domain.aggregate.Member;
import com.threego.algo.member.command.domain.repository.MemberCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCareerCommandServiceImpl implements AdminCareerCommandService{
    private final CareerPostRepository careerPostRepository;
    private final CareerCommentRepository careerCommentRepository;
    private final MemberCommandRepository memberRepository;

    @Autowired
    public AdminCareerCommandServiceImpl(CareerPostRepository careerPostRepository, CareerCommentRepository careerCommentRepository, MemberCommandRepository memberRepository) {
        this.careerPostRepository = careerPostRepository;
        this.careerCommentRepository = careerCommentRepository;
        this.memberRepository = memberRepository;
    }


    @Transactional
    @Override
    public void updatePostStatus(int postId, Status status, String rejectReason) {
        CareerInfoPost post = careerPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        // TODO: 로그인한 회원 == post.getMember() 확인 (관리자만 수행 가능)
        Member member = memberRepository.findById(29)
                .orElseThrow(() -> new IllegalArgumentException("테스트용 관리자가 없습니다."));

        post.setStatus(status);
        if (status == Status.REJECTED) {
            post.setRejectReason(rejectReason);
        }
    }

    @Transactional
    @Override
    public void deletePost(int postId) {
        CareerInfoPost post = careerPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));
        post.delete();
    }

    @Transactional
    @Override
    public void deleteComment(int commentId) {
        CareerInfoComment comment = careerCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if ("N".equals(comment.getVisibility())) {
            throw new IllegalStateException("이미 삭제된 댓글입니다.");
        }

        comment.deleteComment();
        comment.getPost().decreaseCommentCount();
    }
}
