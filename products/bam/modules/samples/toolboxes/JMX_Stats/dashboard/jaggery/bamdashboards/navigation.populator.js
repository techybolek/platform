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
