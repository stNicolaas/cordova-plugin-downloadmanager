var exec = require('cordova/exec');

exports.download = function(arg0, success, error) {
    exec(success, error, "DownloadManager", "download", [arg0]);
};

exports.status = function(arg0, success, error) {
    exec(success, error, "DownloadManager", "status", [arg0]);
};
