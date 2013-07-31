package org.wso2.carbon.appfactory;


/**
 * User service for appfactory using OT userstore
 */

import com.gitblit.GitBlit;
import com.gitblit.GitblitUserService;
import com.gitblit.IStoredSettings;
import com.gitblit.models.TeamModel;
import com.gitblit.models.UserModel;
import com.gitblit.utils.ArrayUtils;
import com.gitblit.utils.StringUtils;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Implementation of an LDAP user service.
 *
 * @author John Crygier
 */
public class AppFactoryGitblitLDAPUserService extends GitblitUserService {

    public static final Logger logger = LoggerFactory.getLogger(AppFactoryGitblitLDAPUserService.class);

    private IStoredSettings settings;

    public AppFactoryGitblitLDAPUserService() {
        super();
    }

    @Override
    public void setup(IStoredSettings settings) {
        this.settings = settings;
        String file = settings.getString("realm.ldap.backingUserService", "users.conf");
        File realmFile = GitBlit.getFileOrFolder(file);

        serviceImpl = createUserService(realmFile);
        logger.info("LDAP User Service backed by " + serviceImpl.toString());
    }

    private LDAPConnection getLdapConnection() {
        try {
            URI ldapUrl = new URI(settings.getRequiredString("realm.ldap.server"));
            String bindUserName = settings.getString("realm.ldap.username", "");
            String bindPassword = settings.getString("realm.ldap.password", "");
            int ldapPort = ldapUrl.getPort();

            if (ldapUrl.getScheme().equalsIgnoreCase("ldaps")) {    // SSL
                if (ldapPort == -1)    // Default Port
                {
                    ldapPort = 636;
                }

                SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
                return new LDAPConnection(sslUtil.createSSLSocketFactory(), ldapUrl.getHost(), ldapPort, bindUserName, bindPassword);
            } else {
                if (ldapPort == -1)    // Default Port
                {
                    ldapPort = 389;
                }

                return new LDAPConnection(ldapUrl.getHost(), ldapPort, bindUserName, bindPassword);
            }
        } catch (URISyntaxException e) {
            logger.error("Bad LDAP URL, should be in the form: ldap(s)://<server>:<port>", e);
        } catch (GeneralSecurityException e) {
            logger.error("Unable to create SSL Connection", e);
        } catch (LDAPException e) {
            logger.error("Error Connecting to LDAP", e);
        }

        return null;
    }

    /**
     * Credentials are defined in the LDAP server and can not be manipulated
     * from Gitblit.
     *
     * @return false
     * @since 1.0.0
     */
    @Override
    public boolean supportsCredentialChanges() {
        return false;
    }

    /**
     * If no displayName pattern is defined then Gitblit can manage the display name.
     *
     * @return true if Gitblit can manage the user display name
     * @since 1.0.0
     */
    @Override
    public boolean supportsDisplayNameChanges() {
        return false;
    }

    /**
     * If no email pattern is defined then Gitblit can manage the email address.
     *
     * @return true if Gitblit can manage the user email address
     * @since 1.0.0
     */
    @Override
    public boolean supportsEmailAddressChanges() {
        return StringUtils.isEmpty(settings.getString("realm.ldap.email", ""));
    }


    /**
     * If the LDAP server will maintain team memberships then LdapUserService
     * will not allow team membership changes.  In this scenario all team
     * changes must be made on the LDAP server by the LDAP administrator.
     *
     * @return true or false
     * @since 1.0.0
     */
    public boolean supportsTeamMembershipChanges() {
        return !settings.getBoolean("realm.ldap.maintainTeams", false);
    }

    @Override
    public UserModel authenticate(String username, char[] password) {
        String simpleUsername = getSimpleUsername(username);

        LDAPConnection ldapConnection = getLdapConnection();
        if (ldapConnection != null) {
            // Find the logging in user's DN
            String accountBase = settings.getString("realm.ldap.accountBase", "");
            String accountPattern = settings.getString("realm.ldap.accountPattern", "(&(objectClass=person)(sAMAccountName=${username}))");
            accountPattern = StringUtils.replace(accountPattern, "${username}", escapeLDAPSearchFilter(simpleUsername));

            SearchResult result = doSearch(ldapConnection, accountBase, accountPattern);
            if (result != null && result.getEntryCount() == 1) {
                SearchResultEntry loggingInUser = result.getSearchEntries().get(0);
                String loggingInUserDN = loggingInUser.getDN();

                if (isAuthenticated(ldapConnection, loggingInUserDN, new String(password))) {
                    logger.debug("LDAP authenticated: " + username);

                    UserModel user = getUserModel(simpleUsername);
                    if (user == null)    // create user object for new authenticated user
                    {
                        user = new UserModel(simpleUsername);
                    }

                    // create a user cookie
                    if (StringUtils.isEmpty(user.cookie) && !ArrayUtils.isEmpty(password)) {
                        user.cookie = StringUtils.getSHA1(user.username + new String(password));
                    }

                    if (!supportsTeamMembershipChanges()) {
                        getTeamsFromLdap(ldapConnection, simpleUsername, loggingInUser, user);
                    }

                    // Get User Attributes
                    setUserAttributes(user, loggingInUser);

                    // Push the ldap looked up values to backing file
                    super.updateUserModel(user);
                    if (!supportsTeamMembershipChanges()) {
                        for (TeamModel userTeam : user.teams) {
                            updateTeamModel(userTeam);
                        }
                    }

                    return user;
                }
            }
        }

        return null;
    }

