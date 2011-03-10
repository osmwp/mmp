function onLoad()
{
    resultNode = document.getElementById("result");

    try {
		jsonrpc = new JSONRpcClient(jsonurl);
		doListMethods();
	} catch(e) {
    	if(e.message) alert(e.message);
		else alert(e);
    }
}

function clickSubmit()
{
    var whoNode = document.getElementById("who");
    var result = jsonrpc.applicationCatalog.getApplicationLabelList();
    alert("The server replied: " + result);
}

function clrscr()
{
    resultNode.value = "";
}

function print(s)
{
    resultNode.value += "" + s;
    resultNode.scrollTop = resultNode.scrollHeight;
}

function doCall()
{
	clrscr();
	var callNode = document.getElementById("method");
	var paramsNode = document.getElementById("params");
	print(callNode.value +"("+paramsNode.value+")\n\n");
	var names = callNode.value.split(".");
	try {
		print(dump(jsonrpc[names[0]][names[1]](paramsNode.value)));
    } catch(e) {
		print("Exception: \n\n" + e);
    }
}

function doListMethods()
{
    clrscr();
    var method = document.getElementById("method");
    try {
		var rslt = jsonrpc.system.listMethods();
		rslt.sort();
		method.length = 0;
		if (rslt.length != 0) {
			method.disabled = false;
			method.options[method.length] = new Option('system.listMethods', 'system.listMethods');
			for(var i=0; i < rslt.length; i++) {
			    method.options[method.length] = new Option(rslt[i],rslt[i]);
			}
		} else {
			methods.disabled = true;
		}
    } catch(e) {
    	method.length = 0;
		method.disabled = true;
		print("Exception: \n\n" + e);
    }
}



/**
* Function : dump()
* Arguments: The data - array,hash(associative array),object
*    The level - OPTIONAL
* Returns  : The textual representation of the array.
* This function was inspired by the print_r function of PHP.
* This will accept some data as the argument and return a
* text that will be a more readable version of the
* array/hash/object that is given.
*/
function dump(arr,level) {
var dumped_text = "";
if(!level) level = 0;

//The padding given at the beginning of the line.
var level_padding = "";
for(var j=0;j<level+1;j++) level_padding += "|    ";

if(typeof(arr) == 'object') { //Array/Hashes/Objects
 for(var item in arr) {
  var value = arr[item];

  if(typeof(value) == 'object') { //If it is an array,
   dumped_text += level_padding + "'" + item + "' ...\n";
   dumped_text += dump(value,level+1);
  } else {
   dumped_text += level_padding + "'" + item + "' => \"" + value + "\"\n";
  }
 }
} else { //Stings/Chars/Numbers etc.
 dumped_text = "===>"+arr+"<===("+typeof(arr)+")";
}
return dumped_text;
}