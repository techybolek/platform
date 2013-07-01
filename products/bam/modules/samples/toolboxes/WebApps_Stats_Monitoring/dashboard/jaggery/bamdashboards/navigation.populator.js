var esb_mediation_stats = ['ESB Mediation Statistics',[['ESB - Proxy','esb_proxy.jsp'],['ESB - Sequence','esb_sequence.jsp'],['ESB - Endpoint','esb_endpoint.jsp']]];
var as_service_stats = ['AS Service Statistics',[['Service Statistics','index.jsp']]];
var activity_monitoring = ['Activity Monitoring',[['Activity Monitoring','index.jsp']]];
var channel_monitoring = ['Mobile/Web Channel Monitoring',[['Channel Monitoring','index.jsp']]];
var jmx_stats = ['JMX Statistics',[['JMX Stats','index.jsp']]];
var as_webapp_stats = ['AS Webapp Statistics',[['Web Application Statistics','index.jsp']]];

$(document).ready(function(){
			var dashboardurl = '..';
			$.ajax({
				type: "GET",
				url: "../getDeployedToolboxes.jag",
				dataType: "json",
				success: function(json) {
					var deployedToolboxes = json;					
					for (var i=0; i<deployedToolboxes.toolboxes.length; i++){
						var navstring = '<li class="nav-header">'+deployedToolboxes.toolboxes[i].dashboard+'</li>';
						for (var k=0; k<deployedToolboxes.toolboxes[i].childDashboards.length; k++){
							navstring = navstring + '<li><a href="'+dashboardurl+'/'+deployedToolboxes.toolboxes[i].childDashboards[k][1]+'">'+deployedToolboxes.toolboxes[i].childDashboards[k][0]+'</a></li>';
						}
		       				if(navstring){
							$("#leftnav").append(navstring);
						}
					}
					
				}
			});
		});