    private void setAdminAttribute(UserModel user) {
        user.canAdmin = false;
        List<String> admins = settings.getStrings("realm.ldap.admins");
        for (String admin : admins) {
            if (admin.startsWith("@")) { // Team
                if (user.getTeam(admin.substring(1)) != null) {
                    user.canAdmin = true;
                }
            } else if (user.getName().equalsIgnoreCase(admin)) {
                user.canAdmin = true;
            }
        }
    }

    private void setUserAttributes(UserModel user, SearchResultEntry userEntry) {
        // Is this user an admin?
        setAdminAttribute(user);

        // Don't want visibility into the real password, make up a dummy
        user.password = "StoredInLDAP";

        // Get full name Attribute
        String displayName = settings.getString("realm.ldap.displayName", "");
        if (!StringUtils.isEmpty(displayName)) {
            // Replace embedded ${} with attributes
            if (displayName.contains("${")) {
                for (Attribute userAttribute : userEntry.getAttributes()) {
                    displayName = StringUtils.replace(displayName, "${" + userAttribute.getName() + "}", userAttribute.getValue());
                }

                user.displayName = displayName;
            } else {
                user.displayName = userEntry.getAttribute(displayName).getValue();
            }
        }

        // Get email address Attribute
        String email = settings.getString("realm.ldap.email", "");
        if (!StringUtils.isEmpty(email)) {
            if (email.contains("${")) {
                for (Attribute userAttribute : userEntry.getAttributes()) {
                    email = StringUtils.replace(email, "${" + userAttribute.getName() + "}", userAttribute.getValue());
                }

                user.emailAddress = email;
            } else {
                user.emailAddress = userEntry.getAttribute(email).getValue();
            }
        }
    }

    private void getTeamsFromLdap(LDAPConnection ldapConnection, String simpleUsername,
                                  SearchResultEntry loggingInUser, UserModel user) {
        String loggingInUserDN = loggingInUser.getDN();

        ldapConnection = getLdapConnection();
        user.teams.clear();        // Clear the users team memberships - we're going to get them from LDAP
        String groupBase = settings.getString("realm.ldap.groupBase", "");
        String groupMemberPattern = settings.getString("realm.ldap.groupMemberPattern", "(&(objectClass=group)(member=${dn}))");

        groupMemberPattern = StringUtils.replace(groupMemberPattern, "${dn}", escapeLDAPSearchFilter(loggingInUserDN));
        groupMemberPattern = StringUtils.replace(groupMemberPattern, "${username}", escapeLDAPSearchFilter(simpleUsername));

        // Fill in attributes into groupMemberPattern
        for (Attribute userAttribute : loggingInUser.getAttributes()) {
            groupMemberPattern = StringUtils.replace(groupMemberPattern, "${" + userAttribute.getName() + "}", escapeLDAPSearchFilter(userAttribute.getValue()));
        }

        SearchResult teamMembershipResult = doSearch(ldapConnection, groupBase, groupMemberPattern);
        if (teamMembershipResult != null && teamMembershipResult.getEntryCount() > 0) {
            for (int i = 0; i < teamMembershipResult.getEntryCount(); i++) {
                SearchResultEntry teamEntry = teamMembershipResult.getSearchEntries().get(i);
                String teamName = teamEntry.getAttribute("cn").getValue();
                if (teamEntry.getDN().split(groupBase)[0].split(",").length >= 3) {
                    String appName = teamEntry.getDN().split(groupBase)[0].split(",")[2].split("=")[1];//.split("ou=")[0];
                    System.out.println("app name " + appName);
                    if ("developer".equals(teamName) || "appOwner".equals(teamName)) {
                        TeamModel teamModel = getTeamModel(appName);

                        if (teamModel == null) {

                            teamModel = createTeamFromLdap(appName);
                        }
                        teamModel.addRepository(appName + ".git");
                        user.teams.add(teamModel);
                        teamModel.addUser(user.getName());

                    }
                }
            }
        }
    }

    private TeamModel createTeamFromLdap(String appName) {
        TeamModel answer = new TeamModel(appName);
        // potentially retrieve other attributes here in the future

        return answer;
    }

    private SearchResult doSearch(LDAPConnection ldapConnection, String base, String filter) {
        try {
            System.out.println("User base " + base + " filter " + filter);
            System.out.println("Connection ");
            SearchResult result = ldapConnection.search(base, SearchScope.SUB, filter);
            System.out.println("result " + result.getEntryCount());
            return result;
        } catch (LDAPSearchException e) {
            logger.error("Problem Searching LDAP", e);

            return null;
        }
    }

    private boolean isAuthenticated(LDAPConnection ldapConnection, String userDn, String password) {
        try {
            // Binding will stop any LDAP-Injection Attacks since the searched-for user needs to bind to that DN
            ldapConnection.bind(userDn, password);
            return true;
        } catch (LDAPException e) {
            logger.error("Error authenticating user", e);
            return false;
        }
    }


    /**
     * Returns a simple username without any domain prefixes.
     *
     * @param username
     * @return a simple username
     */
    protected String getSimpleUsername(String username) {
        int lastSlash = username.lastIndexOf('\\');
        if (lastSlash > -1) {
            username = username.substring(lastSlash + 1);
        }

        return username;
    }

    // From: https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
    public static final String escapeLDAPSearchFilter(String filter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filter.length(); i++) {
            char curChar = filter.charAt(i);
            switch (curChar) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000':
                    sb.append("\\00");
                    break;
                default:
                    sb.append(curChar);
            }
        }
        return sb.toString();
    }
}