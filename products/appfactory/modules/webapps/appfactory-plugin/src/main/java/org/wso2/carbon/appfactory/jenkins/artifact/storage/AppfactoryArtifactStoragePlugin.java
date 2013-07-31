package org.wso2.carbon.appfactory.jenkins.artifact.storage;

import hudson.Plugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;
import org.wso2.carbon.appfactory.core.Deployer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ExportedBean
public class AppfactoryArtifactStoragePlugin extends Plugin {

    private static final Log log = LogFactory.getLog(AppfactoryArtifactStoragePlugin.class);
    /**
     * This method serves the requests coming under <jenkins-url>/plugin/<plugin-name>
     * @param req request
     * @param rsp response
     * @throws IOException
     * @throws ServletException
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {

//        First we check the action.
//        The action get tag names does not depend on the deployer.
        String action = req.getRestOfPath();
        if ("/getTagNamesOfPersistedArtifacts".equals(action)) {

            Utils.getTagNamesOfPersistedArtifacts(req, rsp);
        }else{
//        First we check what is the class that we need to invoke
            String className = req.getParameter("Deployer.ClassName");

            Deployer deployer;

            try {
                ClassLoader loader = getClass().getClassLoader();
                Class<?> customCodeClass = Class.forName(className, true, loader);
                deployer = (Deployer) customCodeClass.newInstance();

                //        log.info(" action : "+action);

                if ("/deployTaggedArtifact".equals(action)) {
                    deployer.deployTaggedArtifact(Utils.getParameterMapFromRequest(req));
                } else if ("/deployLatestSuccessArtifact".equals(action)) {
                    deployer.deployLatestSuccessArtifact(Utils.getParameterMapFromRequest(req));
                } else if("/deployPromotedArtifact".equals(action)) {
                    deployer.deployPromotedArtifact(Utils.getParameterMapFromRequest(req));
                } else {
                    rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    throw new ServletException("Invalid action");
                }
            } catch (ClassNotFoundException e) {
//                log.error(e);
                throw new ServletException(e);
            } catch (InstantiationException e) {
//                log.error(e);
                throw new ServletException(e);
            } catch (IllegalAccessException e) {
//                log.error(e);
                throw new ServletException(e);
            } catch (Exception e) {
//                log.error(e);
                throw new ServletException(e);
            }


        }
    }
}
