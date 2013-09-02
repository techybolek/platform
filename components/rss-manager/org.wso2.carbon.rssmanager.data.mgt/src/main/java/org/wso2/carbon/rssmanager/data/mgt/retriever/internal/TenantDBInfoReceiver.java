package org.wso2.carbon.rssmanager.data.mgt.retriever.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.RSSServer;
import org.wso2.carbon.rssmanager.data.mgt.retriever.entity.datasource.TenantDBInfo;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class TenantDBInfoReceiver {

  

	public Map<String, TenantDBInfo> getTenantDBInformationMap() throws RSSManagerException {

		UsageManagerDataHolder dataHolder = UsageManagerDataHolder.getInstance();
		TenantManager tenantManager = dataHolder.getRealmService().getTenantManager();
		Set<Integer> allTenantIds = new HashSet<Integer>();
		Tenant[] tenants;
		
		try {
			int superTId = MultitenantConstants.SUPER_TENANT_ID;
			allTenantIds.add(superTId);
			tenants = tenantManager.getAllTenants();
		} catch (UserStoreException e) {
			throw new RSSManagerException(" Error while getting all tenants", e);
		}
		Map<String, TenantDBInfo> tenantDBInfoSet = new HashMap<String, TenantDBInfo>();

		for (Tenant tenant : tenants) {
			allTenantIds.add(tenant.getId());
		}		
		
		RSSEnvironment[]  environments = dataHolder.getRSSManagerService().getRSSEnvironments();
		if(environments == null || environments.length == 0){
			return tenantDBInfoSet;
		}
		
		for(Integer tId: allTenantIds){
			for(RSSEnvironment env : environments){
				RSSInstance[] rssInstances = env.getRSSInstances();
				if(rssInstances != null && rssInstances.length > 0){
					for(RSSInstance rssInstance : rssInstances){
						RSSEnvironmentContext ctx = new RSSEnvironmentContext();
						ctx.setEnvironmentName(env.getName());
						ctx.setRssInstanceName(rssInstance.getName());
						
						Database[] databases = dataHolder.getRSSManagerService().getDatabases(ctx, tId);
						for (Database db : databases) {
							try {
								TenantDBInfo tenantDBInfo =
								                            new TenantDBInfo(
								                                             String.valueOf(db.getId()),
								                                             db.getName(),
								                                             db.getType(),
								                                             db.getRssInstanceName(),
								                                             tId.toString(),
								                                             tenantManager.getDomain(tId));
								tenantDBInfoSet.put(tenantDBInfo.getDatabaseName(), tenantDBInfo);
							} catch (UserStoreException e) {
								throw new RSSManagerException(" Error while getting tenant domain info", e);
							}
						}
					}
					
				}
				
				
			}
		
		}

		return tenantDBInfoSet;
	}
    
	public Set<RSSServer> getRSSInstances() throws RSSManagerException {

		Set<RSSServer> rssServers = new HashSet<RSSServer>();

		UsageManagerDataHolder dataHolder = UsageManagerDataHolder.getInstance();		
		RSSEnvironment[]  environments = dataHolder.getRSSManagerService().getRSSEnvironments();
		if(environments == null || environments.length == 0){
			return rssServers;
		}
			
			for(RSSEnvironment env : environments){
				RSSInstance[] rssInstances = env.getRSSInstances();
				if(rssInstances != null && rssInstances.length > 0){
					for(RSSInstance instance : rssInstances){
						RSSServer rssServer =
			                      new RSSServer(String.valueOf(instance.getId()),
			                                    instance.getDataSourceConfig().getUrl(),
			                                    instance.getDbmsType(),
			                                    instance.getDataSourceConfig().getUsername(),
			                                    instance.getDataSourceConfig().getPassword());
						rssServers.add(rssServer);
					}
				}
			}


		return rssServers;
	}
}
