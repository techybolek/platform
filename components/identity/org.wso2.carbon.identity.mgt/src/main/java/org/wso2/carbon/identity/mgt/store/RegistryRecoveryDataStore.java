package org.wso2.carbon.identity.mgt.store;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class RegistryRecoveryDataStore implements UserRecoveryDataStore{

    @Override
    public void store(UserRecoveryDataDO recoveryDataDO) throws IdentityException {

        try{
            Registry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(recoveryDataDO.getTenantId());
            Resource resource = registry.newResource();
            resource.setProperty(SECRET_KEY, recoveryDataDO.getSecret());
            resource.setProperty(USER_ID, recoveryDataDO.getUserName());
            resource.setProperty(EXPIRE_TIME, recoveryDataDO.getExpireTime());
            resource.setVersionableChange(false);
            String confirmationKeyPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA + "/" + recoveryDataDO.getCode();
            registry.put(confirmationKeyPath, resource);
        } catch (RegistryException e) {
            throw new IdentityException("Error while persisting user recovery data for user : " +
                    recoveryDataDO.getUserName());
        }

    }

    @Override
    public void store(UserRecoveryDataDO[] recoveryDataDOs) throws IdentityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRecoveryDataDO[] load(String userName, int tenantId) throws IdentityException {
        return new UserRecoveryDataDO[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UserRecoveryDataDO load(String userName, int tenantId, String code) throws IdentityException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invalidate(UserRecoveryDataDO recoveryDataDO) throws IdentityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void invalidate(String userId, int tenantId) throws IdentityException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
