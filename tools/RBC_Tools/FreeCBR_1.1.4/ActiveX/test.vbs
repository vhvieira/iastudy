option explicit

Const RESULT_SEPARATOR = ":"
Const CASE_SEPARATOR = ";"

dim i
dim j
dim fs
dim cbr
dim speed
dim sName(0)
dim sValue(0)
dim sWeight(0)
dim res
dim rArr
dim sArr
dim mess
dim caseNum
dim caseHit

set cbr = createobject("FreeCBR.CBR")
Set fs = CreateObject("Scripting.FileSystemObject")

cbr.initialize fs.GetParentFolderName(WScript.ScriptFullName) & "\..\PCs.txt", null
set fs = nothing
msgbox "Loaded " & cbr.getNumCases() & " cases with " & cbr.getNumFeatures() & " features."

sName(0) = "CPU speed (MHz)"
speed = inputbox("Please specify desired CPU speed:")
if speed = "" then
	msgbox "No value specified, so exiting..."
	set cbr = nothing
	WScript.Quit
end if

sValue(0) = speed
sWeight(0) = 1
res = cbr.searchAX(sName, sValue, sWeight, null, null, null, RESULT_SEPARATOR, CASE_SEPARATOR)
rArr = Split(res, CASE_SEPARATOR)
mess = "The result is:" & vbcrlf & vbcrlf
for i=lbound(rArr) to ubound(rArr)
	sArr = Split(rArr(i), RESULT_SEPARATOR)
	caseNum = cint(sArr(0))
	caseHit = sArr(1)
	mess = mess & caseHit & "%, "
	for j = 0 to cbr.getNumFeatures() - 1
		mess = mess & cbr.getFeatureName(j) & "=" & cbr.getFeatureValueAX(int(sArr(0)), j) & ", "
	next
	mess = mess & vbcrlf
next
msgbox mess
' To read all used values for a feature of type String or MultiString: 
res = cbr.getUsedStringValuesAX(0, ";")

set cbr = nothing

