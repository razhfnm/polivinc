package com.example.polivinc;

public class MemberProgress {
    private String memberId; // ID del usuario
    private String memberName; // Nombre del usuario
    private int completedTasks;
    private int totalTasks;

    // Constructor vacío necesario para Firebase

    // Constructor completo
    public MemberProgress(String memberId, String Name, int completedTasks, int totalTasks) {
        this.memberId = memberId;
        this.memberName = Name;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
    }

    // Getters y Setters
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String Name) {
        this.memberName = Name;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    // Método para calcular el porcentaje de progreso
    public int getProgressPercentage() {
        if (totalTasks > 0) {
            return (completedTasks * 100) / totalTasks;
        }
        return 0;
    }
}