var IntellicarePlugin = { 
    setTitle: function (success, fail, titleString) 
    { 
		return cordova.exec(success, fail, "edu.northwestern.cbits.ic_template.cordova_plugins.IntellicarePlugin", "setTitle", [titleString]); 
    }, 
    setSubtitle: function (success, fail, titleString) 
    { 
		return cordova.exec(success, fail, "edu.northwestern.cbits.ic_template.cordova_plugins.IntellicarePlugin", "setSubtitle", [titleString]); 
    } 
};
