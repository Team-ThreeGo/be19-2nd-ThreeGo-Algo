package com.threego.algo.study.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyRoadmapDetailDTO {
    private int roadmapId;
    private String roadmapTitle;
    private String roadmapDescription;
    private List<MilestoneInfo> milestones;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneInfo {
        private int milestoneId;
        private String milestoneTitle;
        private String milestoneDescription;
    }
}