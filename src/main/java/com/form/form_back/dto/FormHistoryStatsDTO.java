package com.form.form_back.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormHistoryStatsDTO {
    private int totalActions;
    private int todayActions;
    private int weekActions;
    private int monthActions;
    private List<ActionTypeStatsDTO> actionTypeStats;
    private List<SecteurStatsDTO> secteurStats;
    private List<UserActivityDTO> topActiveUsers;
    private List<FormActivityDTO> mostActiveforms;

    public int getTotalActions() {
        return totalActions;
    }

    public void setTotalActions(int totalActions) {
        this.totalActions = totalActions;
    }

    public int getTodayActions() {
        return todayActions;
    }

    public void setTodayActions(int todayActions) {
        this.todayActions = todayActions;
    }

    public int getWeekActions() {
        return weekActions;
    }

    public void setWeekActions(int weekActions) {
        this.weekActions = weekActions;
    }

    public int getMonthActions() {
        return monthActions;
    }

    public void setMonthActions(int monthActions) {
        this.monthActions = monthActions;
    }

    public List<ActionTypeStatsDTO> getActionTypeStats() {
        return actionTypeStats;
    }

    public void setActionTypeStats(List<ActionTypeStatsDTO> actionTypeStats) {
        this.actionTypeStats = actionTypeStats;
    }

    public List<SecteurStatsDTO> getSecteurStats() {
        return secteurStats;
    }

    public void setSecteurStats(List<SecteurStatsDTO> secteurStats) {
        this.secteurStats = secteurStats;
    }

    public List<UserActivityDTO> getTopActiveUsers() {
        return topActiveUsers;
    }

    public void setTopActiveUsers(List<UserActivityDTO> topActiveUsers) {
        this.topActiveUsers = topActiveUsers;
    }

    public List<FormActivityDTO> getMostActiveforms() {
        return mostActiveforms;
    }

    public void setMostActiveforms(List<FormActivityDTO> mostActiveforms) {
        this.mostActiveforms = mostActiveforms;
    }
}