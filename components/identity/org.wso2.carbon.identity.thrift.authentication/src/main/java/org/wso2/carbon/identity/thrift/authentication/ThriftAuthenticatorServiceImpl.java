/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.thrift.authentication;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.caching.core.CacheInvalidator;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.thrift.authentication.dao.ThriftSessionDAO;
import org.wso2.carbon.identity.thrift.authentication.internal.ThriftAuthenticationServiceComponent;
import org.wso2.carbon.identity.thrift.authentication.util.ThriftAuthenticationConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ThriftSession;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a utility class that performs authentication related functionalities
 * by talking to back end authentication service.
 */
public class ThriftAuthenticatorServiceImpl extends AbstractAdmin
        implements ThriftAuthenticatorService {

    private static final Log log = LogFactory.getLog(ThriftAuthenticatorServiceImpl.class);

    private static ThriftAuthenticatorServiceImpl instance = null;
    //session timeout in milli seconds
    private static long thriftSessionTimeOut = 60000*30;
    private AuthenticationService authenticationService;
    private RealmService realmService;

    private Map<String, ThriftSession> authenticatedSessions =
            new ConcurrentHashMap<String, ThriftSession>();

   
    public static ThriftAuthenticatorServiceImpl getInstance() {
        if(instance == null){
            synchronized (ThriftAuthenticatorServiceImpl.class){
                if(instance == null){
                    instance = new ThriftAuthenticatorServiceImpl();
                    return instance;
                } else {
                    return instance;
                }
            }
        }
        return instance;
    }

    private void addThriftSession(ThriftSession thriftSession) throws IdentityException {
        //add to cache
        authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
        //add to database
        ThriftSessionDAO thriftSessionDAO = new ThriftSessionDAO();
        thriftSessionDAO.addSession(thriftSession);
    }

    private void removeThriftSession(String thriftSessionId) throws IdentityException {
        //remove from cache
        //thriftSessionCache.remove(thriftSessionId);
        authenticatedSessions.remove(thriftSessionId);
        //remove from db
        ThriftSessionDAO thriftSessionDAO = new ThriftSessionDAO();
        thriftSessionDAO.removeSession(thriftSessionId);
    }

    /*initialize the org.wso2.carbon.identity.authentication.AuthenticationService which is wrapped
    by this ThriftAuthenticationService.*/

    public void init(AuthenticationService authenticationService, RealmService realmService) {
        this.authenticationService = authenticationService;
        this.realmService = realmService;
    }
    //TODO: get remote address as an input to be used for logging purposes in OnSuccessAdminLogin method. 

    public String authenticate(String userName, String password) throws AuthenticationException {

        if (userName == null) {
            logAndAuthenticationException("Authentication request was missing the user name ");
        }

        if (userName.indexOf("@") > 0) {
            String domainName = userName.substring(userName.indexOf("@") + 1);
            if (domainName == null || domainName.trim().equals("")) {
                logAndAuthenticationException("Authentication request was missing the domain name of" +
                                              " the user");
            }
        }

        if (password == null) {
            logAndAuthenticationException("Authentication request was missing the required password");
        }
        //check whether the credentials are authenticated.
        boolean isSuccessful = authenticationService.authenticate(userName, password);

        if (log.isDebugEnabled()) {
            if (isSuccessful) {
                log.debug("User: " + userName + " was successfully authenticated..");
            } else {
                log.debug("Authentication failed for user: " + userName + " Hence, returning null for session id.");
            }
        }

        if (isSuccessful) {
            
            //if not, create a new session
            String sessionId = null;
            ThriftSession session = null;
            try {
                sessionId = UUID.randomUUID().toString();
                //populate thrift session
                session = new ThriftSession();
                session.setSessionId(sessionId);
                session.setUserName(userName);
                session.setCreatedAt(System.currentTimeMillis());
                session.setLastAccess(System.currentTimeMillis());
                
                callOnSuccessAdminLogin(session);
                addThriftSession(session);

            } catch (Exception e) {
                String errorMsg = "Error occured while authenticating the user: " + userName;
                log.error(errorMsg, e);
                throw new AuthenticationException(errorMsg);
            }
            return sessionId;
        } else {
            //TODO:call onFailedLogin: just for logging purposes
        }

        return null;

    }

    public boolean isAuthenticated(String sessionId) {

        if (sessionId == null) {
            return false;
        }
        //if cache empty, try to populate from db
        if (authenticatedSessions.isEmpty()) {
            try {
                populateSessionsFromDB();
            } catch (IdentityException e) {
                String error = "Error while populating thrift sessions from cache";
                log.error(error, e);
            } catch (Exception e) {
                String error = "Error while populating thrift sessions from cache";
                log.error(error, e);
            }
        }
        //if cache not empty, check if session id existing and valid, if so, update last access time and return it.
        if (!authenticatedSessions.isEmpty()) {
            ThriftSessionDAO thriftSessionDAO = new ThriftSessionDAO();
            if (authenticatedSessions.containsKey(sessionId)) {
                ThriftSession thriftSessionInCache = authenticatedSessions.get(sessionId);
                if (isSessionValid(thriftSessionInCache)) {
                    //update the last access time in cache and d
                    long lastAccessTime = System.currentTimeMillis();
                    (authenticatedSessions.get(sessionId)).setLastAccess(lastAccessTime);
                    try {
                        //if carbon context in the thrift session is not initialized, should do that now.
                        if ((thriftSessionInCache.getAttribute(MultitenantConstants.TENANT_DOMAIN)) == null) {
                            callOnSuccessAdminLogin(thriftSessionInCache);
                        }
                        //put the thrift session filled with carbon context info
                        authenticatedSessions.put(sessionId, thriftSessionInCache);
                        thriftSessionDAO.updateLastAccessTime(sessionId,lastAccessTime);
                    } catch (IdentityException e) {
                        String error = "Error while updating last access time in DB";
                        log.error(error, e);
                    } catch (Exception e) {
                        String error = "Error in calling on success admin login for the thrift session.";
                        log.error(error, e);
                    }
                    return true;
                } else {
                    //if not valid in cache, check if valid in db
                    try {
                        ThriftSession thriftSession = thriftSessionDAO.getSession(sessionId);
                        if (isSessionValid(thriftSession)) {
                            //update cache and return true
                            thriftSession.setLastAccess(System.currentTimeMillis());
                            if (thriftSession.getAttribute(MultitenantConstants.TENANT_DOMAIN) == null) {
                                callOnSuccessAdminLogin(thriftSession);
                            }
                            authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
                            thriftSessionDAO.updateLastAccessTime(sessionId, thriftSession.getLastAccess());
                            return true;
                        } else {
                            //remove from cache and db and return false
                            removeThriftSession(sessionId);
                            return false;
                        }
                    } catch (IdentityException e) {
                        String error = "Error while obtaining thrift session from database.";
                        log.error(error, e);
                    } catch (Exception e) {
                        String error = "Error in calling on success admin login for the thrift session.";
                        log.error(error, e);
                    }
                }
            } else {
                //if session id not found, check in db as well, if exist in db, populate cache
                try {
                    if (thriftSessionDAO.isSessionExisting(sessionId)) {
                        ThriftSession thriftSession = thriftSessionDAO.getSession(sessionId);
                        if (isSessionValid(thriftSession)) {
                            thriftSession.setLastAccess(System.currentTimeMillis());
                            if (thriftSession.getAttribute(MultitenantConstants.TENANT_DOMAIN) == null) {
                                callOnSuccessAdminLogin(thriftSession);
                            }
                            authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
                            thriftSessionDAO.updateLastAccessTime(sessionId, thriftSession.getLastAccess());
                            return true;
                        } else {
                            thriftSessionDAO.removeSession(sessionId);
                            return false;
                        }
                    }
                } catch (IdentityException e) {
                    String error = "Error while obtaining thrift session from database.";
                    log.error(error, e);
                } catch (Exception e) {
                    String error = "Error in calling on success admin login for the thrift session obtained from DB.";
                    log.error(error, e);
                }
            }
        }

        return false;
    }

    public ThriftSession getSessionInfo(String sessionId) {
        return authenticatedSessions.get(sessionId);
    }

    private void logAndAuthenticationException(String msg) throws AuthenticationException {
        log.error(msg);
        throw new AuthenticationException(msg);
    }

    /**
     * Perform session invalidation to avoid replay attacks.
     */
    /*public class SessionInvalidator implements Runnable {

        public void run() {
            while (true) {
                try {
                    for (ThriftSession thriftSession : authenticatedSessions.values()) {
                        long currentTime = System.currentTimeMillis();
                        long createdTime = thriftSession.getCreatedAt();
                        if ((currentTime - createdTime) > 50000) {
                            authenticatedSessions.remove(thriftSession.getSessionId());
                        }
                    }
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
    private boolean isSessionValid(ThriftSession thriftSession) {
        //check whether the session is expired.
        return ((System.currentTimeMillis() - thriftSession.getLastAccess()) < thriftSessionTimeOut);
    }

    private void populateSessionsFromDB() throws Exception {
        //first clear the cache
        if (!authenticatedSessions.isEmpty()) {
            authenticatedSessions.clear();
        }
        //get all sessions from db
        ThriftSessionDAO thriftSessionDAO = new ThriftSessionDAO();
        List<ThriftSession> thriftSessions = thriftSessionDAO.getAllSessions();
        //add to cache
        if (thriftSessions != null && thriftSessions.size() != 0) {
            for (ThriftSession thriftSession : thriftSessions) {
                //callOnSuccessAdminLogin(thriftSession);
                authenticatedSessions.put(thriftSession.getSessionId(), thriftSession);
            }
        }
    }

    private boolean isSessionExistInDB(String sessionId) throws IdentityException {
        ThriftSessionDAO thriftSessionDAO = new ThriftSessionDAO();
        return thriftSessionDAO.isSessionExisting(sessionId);
    }

    private void callOnSuccessAdminLogin(ThriftSession session) throws Exception {
        if (realmService != null) {
            String tenantDomain = MultitenantUtils.getTenantDomain(session.getUserName());
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            CarbonAuthenticationUtil.onSuccessAdminLogin(session, session.getUserName(), tenantId,
                                                         tenantDomain, "");
        } else {
            String errorMsg = "Realm service not properly set..";
            log.error(errorMsg);
            throw new AuthenticationException(errorMsg);
        }
    }
}
