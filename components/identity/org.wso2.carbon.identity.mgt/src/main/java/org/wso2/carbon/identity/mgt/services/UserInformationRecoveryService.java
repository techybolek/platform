/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.ChallengeQuestionProcessor;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.RecoveryProcessor;
import org.wso2.carbon.identity.mgt.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.dto.NotificationDataDTO;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.util.Utils;

/**
 * This service provides the services needed to recover user password and user
 * account information.
 * 
 */
public class UserInformationRecoveryService {

	Log log = LogFactory.getLog(UserInformationRecoveryService.class);

	public CaptchaInfoBean getCaptcha() throws IdentityMgtServiceException {

		try {
			CaptchaUtil.cleanOldCaptchas();
			CaptchaInfoBean bean = CaptchaUtil.generateCaptchaImage();

			return bean;

		} catch (Exception e) {
			log.debug("Error while generating captcha", e);
			throw new IdentityMgtServiceException("Error while generating captcha", e);
		}
	}

	public VerificationBean verifyUser(String username, CaptchaInfoBean captcha)
			throws IdentityMgtServiceException {

		UserDTO userDTO;
		VerificationBean bean = new VerificationBean();

		if (IdentityMgtConfig.getInstance().isCaptchaVerificationInternallyManaged()) {
			try {
				CaptchaUtil.processCaptchaInfoBean(captcha);
			} catch (Exception e) {
				log.debug(e.getMessage());
				bean.setError(VerificationBean.ERROR_CODE_INVALID_CAPTCHA);
				bean.setVerified(false);
				return bean;
			}
		}

		try {
			userDTO = Utils.processUserId(username);
		} catch (IdentityException e) {
			log.debug(e.getMessage());
			bean.setError(VerificationBean.ERROR_CODE_INVALID_USER);
			bean.setVerified(false);
			return bean;
		}

		RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

		return processor.verifyUserForRecovery(userDTO);
	}

	public boolean sendRecoveryNotification(String username, String key, String notificationType)
			throws IdentityMgtServiceException {

		UserDTO userDTO = null;

		try {
			userDTO = Utils.processUserId(username);
		} catch (IdentityException e) {
			throw new IdentityMgtServiceException("Invalid user name");
		}

		RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

		VerificationBean bean = processor.verifyConfirmationKey(key);

		if (!bean.isVerified()) {
			log.warn("Invalid user is trying to recover the password : " + username);
			return false;
		}

		UserRecoveryDTO dto = new UserRecoveryDTO(userDTO);
		dto.setNotification(IdentityMgtConstants.Notification.PASSWORD_RESET_RECOVERY);
		dto.setNotificationType(notificationType);

		NotificationDataDTO dataDTO = null;
		try {
			dataDTO = processor.recoverWithNotification(dto);
		} catch (IdentityException e) {
			throw new IdentityMgtServiceException("Error while password recovery");
		}
		return dataDTO.isNotificationSent();
	}

	/**
	 * This method is used to verify the confirmation code sent to user is
	 * correct and validates. Before calling this method it needs to supply a
	 * Captcha and should call getCaptcha().
	 * 
	 * @param username
	 *            - username of whom the password needs to be recovered.
	 * @param code
	 *            - confirmation code sent to user by notification.
	 * @param captcha
	 *            - generated captcha with answer for this communication.
	 * 
	 * @return - VerificationBean with new code to be used in updatePassword().
	 * 
	 * @throws IdentityMgtServiceException
	 */
	public VerificationBean verifyConfirmationCode(String username, String code,
			CaptchaInfoBean captcha) throws IdentityMgtServiceException {

		UserDTO userDTO;
		VerificationBean bean = new VerificationBean();

		if (IdentityMgtConfig.getInstance().isCaptchaVerificationInternallyManaged()) {
			try {
				CaptchaUtil.processCaptchaInfoBean(captcha);
			} catch (Exception e) {
				log.debug(e.getMessage());
				bean.setError(VerificationBean.ERROR_CODE_INVALID_CAPTCHA);
				bean.setVerified(false);
				return bean;
			}
		}

		try {
			userDTO = Utils.processUserId(username);
		} catch (IdentityException e) {
			log.debug(e.getMessage());
			bean.setError(VerificationBean.ERROR_CODE_INVALID_USER);
			bean.setVerified(false);
			return bean;
		}

		RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

		try {
			bean = processor.verifyConfirmationCode(code, userDTO);
		} catch (IdentityException e) {
			throw new IdentityMgtServiceException("Error while validating given confirmation code");
		}

		return bean;

	}

	/**
	 * This method is used to update the password in the system for password
	 * recovery process. Before calling this method caller needs to call
	 * verifyConfirmationCode and get the newly generated confirmation code.
	 * 
	 * @param username
	 *            - username
	 * @param confirmationCode
	 *            - newly generated confirmation code
	 * @param newPassword
	 *            - new password
	 * 
	 * @return - VerificationBean with operation status true or false.
	 * 
	 * @throws IdentityMgtServiceException
	 */
	public VerificationBean updatePassword(String username, String confirmationCode,
			String newPassword) throws IdentityMgtServiceException {

		RecoveryProcessor recoveryProcessor = IdentityMgtServiceComponent.getRecoveryProcessor();

		try {
			UserDTO userDTO = Utils.processUserId(username);
			if (recoveryProcessor.verifyConfirmationKey(confirmationCode).isVerified()) {
				Utils.updatePassword(userDTO.getUserId(), userDTO.getTenantId(), newPassword);
				log.info("Credential is updated for user : " + userDTO.getUserId()
						+ " and tenant domain : " + userDTO.getTenantDomain());
				return new VerificationBean(true);
			} else {
				new VerificationBean(VerificationBean.ERROR_CODE_UN_EXPECTED);
				log.warn("Invalid user tried to update credential with user Id : "
						+ userDTO.getUserId() + " and tenant domain : " + userDTO.getTenantDomain());
			}

		} catch (Exception e) {
			log.debug("Error while updating credential for user : " + username, e);
		}
		return new VerificationBean(VerificationBean.ERROR_CODE_UN_EXPECTED);
	}

