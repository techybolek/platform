var updateSubscription = function (apiName, version, provider, appId, newstatus, link) {
    //var ahrefId = $('#' + apiName + provider + appId);
    var ahrefId = $(link);
    var status = ahrefId.text();
    var blockType = getRadioValue($('input[name=blockType'+n+']:radio:checked'));
    var newStatus;
    if (status.trim().toUpperCase() == 'Unblock'.toUpperCase()) {
        newStatus = 'UNBLOCKED';
        $('input[name=blockType'+n+']:radio:checked').removeAttr("checked");
    } else if(blockType == 'blockProduction') {
    	newStatus = 'PROD_ONLY_BLOCKED';
    } else {
        newStatus = 'BLOCKED';
    }
    jagg.post("/site/blocks/users-keys/ajax/subscriptions.jag", {
        action:"updateSubscription",
        apiName:apiName,
        version:version,
        provider:provider,
        appId:appId,
        newStatus:newStatus
    }, function (result) {
        if (!result.error) {
            if (newStatus == 'UNBLOCKED') {
                ahrefId.html('<i class="icon-ban-circle"></i> Block');
            } else {
                ahrefId.html('<i class="icon-ok-circle"></i> Unblock');
            }

        } else {
            jagg.message({content:result.message, type:"error"});
        }


    }, "json");


}

var getRadioValue = function (radioButton) {
    if (radioButton.length > 0) {
        return radioButton.val();
    }
    else {
        return 0;
    }
};