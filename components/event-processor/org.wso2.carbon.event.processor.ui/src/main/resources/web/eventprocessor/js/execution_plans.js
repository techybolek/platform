function removeExportedStream(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Exported stream removed successfully!!");
    return;
}


function removeImportedStreamDefinition(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Imported stream removed successfully!!");
    return;
}


