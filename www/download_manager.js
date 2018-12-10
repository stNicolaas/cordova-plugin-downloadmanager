var exec = require('cordova/exec');

exports.download = function(url, title, success, error) {
    exec(success, error, "DownloadManager", "download", [url, title]);
};

exports.status = function(arg0, success, error) {
    exec(success, error, "DownloadManager", "status", [arg0]);
};
