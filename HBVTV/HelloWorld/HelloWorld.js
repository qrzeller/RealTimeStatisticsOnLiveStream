/*
 * 	Institut für Rundfunktechnik (IRT | www.irt.de) Germany, 2014
 *	
 * 	Demo of simple HbbTV Application realised within the EU founded TV-Ring project. 
 * 	contact: contact@hbbtv-developer.com
 */

var header= "";
var content= "";

function initApp() 
{
	app_area = document.getElementById("app_area");
	info_field = document.getElementById("info_field");

	header = "Hello World!";
	content = 	"This is a simple HbbTV-Application that will be displayed "  +
				"on every SmartTV." + "<br/>" +
				"More information on how to develop apps for connected " +
				"TV devices that use the HbbTV standard can be found on " +
				"www.hbbtv-developer.com."

	document.getElementById("header_field").innerHTML = header;			
	document.getElementById("content_field").innerHTML = content;			
	
	// make <div>-container elements visible
	app_area.style.visibility = "visible";
	info_field.style.visibility = "hidden";
	
	//Needs to be called within broadcast related applications as expected within the HbbTV specification 6.2.2.4
	Application.show();
}