	public ChallengeQuestionIdsDTO getUserChallengeQuestionIds(String username, String confirmation)
			throws IdentityMgtServiceException {

		UserDTO userDTO = null;
		ChallengeQuestionIdsDTO idsDTO = new ChallengeQuestionIdsDTO();

		try {
			userDTO = Utils.processUserId(username);
		} catch (IdentityException e) {
			log.debug(e.getMessage());
			idsDTO.setError(e.getMessage());
			return idsDTO;
		}

		RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

		VerificationBean bean = null;
		try {
			bean = processor.verifyConfirmationCode(confirmation, userDTO);
		} catch (IdentityException e1) {
			log.debug(e1.getMessage());
			idsDTO.setError(e1.getMessage());
			return idsDTO;
		}
		if (bean.isVerified()) {
			idsDTO = processor.getQuestionProcessor().getUserChallengeQuestionIds(
					userDTO.getUserId(), userDTO.getTenantId());
			idsDTO.setKey(bean.getKey());
		} else {
			log.debug("Verfication failed for user. Error : " + bean.getError());
			idsDTO.setError(bean.getError());
		}

		return idsDTO;

	}

	/**
	 * To get the challenge question for the user.
	 * 
	 * @param userName
	 * @param confirmation
	 * @param questionId
	 *            - Question id returned from the getUserChanllegneQuestionIds
	 *            method.
	 * 
	 * @return Populated question bean with the question details and the key.
	 * @throws IdentityMgtServiceException
	 */
	public UserChallengesDTO getUserChallengeQuestion(String userName, String confirmation,
			String questionId) throws IdentityMgtServiceException {

		UserDTO userDTO = null;
		UserChallengesDTO userChallengesDTO = new UserChallengesDTO();

		try {
			userDTO = Utils.processUserId(userName);
		} catch (IdentityException e) {
			log.debug(e.getMessage());
			userChallengesDTO.setError(e.getMessage());
			return userChallengesDTO;
		}

		RecoveryProcessor processor = IdentityMgtServiceComponent.getRecoveryProcessor();

		VerificationBean bean;
		try {
			bean = processor.verifyConfirmationCode(confirmation, userDTO);
		} catch (IdentityException e1) {
			log.debug(e1.getMessage());
			userChallengesDTO.setError(e1.getMessage());
			return userChallengesDTO;
		}

		if (bean.isVerified()) {
			userChallengesDTO = processor.getQuestionProcessor().getUserChallengeQuestion(
					userDTO.getUserId(), userDTO.getTenantId(), questionId);
			userChallengesDTO.setKey(bean.getKey());
		} else {
			log.debug("Verificaton failed for user. Error : " + bean.getError());
			userChallengesDTO.setError(bean.getError());
		}

		return userChallengesDTO;
	}

	/**
	 * This method is to verify the user supplied answer for the challenge
	 * question.
	 * 
	 * @param userName
	 * @param confirmation
	 * @param questionId
	 * @param answer
	 * 
	 * @return status and key details about the operation status.
	 * @throws IdentityMgtServiceException
	 */
	public VerificationBean verifyUserChallengeAnswer(String userName, String confirmation,
			String questionId, String answer) throws IdentityMgtServiceException {

		VerificationBean bean = new VerificationBean();
		bean.setVerified(false);

		if (questionId != null && answer != null) {

		} else {
			String error = "No challenge question id provided for verification";
			bean.setError(error);
			log.debug(error);
			return bean;
		}

		UserDTO userDTO = null;
		try {
			userDTO = Utils.processUserId(userName);
		} catch (IdentityException e) {
			log.debug(e.getMessage());
			bean.setError(e.getMessage());
			return bean;
		}

		RecoveryProcessor recoveryProcessor = IdentityMgtServiceComponent.getRecoveryProcessor();

		try {
			bean = recoveryProcessor.verifyConfirmationCode(confirmation, userDTO);
		} catch (IdentityException e1) {
			log.debug(e1.getMessage());
			bean.setError(e1.getMessage());
			return bean;
		}

		ChallengeQuestionProcessor processor = recoveryProcessor.getQuestionProcessor();

		UserChallengesDTO userChallengesDTO = new UserChallengesDTO();
		userChallengesDTO.setId(questionId);
		userChallengesDTO.setAnswer(answer);

		boolean verification = processor.verifyUserChallengeAnswer(userDTO.getUserId(),
				userDTO.getTenantId(), userChallengesDTO);

		if (verification) {
			bean.setError("");
			bean.setUserId(userName);

		} else {
			bean.setError("Answer verification failed for user: " + userName);
			bean.setKey(""); // clear the key to avoid returning to caller.
			log.debug("Answer verification failed for user: " + userName);
		}

		return bean;
	}
}
