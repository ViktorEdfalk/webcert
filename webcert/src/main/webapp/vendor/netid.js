//-----------------------------------------------------------------------
// Globals
//-----------------------------------------------------------------------
var IID_NAME_OBJECT = "iid_object";
var IID_NAME_PLACE = "iid_place_holder";
var IID_DEVICE_INFO = null;
var IID_AVAILABLE_CHECK = false;
var IID_AVAILABLE = false;
var IID_JS_BRIDGE = null;
//-----------------------------------------------------------------------------
// iid_SetProperty
//-----------------------------------------------------------------------------
function iid_SetProperty(name, value) {
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            iid.SetProperty(name, value);
        }
        catch (err) { }
    }
    return;
}
//-----------------------------------------------------------------------------
// iid_GetProperty
//-----------------------------------------------------------------------------
function iid_GetProperty(name) {
    var value = "";
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            value = iid.GetProperty(name);
        }
        catch (err) {
        }
    }
    return value;
}
//-----------------------------------------------------------------------------
// iid_EnumProperty
//-----------------------------------------------------------------------------
function iid_EnumProperty(name, index) {
    var value = "";
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            value = iid.EnumProperty(name, index);
        }
        catch (err) { }
    }
    return value;
}
//-----------------------------------------------------------------------------
// iid_Invoke
//-----------------------------------------------------------------------------
function iid_Invoke(name) {
    var value = "";
    var iid = iid_GetObject();
    if (iid != null) {
        try {
            value = iid.Invoke(name);
        }
        catch (err) { }
    }
    return value;
}
//-----------------------------------------------------------------------
// iid_GetDeviceInfo
//-----------------------------------------------------------------------
function iid_GetDeviceInfo() {
    if (IID_DEVICE_INFO == null) {
        IID_DEVICE_INFO = new iid_DeviceInfo(null);
    }
    return IID_DEVICE_INFO;
}
//-----------------------------------------------------------------------
// iid_IsExplorer
//-----------------------------------------------------------------------
function iid_IsExplorer() {
    var explorer = false;
    var info = null;
    if ((info = iid_GetDeviceInfo()) != null) {
        explorer = info.browser.explorer;
    }
    return explorer;
}
//-----------------------------------------------------------------------
// iid_IsDeviceType
//-----------------------------------------------------------------------
function iid_IsDeviceType(types) {
    var found = false;
    var info = null;
    var i = 0;
    var type = null;
    if ((info = iid_GetDeviceInfo()) != null) {
        while ((type = iid_GetPartBy(types, i, ';')) != "") {
            if (info.device.name.indexOf(type) != -1) {
                found = true;
                break;
            }
            i++;
        }
    }
    return found;
}
//-----------------------------------------------------------------------
// iid_IsAvailable
//-----------------------------------------------------------------------
function iid_IsAvailable() {
    var available = false;
    var explorer = false;
    var name = null;
    var elem = null;
    var elem2 = null;
    // At first call mark that available check is completed
    IID_AVAILABLE_CHECK = true;
    // Always start check for Javascript interface
    if (iid_HasJavascriptInterface()) {
        available = true;
    }
    // follow with check for Javascript bridge
    else if (iid_HasJavascriptBridge()) {
        available = true;
    }
    else {
        // Always use same div id as place holder for create of plugin
        name = IID_NAME_PLACE;
        if (document.getElementById(name) == null) {
            // Not declared, so add our special tag last in body (not allowed for setup)
            if (top.window.location.href.indexOf("setup.html") == -1) {
                elem2 = document.createElement("div");
                elem2.setAttribute("id", name);
                document.body.appendChild(elem2);
            }
        }
        // Start checking already created
        if (iid_GetObject() != null) {
            available = true;
        }
        // Require place holder defined (used to declare plugin object)
        else if (document.getElementById(name) != null) {
            explorer = iid_IsExplorer();
            // First try Net iD Enterprise plugin
            if (!(available = iid_Declare(name, explorer, false))) {
                // Second try Net iD Live plugin (only explorer)
                if (explorer) {
                    available = iid_Declare(name, explorer, true);
                }
            }
        }
    }
    IID_AVAILABLE = available;
    return available;
}
//-----------------------------------------------------------------------
// iid_HasJavascriptInterface
//-----------------------------------------------------------------------
function iid_HasJavascriptInterface() {
    var available = false;
    if (iid_IsDeviceType("Android")) {
        if (window.JSInterface != null) {
            if ((window.JSInterface.SetProperty != null) &&
                (window.JSInterface.GetProperty != null) &&
                (window.JSInterface.EnumProperty != null) &&
                (window.JSInterface.Invoke != null)) {
                available = true;
            }
        }
    }
    return available;
}
//-----------------------------------------------------------------------
// iid_HasJavascriptBridge
//-----------------------------------------------------------------------
function iid_HasJavascriptBridge() {
    var available = false;
    if (iid_IsDeviceType("iPhone;iPad")) {
        if (IID_JS_BRIDGE == null) {
            IID_JS_BRIDGE = new IID_JavascriptBridge();
        }
        if (IID_JS_BRIDGE != null) {
            available = IID_JS_BRIDGE.Available();
        }
    }
    return available;
}
//-----------------------------------------------------------------------
// iid_GetDeclare
//-----------------------------------------------------------------------
function iid_GetDeclare(explorer, live) {
    var value = "";
    var clsid = "";
    var name = "";
    if (live) {
        name = "application/x-iid-live"
        clsid = "5BF56AD2-E297-416E-BC49-00B327C4428E";
    }
    else {
        name = "application/x-iid"
        clsid = "5BF56AD2-E297-416E-BC49-00B327C4426E";
    }
    if (explorer) {
        value = "<object name='" + IID_NAME_OBJECT + "' id='" + IID_NAME_OBJECT + "' classid='CLSID:";
        value += clsid;
        value += "' width='0' height='0'></object>";
    }
    else {
        value = "<object name='" + IID_NAME_OBJECT + "' id='" + IID_NAME_OBJECT + "' type='";
        value += name;
        value += "' width='0' height='0'></object>";
    }
    return value;
}
//-----------------------------------------------------------------------
// iid_GetObject
//-----------------------------------------------------------------------
function iid_GetObject() {
    var obj = null;
    if (!IID_AVAILABLE_CHECK) {
        iid_IsAvailable();
    }
    if (IID_AVAILABLE) {
        if (iid_HasJavascriptInterface()) {
            obj = window.JSInterface;
        }
        else if (iid_HasJavascriptBridge()) {
            obj = IID_JS_BRIDGE;
        }
        else {
            obj = document.getElementById(IID_NAME_OBJECT);
        }
    }
    return obj;
}
//-----------------------------------------------------------------------
// iid_Declare
//-----------------------------------------------------------------------
function iid_Declare(name, explorer, live) {
    var success = false;
    var iid_place = null;
    var iid_object = null;
    var version = 0;
    var count = 0;
    iid_place = document.getElementById(name);
    if (iid_place != null) {
        // Get object declaration
        iid_place.innerHTML = iid_GetDeclare(explorer, live);
        // Require plugin object return something useful
        if ((iid_object = document.getElementById(IID_NAME_OBJECT)) != null) {
            try {
                version = iid_object.GetProperty("Version");
                if ((version != null) && (version.length > 0)) {
                    success = true;
                }
            }
            catch (ex) {
            }
        }
    }
    return success;
}
//-----------------------------------------------------------------------------
// Javascript bridge 
//-----------------------------------------------------------------------------
function iid_GetJavascriptBridgeResponseValue() {
    var value = null;
    try {
	    if (typeof localStorage == "object") {
	        if (localStorage.getItem != null) {
		    value = localStorage.getItem("iid_JavascriptBridgeResponseValue");
	        }
	    }
    }
    catch (ex) {
    }
    return value;
}
function iid_SetJavascriptBridgeResponseValue(value) {
    try {
	if (typeof localStorage == "object") {
	    if (localStorage.setItem != null) {
		localStorage.setItem("iid_JavascriptBridgeResponseValue", value);
	    }
	}
    }
    catch (ex) {
    }
    return iid_GetJavascriptBridgeResponseValue();
}
function iid_JavascriptBridgeResponse(result) {
    return iid_SetJavascriptBridgeResponseValue(IID_URL.decode(result));
}
function IID_JavascriptBridge() {
    // Members
    this._available = false;
    this._count = 0;
    // Function Available
    this.Available = function() {
        if (this._available) {
	    this._count += 1;
        }
        else {
            this._available = true;
            if (this.GetProperty("Version") == null) {
                this._available = false;
            }
        }
        return this._available;
    }
    // Function GetProperty
    this.GetProperty = function(name) {
        var result = null;
        if (this.Available()) {
            result = this.Send("GetProperty", name, null, null);
        }
        return result;
    }
    // Function SetProperty
    this.SetProperty = function(name, value) {
        var result = null;
        if (this.Available()) {
            result = this.Send("SetProperty", name, null, value);
        }
        return result;
    }
    // Function EnumProperty
    this.EnumProperty = function(name, index) {
        var result = null;
        if (this.Available()) {
            result = this.Send("EnumProperty", name, index, null);
        }
        return result;
    }
    // Function Invoke
    this.Invoke = function(name) {
        var result = null;
        if (this.Available()) {
            result = this.Send("Invoke", name, null, null);
        }
        return result;
    }
    // Function Send
    this.Send = function(func, name, index, value) {
	var result = null;
	var info = "";
        var iframe = null;
        try {
	    if ((func != null) && (name != null)) {
		if (index == null) {
		    index = "";
		}
		if (value == null) {
		    value = "";
		}
		if ((iframe = document.createElement("iframe")) != null) {
		    info = "iidjs://?count=" + this._count;
		    info += "&func=" + func;
		    info += "&name=" + IID_URL.encode(name);
		    info += "&index=" + index;
		    info += "&value=" + IID_URL.encode(value);
		    info += "&response=iid_JavascriptBridgeResponse";
		    iframe.src = info;
		    iframe.style.display = "none";
		    iframe.name = "iid_JavascriptBridgeFrame";
		    iframe.id = "iid_JavascriptBridgeFrame";
		    iframe.width = 0;
		    iframe.height = 0;
		    document.getElementsByTagName("body")[0].appendChild(iframe);
		    iframe.parentNode.removeChild(iframe);
		    iframe = null;
		    result = iid_GetJavascriptBridgeResponseValue();
		}
	    }
        }
        catch (ex) {
        }
        return result;
    }
}
//-----------------------------------------------------------------------------
// iid_NameVersionInfo
//-----------------------------------------------------------------------------
function iid_NameVersionInfo(name, version) {
    this.name = name;
    this.version = version;
    this.explorer = (name.indexOf("MSIE") >= 0);
}
//-----------------------------------------------------------------------------
// iid_ParseBrowserInfo
//-----------------------------------------------------------------------------
function iid_ParseBrowserInfo(value) {
    var name = null;
    var version = null;
    var i = 0;
    var j = 0;
    // Get browser name and version, either last entry or known string
    if ((i = value.indexOf("MSIE ")) > 0) {
        // Microsoft Internet Explorer
        name = "MSIE";
        i += 5;
        j = 0;
        while ((value.charAt(i + j) != '\0') && (value.charAt(i + j) != ';')) {
            j++;
        }
        version = value.substr(i, j);
    }
    // For IE11 Microsoft have changed completly, look for .NET or Trident
    else if ((value.indexOf(".NET") > 0) || (value.indexOf("Trident") > 0)) {
        name = "MSIE";
        if ((i = value.indexOf("; rv:")) > 0) {
            i += 5;
            j = 0;
            while ((value.charAt(i + j) != '\0') && (value.charAt(i + j) != ')')) {
                j++;
            }
            version = value.substr(i, j);
        }
    }
    // Chrome/Chromium include WebKit, so take first (<name>/<version>)
    else if (((i = value.lastIndexOf("Chromium")) > 0) ||
             ((i = value.lastIndexOf("Chrome")) > 0)) {
        j = 0;
        while ((value.charAt(i + j) != 0) && (value.charAt(i + j) != '/')) {
            j++;
        }
        name = value.substr(i, j);
        version = value.substr(i + j + 1);
        if ((i = version.indexOf(' ')) > 0) {
            version = version.substr(0, i);
        }
    }
    // WebKit are availble in different flavors (???WebKit/<version>)
    else if ((i = value.indexOf("WebKit")) > 0) {
        j = i + 6;
        while ((i > 0) && (value.charAt(i - 1) != ' ')) {
            i--;
        }
        name = value.substr(i, j - i);
        i = j + 1;
        j = 0;
        while ((value.charAt(i + j) != 0) && (value.charAt(i + j) != ' ')) {
            j++;
        }
        version = value.substr(i, j);
    }
    // Let's hope last entry tells browser (<name>/<version>)
    else if ((i = value.lastIndexOf(" ")) > 0) {
        i++;
        while ((value.charAt(i + j) != 0) && (value.charAt(i + j) != '/')) {
            j++;
        }
        name = value.substr(i, j);
        version = value.substr(i + j + 1);
    }
    // Need default value
    if (name == null) {
        name = "Unknown";
    }
    if (version == null) {
        version = "0.0";
    }
    // Create name version info object
    return new iid_NameVersionInfo(name, version);
}
//-----------------------------------------------------------------------------
// iid_ParseDeviceInfo
//-----------------------------------------------------------------------------
function iid_ParseDeviceInfo(value) {
    var name = null;
    var version = null;
    var i = 0;
    var j = 0;
    var k = 0;
    var part = null;
    // Get os name and version. Information is stored a little different
    // depending on browser, but available between '(' and ')'.
    if ((i = value.indexOf('(')) > 0) {
        value = value.substr(i + 1);
        if ((i = value.indexOf(')')) > 0) {
            value = value.substr(0, i);
        }
    }
    // Enumerate all arguments and try to extract information
    i = 0;
    while ((part = iid_GetPartBy(value, i, ';')) != "") {
        // Format "<name>" or "...<name>...<version>"
        if (((j = part.indexOf("Windows")) >= 0) ||
            ((j = part.indexOf("Android")) >= 0) ||
            ((j = part.indexOf("iPhone")) >= 0) ||
            ((j = part.indexOf("iPad")) >= 0) ||
            ((j = part.indexOf("CPU OS")) >= 0) || // iPad
            ((j = part.indexOf("Macintosh")) >= 0) ||
            ((j = part.indexOf("Mac OS X")) >= 0)) {
            // Get eventual following version number
            k = 0;
            while ((part.length > (j + k)) &&
                   ((part.charAt(j + k) < '0') || (part.charAt(j + k) > '9'))) {
                k++;
            }
            // Set all except version number as name (Android may be specified with Linux too)
            if ((name == null) || (name == "Linux")) {
                name = part.substr(j, k).replace(/^\s+|\s+$/g, "");
            }
            // Found version?
            part = part.substr(j + k).replace(/^\s+|\s+$/g, "");
            k = 0;
            if ((part.charAt(0) >= '0') && (part.charAt(0) <= '9')) {
                part = part.replace(new RegExp("_", 'g'), ".");
                k = 0;
                while ((part.charAt(k) == '.') ||
                       ((part.charAt(k) >= '0') && (part.charAt(k) <= '9'))) {
                    k++;
                }
                version = part.substr(0, k);
            }
            // Break when we have both name and version
            if ((name != null) && (version != null)) {
                break;
            }
        }
        // Linux only namn no version
        else if ((j = part.indexOf("Linux")) >= 0) {
            name = "Linux";
        }
        i++;
    }
    // Need default value
    if (name == null) {
        name = navigator.platform;
    }
    if (version == null) {
        version = "0.0";
    }
    // Create name version info object
    return new iid_NameVersionInfo(name, version);
}
//-----------------------------------------------------------------------------
// iid_DeviceInfo
//-----------------------------------------------------------------------------
function iid_DeviceInfo(info) {
    if (info == null) {
        info = navigator.userAgent;
    }
    // Get os name and version
    this.device = iid_ParseDeviceInfo(info);
    // Get browser name and version
    this.browser = iid_ParseBrowserInfo(info);
}
//-----------------------------------------------------------------------------
// iid_GetPartBy
//-----------------------------------------------------------------------------
function iid_GetPartBy(text, pos, c) {
    var text2 = "";
    if ((text != null) && (text != "")) {
        text2 = text;
        while ((text2 != "") && (pos > 0)) {
            if (text2.indexOf(c) > 0) {
                text2 = text2.substr(text2.indexOf(c) + 1, text2.length - text2.indexOf(c) - 1);
            }
            else if (text2.indexOf(c) == 0) {
                text2 = text2.substr(1, text2.length);
            }
            else {
                text2 = "";
            }
            pos--;
        }
        if (text2 != "") {
            if (text2.indexOf(c) > 0) {
                text2 = text2.substr(0, text2.indexOf(c));
            }
            else if (text2.indexOf(c) == 0) {
                text2 = "";
            }
        }
    }
    return text2;
}
//-----------------------------------------------------------------------------
// URL converter
//-----------------------------------------------------------------------------
var IID_URL = {
    encode: function(value) {
        if (value != null) {
            // Replace all '+' with '%2B' to avoid problem below
            value = ReplaceAll(value, "+", "%2B");
            // Convert all
            value = escape(value);
        }
        return value;
    },
    decode: function(value) {
        if (value != null) {
            // '+' are not removed by unescape, so start with replacing them
            value = ReplaceAll(value, "+", "%20");
            // Convert all
            value = unescape(value);
        }
        return value;
    }
}
