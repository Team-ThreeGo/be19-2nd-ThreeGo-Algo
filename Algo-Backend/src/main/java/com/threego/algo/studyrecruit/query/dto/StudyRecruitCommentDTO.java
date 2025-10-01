package com.threego.algo.studyrecruit.query.dto;
import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecruitCommentDTO {

    private int id;                    // 댓글 ID
    private int postId;                // 게시글 ID
    private Integer parentId;              // 부모 댓글 ID (대댓글인 경우)
    private String content;             // 댓글 내용
    private int memberId;               // 작성자 id
    private String memberNickname;      // 작성자 닉네임
    private String rankName;            // 작성자 등급명
    private String createdAt;           // 작성일
    private String updatedAt;           // 수정일

    // 대댓글 여부 확인
    public boolean isReply() {
        return parentId != null;
    }
}