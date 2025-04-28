package sf.mephi.study.otp.model;

public class OTPConfig {

    private int id;
    private int codeLength;
    private int expirationTime; // в секундах

    public OTPConfig(int id, int codeLength, int expirationTime) {
        this.id = id;
        this.codeLength = codeLength;
        this.expirationTime = expirationTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String toString() {
        return "OTPConfig{" +
                "id=" + id +
                ", codeLength=" + codeLength +
                ", expirationTime=" + expirationTime +
                '}';
    }
}
