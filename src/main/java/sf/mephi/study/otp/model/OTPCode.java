package sf.mephi.study.otp.model;

import java.time.LocalDateTime;

public class OTPCode {

    private int id;
    private String operationId;
    private String user;
    private String code;
    private Status status;
    private LocalDateTime createdAt;

    public OTPCode(int id, String operationId, String code, Status status, LocalDateTime createdAt, String user) {
        this.id = id;
        this.operationId = operationId;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "OTPCode{" +
                "id=" + id +
                ", user=" + user +
                ", operationId='" + operationId + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    public enum Status {
        ACTIVE,
        EXPIRED,
        USED
    }
}