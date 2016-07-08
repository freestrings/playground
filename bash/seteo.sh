set -ex
VAR="Hello World!"
function changeVar {
local VAR="local var"
echo $VAR
}
echo $VAR
changeVar
echo $VAR
