package org.wso2.carbon.identity.mgt.dto;

/**
 * This object contains the information of the created user account. This
 * information can be sent to the user to complete the user registration
 * process. Information are such as the temporary password, confirmation code
 * etc
 * 
 * @author sga
 * 
 */
public class UserRecoveryDTO {

	private String userId;
    private String tenantDomain;
	private String temporaryPassword;
	private String confirmationCode;
	private String notificationType;
	private String recoveryType;

	public UserRecoveryDTO(String userName) {
		this.userId = userName;
	}

	/**
	 * Returns the temporary password of the created account
	 * @return
	 */
	public String getTemporaryPassword() {
		return temporaryPassword;
	}

	public UserRecoveryDTO setTemporaryPassword(String temporaryPassword) {
		this.temporaryPassword = temporaryPassword;
		return this;
	}

	/**
	 * Returns the confirmation code for the created account
	 * @return
	 */
	public String getConfirmationCode() {
		return confirmationCode;
	}

	public UserRecoveryDTO setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
		return this;
	}

	public String getUserId() {
	    return userId;
    }

	public UserRecoveryDTO setUserId(String userId) {
	    this.userId = userId;
	    return this;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getRecoveryType() {
        return recoveryType;
    }

    public void setRecoveryType(String recoveryType) {
        this.recoveryType = recoveryType;
    }
}
